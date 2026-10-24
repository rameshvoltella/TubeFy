package com.ramzmania.tubefy.core.recognition

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin

/**
 * The audio "fingerprint" Shazam's recognition works with: the strongest frequency peaks of a short
 * recording, in four frequency bands. A Kotlin port of SongRec's signature generator
 * (https://github.com/marin-m/SongRec, GPL-3.0), which reimplements the format Shazam uses.
 * Only this fingerprint is sent for recognition, never the recording itself.
 */
class ShazamSignature(val sampleRateHz: Int, val numberSamples: Int, val bands: List<List<Peak>>) {

    class Peak(val fftPassNumber: Int, val magnitude: Int, val correctedBin: Int)

    /** The binary signature (little-endian, CRC-32 protected), as Shazam expects it. */
    fun encode(): ByteArray {
        val peaksBuffer = java.io.ByteArrayOutputStream()
        bands.forEachIndexed { band, peaks ->
            if (peaks.isEmpty()) return@forEachIndexed
            val bandBytes = java.io.ByteArrayOutputStream()
            var pass = 0
            peaks.forEach { peak ->
                if (peak.fftPassNumber - pass >= 255) {
                    bandBytes.write(0xff)
                    bandBytes.write(le32(peak.fftPassNumber))
                    pass = peak.fftPassNumber
                }
                bandBytes.write(peak.fftPassNumber - pass)
                bandBytes.write(le16(peak.magnitude))
                bandBytes.write(le16(peak.correctedBin))
                pass = peak.fftPassNumber
            }
            val bytes = bandBytes.toByteArray()
            peaksBuffer.write(le32(0x60030040 + band))
            peaksBuffer.write(le32(bytes.size))
            peaksBuffer.write(bytes)
            repeat((4 - bytes.size % 4) % 4) { peaksBuffer.write(0) }
        }
        val peaks = peaksBuffer.toByteArray()
        val size = 48 + 8 + peaks.size
        val out = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
        out.putInt(0xcafe2580.toInt())
        out.putInt(0) // CRC-32, filled in below
        out.putInt(size - 48)
        out.putInt(0x94119c00.toInt())
        out.putInt(0); out.putInt(0); out.putInt(0)
        out.putInt(sampleRateId(sampleRateHz) shl 27)
        out.putInt(0); out.putInt(0)
        out.putInt(numberSamples + (sampleRateHz * 0.24f).toInt())
        out.putInt((15 shl 19) + 0x40000)
        out.putInt(0x40000000)
        out.putInt(size - 48)
        out.put(peaks)
        val bytes = out.array()
        val crc = CRC32().apply { update(bytes, 8, bytes.size - 8) }.value
        ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(crc.toInt())
        return bytes
    }

    private fun sampleRateId(hz: Int) = when (hz) {
        8000 -> 1; 11025 -> 2; 16000 -> 3; 32000 -> 4; 44100 -> 5; 48000 -> 6
        else -> error("Unsupported sample rate $hz")
    }

