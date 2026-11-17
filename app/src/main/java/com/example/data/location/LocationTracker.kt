package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.example.data.model.GeorgianTerritories
import com.example.data.model.RegionGroup
import com.example.data.model.Territory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class LocationTracker(private val context: Context) {

  private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(context) }

  @SuppressLint("MissingPermission")
  suspend fun getCurrentTerritory(): Territory? {
    val location = fetchCoarseLocation() ?: return null
    val (name, region) = resolveLocationName(location.latitude, location.longitude)
    val nearest = findNearestGeorgianTerritory(location.latitude, location.longitude)
    return Territory(
      id = "my_location",
      nameKa = name,
      regionKa = region,
      primaryGroup = nearest?.primaryGroup ?: RegionGroup.ALL,
      tags = nearest?.tags ?: emptySet(),
      lat = location.latitude,
      lon = location.longitude,
      mapX = nearest?.mapX ?: 0.5f,
      mapY = nearest?.mapY ?: 0.5f,
      elevationM = nearest?.elevationM ?: 0,
      isCurrentLocation = true,
    )
  }

  @SuppressLint("MissingPermission")
  private suspend fun fetchCoarseLocation(): Location? {
    val fusedCurrent =
      withTimeoutOrNull(4500L) {
        suspendCancellableCoroutine { cont ->
          val cts = CancellationTokenSource()
          cont.invokeOnCancellation { cts.cancel() }
          fusedClient
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
              if (cont.isActive) cont.resume(loc)
            }
            .addOnFailureListener {
              if (cont.isActive) cont.resume(null)
            }
        }
      }
    if (fusedCurrent != null) return fusedCurrent

    val fusedLast =
      withTimeoutOrNull(2000L) {
        suspendCancellableCoroutine { cont ->
          fusedClient.lastLocation
            .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
            .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }
      }
    if (fusedLast != null) return fusedLast

    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val lastKnown =
      try {
        lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
          ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
      } catch (_: SecurityException) {
        null
      }
    if (lastKnown != null) return lastKnown

    val provider =
      when {
        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        else -> null
      } ?: return null

    return withTimeoutOrNull(4000L) {
      suspendCancellableCoroutine { cont ->
        val cancelSignal = CancellationSignal()
        cont.invokeOnCancellation { cancelSignal.cancel() }
        try {
          LocationManagerCompat.getCurrentLocation(
            lm,
            provider,
            cancelSignal,
            { runnable -> android.os.Handler(Looper.getMainLooper()).post(runnable) },
          ) { location ->
            if (cont.isActive) cont.resume(location)
          }
        } catch (_: SecurityException) {
          if (cont.isActive) cont.resume(null)
        }
      }
    }
  }

  private suspend fun resolveLocationName(lat: Double, lon: Double): Pair<String, String> =
    withContext(Dispatchers.IO) {
      try {
        if (Geocoder.isPresent()) {
          val geocoder = Geocoder(context, Locale("ka", "GE"))
          val address =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              withTimeoutOrNull(2500L) {
                suspendCancellableCoroutine { cont ->
                  geocoder.getFromLocation(lat, lon, 1) { list ->
                    if (cont.isActive) cont.resume(list.firstOrNull())
                  }
                }
              }
            } else {
              @Suppress("DEPRECATION")
              geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
            }
          if (address != null) {
            val locality =
              address.locality ?: address.subAdminArea ?: address.adminArea ?: "ჩემი ტერიტორია"
            val admin = address.adminArea ?: address.countryName ?: "მიმდინარე მდებარეობა"
            return@withContext locality to admin
          }
        }
      } catch (_: Exception) {
        // Fallback below
      }
      val nearest = findNearestGeorgianTerritory(lat, lon)
      if (nearest != null) {
        "${nearest.nameKa} (GPS)" to nearest.regionKa
      } else {
        "ჩემი მდებარეობა" to "%.2f°, %.2f°".format(Locale.US, lat, lon)
      }
    }

  private fun findNearestGeorgianTerritory(lat: Double, lon: Double): Territory? {
    val nearest =
      GeorgianTerritories.defaults.minByOrNull { territory ->
        haversineKm(lat, lon, territory.lat, territory.lon)
      } ?: return null
    val dist = haversineKm(lat, lon, nearest.lat, nearest.lon)
    return if (dist <= 220.0) nearest else null
  }

  private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a =
      sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
  }
}
