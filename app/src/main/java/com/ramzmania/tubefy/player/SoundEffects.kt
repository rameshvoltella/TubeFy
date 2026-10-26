package com.ramzmania.tubefy.player

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.ln

/**
 * Sound controls: an equalizer with presets, bass boost, virtualizer, loudness boost and reverb, applied to
 * the player's audio session (music, videos, downloads and Android Auto all play through it). The service
 * [attach]es the session; the UI changes [settings], which are remembered.
 */
object SoundEffects {

    data class Settings(
        val eqEnabled: Boolean = false,
        val preset: String = PRESET_CUSTOM,
        /** dB for each of [BAND_POINTS_HZ] in Custom. */
        val customDb: List<Float> = List(BAND_POINTS_HZ.size) { 0f },
        val bassEnabled: Boolean = false,
        val bass: Float = 0.7f,
        val virtualizerEnabled: Boolean = false,
        val virtualizer: Float = 0.7f,
        val loudnessEnabled: Boolean = false,
        val loudness: Float = 0.7f,
        val reverb: String = REVERB_NONE
    ) {
        /** The curve playing now, in dB per [BAND_POINTS_HZ]. */
        val curveDb: List<Float> get() = PRESETS[preset] ?: customDb

        /** On but every band at 0 dB: sounds the same as off. */
        val isFlat: Boolean get() = curveDb.all { it == 0f }
    }

    /** What this phone's equalizer offers (known once the player has an audio session). */
    data class Capabilities(
        val bandsHz: List<Int> = BAND_POINTS_HZ.toList(),
        val maxDb: Float = 15f,
        val virtualizer: Boolean = true,
        val reverb: Boolean = true
    )

    // The design's five bands; phones with other bands get the curve at their own frequencies
    val BAND_POINTS_HZ = intArrayOf(60, 230, 910, 3_600, 14_000)

    const val PRESET_CUSTOM = "custom"
    val PRESETS: Map<String, List<Float>> = linkedMapOf(
        "pop" to listOf(-2f, 3f, 6f, 3f, -1f),
        "rock" to listOf(7f, 3f, -3f, 4f, 7f),
        "vocal" to listOf(-3f, 0f, 6f, 6f, 2f),
        "bass" to listOf(10f, 6f, 0f, 0f, 0f),
        "jazz" to listOf(4f, 2f, -2f, 3f, 6f),
        "classical" to listOf(6f, 3f, -2f, 3f, 6f),
        "electronic" to listOf(7f, 4f, 0f, 3f, 6f)
    )

    const val REVERB_NONE = "none"
    private val REVERBS = mapOf(
        "small_room" to PresetReverb.PRESET_SMALLROOM,
        "hall" to PresetReverb.PRESET_MEDIUMHALL,
        "concert" to PresetReverb.PRESET_LARGEHALL
    )

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()
    private val _capabilities = MutableStateFlow(Capabilities())
    val capabilities: StateFlow<Capabilities> = _capabilities.asStateFlow()