    private fun le32(value: Int) = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    private fun le16(value: Int) = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value.toShort()).array()

    companion object {
        const val SAMPLE_RATE = 16_000
        const val DATA_URI_PREFIX = "data:audio/vnd.shazam.sig;base64,"

        /** The fingerprint of 16 kHz mono 16-bit samples. */
        fun from(samples: ShortArray): ShazamSignature = Generator(samples.size).apply { process(samples) }.result()
    }

    /** SongRec's algorithm: FFTs every 128 samples, peak spreading, then peak picking. */
    private class Generator(private val numberSamples: Int) {
        private val ring = ShortArray(2048)
        private var ringIndex = 0
        private val fftOutputs = Array(256) { FloatArray(1025) }
        private var fftIndex = 0 // wraps at 256 like SongRec's u8
        private val spreadOutputs = Array(256) { FloatArray(1025) }
        private var spreadIndex = 0
        private var spreadDone = 0
        private val bands = List(4) { mutableListOf<Peak>() }

        // Work buffers for the 2048-point FFT
        private val re = DoubleArray(2048)
        private val im = DoubleArray(2048)

        fun process(samples: ShortArray) {
            var offset = 0
            while (offset + 128 <= samples.size) {
                doFft(samples, offset)
                doPeakSpreading()
                spreadDone++
                if (spreadDone >= 46) doPeakRecognition()
                offset += 128
            }
        }

        fun result() = ShazamSignature(SAMPLE_RATE, numberSamples, bands)

        private fun doFft(samples: ShortArray, offset: Int) {
            System.arraycopy(samples, offset, ring, ringIndex, 128)
            ringIndex = (ringIndex + 128) and 2047
            // Latest data last, with the Hanning window
            for (i in 0 until 2048) {
                re[i] = (ring[(i + ringIndex) and 2047] * HANNING[i]).toDouble()
                im[i] = 0.0
            }
            Fft.transform(re, im)
            val out = fftOutputs[fftIndex]
            for (i in 0..1024) {
                out[i] = max(((re[i] * re[i] + im[i] * im[i]) / (1 shl 17)).toFloat(), 0.0000000001f)
            }
            fftIndex = (fftIndex + 1) and 255
        }

        private fun doPeakSpreading() {
            val latest = fftOutputs[(fftIndex - 1) and 255]
            val spread = spreadOutputs[spreadIndex]
            System.arraycopy(latest, 0, spread, 0, 1025)
            // Frequency-domain spreading
            for (position in 0..1022) {
                spread[position] = max(max(spread[position], spread[position + 1]), spread[position + 2])
            }
            // Time-domain spreading
            val copy = spread.copyOf()
            for (position in 0..1024) {
                for (former in intArrayOf(1, 3, 6)) {
                    val formerOutput = spreadOutputs[(spreadIndex - former) and 255]
                    formerOutput[position] = max(formerOutput[position], copy[position])
                }
            }
            spreadIndex = (spreadIndex + 1) and 255
        }

        private fun doPeakRecognition() {
            val fftMinus46 = fftOutputs[(fftIndex - 46) and 255]
            val fftMinus49 = spreadOutputs[(spreadIndex - 49) and 255]
            for (bin in 10..1014) {
                if (fftMinus46[bin] < 1f / 64f || fftMinus46[bin] < fftMinus49[bin - 1]) continue
                // A frequency-domain local maximum…
                var maxNeighbor = 0f
                for (offset in intArrayOf(-10, -7, -4, -3, 1, 2, 5, 8)) maxNeighbor = max(maxNeighbor, fftMinus49[bin + offset])
                if (fftMinus46[bin] <= maxNeighbor) continue
                // …and a time-domain one
                var maxOther = maxNeighbor
                for (offset in intArrayOf(-53, -45, 165, 172, 179, 186, 193, 200, 214, 221, 228, 235, 242, 249)) {
                    maxOther = max(maxOther, spreadOutputs[(spreadIndex + offset) and 255][bin - 1])
                }
                if (fftMinus46[bin] <= maxOther) continue

                val pass = spreadDone - 46
                fun magnitude(value: Float) = max(ln(value), 1f / 64f) * 1477.3f + 6144f
                val peak = magnitude(fftMinus46[bin])
                val before = magnitude(fftMinus46[bin - 1])
                val after = magnitude(fftMinus46[bin + 1])
                val variation1 = peak * 2 - before - after
                val variation2 = (after - before) * 32 / variation1
                val correctedBin = ((bin * 64) + variation2.toInt()) and 0xffff
                val frequencyHz = correctedBin * (16000f / 2f / 1024f / 64f)
                val band = when (frequencyHz.toInt()) {
                    in 250..519 -> 0
                    in 520..1449 -> 1
                    in 1450..3499 -> 2
                    in 3500..5500 -> 3
                    else -> continue
                }
                bands[band] += Peak(pass, peak.toInt() and 0xffff, correctedBin)
            }
        }
    }

    /** In-place radix-2 FFT of 2048 points. */
    private object Fft {
        private const val N = 2048
        private val cosTable = DoubleArray(N / 2) { cos(2 * PI * it / N) }
        private val sinTable = DoubleArray(N / 2) { sin(2 * PI * it / N) }
        private val reversed = IntArray(N) { i -> Integer.reverse(i) ushr (32 - 11) }

        fun transform(re: DoubleArray, im: DoubleArray) {
            for (i in 0 until N) {
                val j = reversed[i]
                if (j > i) {
                    val tr = re[i]; re[i] = re[j]; re[j] = tr
                    val ti = im[i]; im[i] = im[j]; im[j] = ti
                }
            }
            var size = 2
            while (size <= N) {
                val half = size / 2
                val step = N / size
                var start = 0
                while (start < N) {
                    var k = 0
                    for (j in start until start + half) {
                        // Forward transform: e^(-2πik/N)
                        val wr = cosTable[k]
                        val wi = -sinTable[k]
                        val l = j + half
                        val tr = re[l] * wr - im[l] * wi
                        val ti = re[l] * wi + im[l] * wr
                        re[l] = re[j] - tr; im[l] = im[j] - ti
                        re[j] += tr; im[j] += ti
                        k += step
                    }
                    start += size
                }
                size *= 2
            }
        }
    }
}

/** Hanning window over 2048 samples, matching SongRec's table (leading and trailing zeroes omitted). */
private val HANNING = FloatArray(2048) { i -> (0.5 * (1 - cos(2 * PI * (i + 1) / 2049))).toFloat() }
