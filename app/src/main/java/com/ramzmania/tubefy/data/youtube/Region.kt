package com.ramzmania.tubefy.data.youtube

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** The user's state (e.g. "Kerala") and country code (e.g. "IN") for the YouTube tab. */
data class Region(val state: String?, val country: String)

/** Indian states and union territories → the language most of their YouTube is in. */
object StateLanguages {
    val INDIA: Map<String, String> = linkedMapOf(
        "Andhra Pradesh" to "Telugu",
        "Arunachal Pradesh" to "Hindi",
        "Assam" to "Assamese",
        "Bihar" to "Bhojpuri",
        "Chhattisgarh" to "Chhattisgarhi",
        "Delhi" to "Hindi",
        "Goa" to "Konkani",
        "Gujarat" to "Gujarati",
        "Haryana" to "Haryanvi",
        "Himachal Pradesh" to "Himachali",
        "Jammu and Kashmir" to "Kashmiri",
        "Jharkhand" to "Hindi",
        "Karnataka" to "Kannada",
        "Kerala" to "Malayalam",
        "Ladakh" to "Hindi",
        "Madhya Pradesh" to "Hindi",
        "Maharashtra" to "Marathi",
        "Manipur" to "Manipuri",
        "Meghalaya" to "Khasi",
        "Mizoram" to "Mizo",
        "Nagaland" to "Nagamese",
        "Odisha" to "Odia",
        "Puducherry" to "Tamil",
        "Punjab" to "Punjabi",
        "Rajasthan" to "Rajasthani",
        "Sikkim" to "Nepali",
        "Tamil Nadu" to "Tamil",
        "Telangana" to "Telugu",
        "Tripura" to "Bengali",
        "Uttar Pradesh" to "Hindi",
        "Uttarakhand" to "Garhwali",
        "West Bengal" to "Bengali"
    )

    /** The language for [state], matching loosely ("Kerala", "kerala", "State of Kerala"); null if unknown. */
    fun languageOf(state: String?): String? {
        val name = state?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        INDIA.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.let { return it.value }
        return INDIA.entries.firstOrNull { name.contains(it.key, ignoreCase = true) }?.value
    }

    /** The canonical state name for what the geocoder returned, or null if it's not a known state. */
    fun canonical(state: String?): String? {
        val name = state?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return INDIA.keys.firstOrNull { it.equals(name, ignoreCase = true) }
            ?: INDIA.keys.firstOrNull { name.contains(it, ignoreCase = true) }
    }
}

/**
 * Remembers the user's region. The country comes from the SIM / network (no permission); the state is
 * detected once from an approximate location (only after the user allows it) or picked from a list.
 */
@Singleton
class RegionStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs = context.getSharedPreferences("youtube_region", Context.MODE_PRIVATE)
    private val _region = MutableStateFlow(Region(prefs.getString(KEY_STATE, null), country()))
    val region: StateFlow<Region> = _region.asStateFlow()

    /** True once the user dismissed the "trending in your state" card. */
    var promptDismissed: Boolean
        get() = prefs.getBoolean(KEY_DISMISSED, false)
        set(value) = prefs.edit().putBoolean(KEY_DISMISSED, value).apply()

    fun setState(state: String?) {
        prefs.edit().putString(KEY_STATE, state).apply()
        _region.value = _region.value.copy(state = state)
    }

    /**
     * Finds the state from an approximate location; null when there's no location or the geocoder
     * can't tell. The caller must hold ACCESS_COARSE_LOCATION.
     */
    suspend fun detectState(): String? = withContext(Dispatchers.IO) {
        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) { currentLocation() } ?: lastKnownLocation()
            ?: return@withContext null
        val adminArea = withTimeoutOrNull(LOCATION_TIMEOUT_MS) { adminArea(location) } ?: return@withContext null
        (StateLanguages.canonical(adminArea) ?: adminArea).also(::setState)
    }

    private fun country(): String {
        val telephony = context.getSystemService(TelephonyManager::class.java)
        val fromNetwork = runCatching { telephony?.networkCountryIso }.getOrNull()
        val fromSim = runCatching { telephony?.simCountryIso }.getOrNull()
        return listOf(fromNetwork, fromSim, Locale.getDefault().country)
            .firstOrNull { !it.isNullOrBlank() && it.length == 2 }
            ?.uppercase(Locale.ROOT) ?: "IN"
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): Location? {
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val provider = listOf(LocationManager.NETWORK_PROVIDER, "fused", LocationManager.GPS_PROVIDER)
            .firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) } ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return suspendCancellableCoroutine { continuation ->
            val cancel = CancellationSignal()
            continuation.invokeOnCancellation { cancel.cancel() }
            runCatching {
                manager.getCurrentLocation(provider, cancel, context.mainExecutor) { continuation.resume(it) }
            }.onFailure { if (continuation.isActive) continuation.resume(null) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): Location? {
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        return runCatching { manager.getProviders(true) }.getOrDefault(emptyList())
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @Suppress("DEPRECATION")
    private suspend fun adminArea(location: Location): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.ENGLISH)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(location.latitude, location.longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        if (continuation.isActive) continuation.resume(addresses.firstOrNull()?.adminArea)
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                })
            }
        } else {
            runCatching { geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()?.adminArea }.getOrNull()
        }
    }

    private companion object {
        const val KEY_STATE = "state"
        const val KEY_DISMISSED = "prompt_dismissed"
        const val LOCATION_TIMEOUT_MS = 10_000L
    }
}