    private var prefs: android.content.SharedPreferences? = null
    private var sessionId = 0
    private var setAuxEffect: ((effectId: Int, level: Float) -> Unit)? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null
    private var reverb: PresetReverb? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences("sound_controls", Context.MODE_PRIVATE).also { p ->
            _settings.value = Settings(
                eqEnabled = p.getBoolean("eq", false),
                preset = p.getString("preset", PRESET_CUSTOM) ?: PRESET_CUSTOM,
                customDb = p.getString("custom", null)?.split(",")?.mapNotNull { it.toFloatOrNull() }
                    ?.takeIf { it.size == BAND_POINTS_HZ.size } ?: List(BAND_POINTS_HZ.size) { 0f },
                bassEnabled = p.getBoolean("bass_on", false),
                bass = p.getFloat("bass", 0.5f),
                virtualizerEnabled = p.getBoolean("virt_on", false),
                virtualizer = p.getFloat("virt", 0.5f),
                loudnessEnabled = p.getBoolean("loud_on", false),
                loudness = p.getFloat("loud", 0.5f),
                reverb = p.getString("reverb", REVERB_NONE) ?: REVERB_NONE
            )
        }
    }

    /** Attaches the effects to the player's audio session (again whenever it changes). */
    fun attach(audioSessionId: Int, auxEffect: (effectId: Int, level: Float) -> Unit) {
        if (audioSessionId == 0 || audioSessionId == sessionId) return
        release()
        sessionId = audioSessionId
        setAuxEffect = auxEffect
        equalizer = create { Equalizer(0, audioSessionId) }
        bassBoost = create { BassBoost(0, audioSessionId) }
        virtualizer = create { Virtualizer(0, audioSessionId) }
        loudness = create { LoudnessEnhancer(audioSessionId) }
        // Reverb is an auxiliary effect on the output mix; many phones don't support it
        reverb = create { PresetReverb(0, 0) }
        equalizer?.let { eq ->
            val range = eq.bandLevelRange
            _capabilities.value = Capabilities(
                bandsHz = (0 until eq.numberOfBands).map { eq.getCenterFreq(it.toShort()) / 1000 },
                maxDb = minOf(abs(range[0].toInt()), abs(range[1].toInt())) / 100f,
                virtualizer = virtualizer?.strengthSupported == true,
                reverb = reverb != null
            )
        }
        apply(_settings.value)
    }

    fun release() {
        listOf(equalizer, bassBoost, virtualizer, loudness, reverb).forEach { runCatching { it?.release() } }
        equalizer = null; bassBoost = null; virtualizer = null; loudness = null; reverb = null
        sessionId = 0
    }

    fun update(change: (Settings) -> Settings) {
        val next = change(_settings.value)
        _settings.value = next
        apply(next)
        prefs?.edit()?.apply {
            putBoolean("eq", next.eqEnabled)
            putString("preset", next.preset)
            putString("custom", next.customDb.joinToString(","))
            putBoolean("bass_on", next.bassEnabled)
            putFloat("bass", next.bass)
            putBoolean("virt_on", next.virtualizerEnabled)
            putFloat("virt", next.virtualizer)
            putBoolean("loud_on", next.loudnessEnabled)
            putFloat("loud", next.loudness)
            putString("reverb", next.reverb)
        }?.apply()
    }

    /** Dragging a band: the curve becomes Custom, starting from the preset that was playing. */
    fun setBand(index: Int, db: Float) = update { s ->
        val curve = s.curveDb.toMutableList().apply { this[index] = db }
        s.copy(eqEnabled = true, preset = PRESET_CUSTOM, customDb = curve)
    }

    fun reset() = update { Settings() }

    /** Turning the equalizer on with a flat Custom curve picks Bass Boost, so the switch is always heard. */
    fun setEqEnabled(on: Boolean) = update { s ->
        if (on && s.preset == PRESET_CUSTOM && s.customDb.all { it == 0f }) s.copy(eqEnabled = true, preset = "bass")
        else s.copy(eqEnabled = on)
    }

    private fun apply(s: Settings) {
        runCatching {
            equalizer?.let { eq ->
                val max = (_capabilities.value.maxDb * 100).toInt()
                for (band in 0 until eq.numberOfBands) {
                    val hz = eq.getCenterFreq(band.toShort()) / 1000
                    val mB = (curveAt(s.curveDb, hz) * 100).toInt().coerceIn(-max, max)
                    eq.setBandLevel(band.toShort(), mB.toShort())
                }
                eq.enabled = s.eqEnabled
            }
            bassBoost?.let {
                if (it.strengthSupported) it.setStrength((s.bass * 1000).toInt().toShort())
                it.enabled = s.bassEnabled
            }
            virtualizer?.let {
                if (it.strengthSupported) it.setStrength((s.virtualizer * 1000).toInt().toShort())
                it.enabled = s.virtualizerEnabled
            }
            loudness?.let {
                // Up to +10 dB
                it.setTargetGain((s.loudness * 1000).toInt())
                it.enabled = s.loudnessEnabled
            }
            reverb?.let { r ->
                val preset = REVERBS[s.reverb]
                if (preset != null) r.preset = preset
                r.enabled = preset != null
                setAuxEffect?.invoke(r.id, if (preset != null) 1f else 0f)
            }
        }.onFailure { Log.w("SoundEffects", "Couldn't apply sound settings", it) }
    }

    /** The curve's dB at [hz], interpolated on a log-frequency scale between [BAND_POINTS_HZ]. */
    private fun curveAt(curve: List<Float>, hz: Int): Float {
        val x = ln(hz.coerceAtLeast(1).toFloat())
        val points = BAND_POINTS_HZ.map { ln(it.toFloat()) }
        if (x <= points.first()) return curve.first()
        if (x >= points.last()) return curve.last()
        val i = points.indexOfLast { it <= x }
        val t = (x - points[i]) / (points[i + 1] - points[i])
        return curve[i] + (curve[i + 1] - curve[i]) * t
    }

    private fun <T> create(block: () -> T): T? = try {
        block()
    } catch (e: Throwable) {
        Log.w("SoundEffects", "Effect not available on this phone", e)
        null
    }
}
