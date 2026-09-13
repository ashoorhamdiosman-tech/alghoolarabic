package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onLocationReceived: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("خدمة تحديد الموقع غير متوفرة بالجهاز")
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Fallback: Default Al-Arish center coordinates
            onLocationReceived(31.1321, 33.8033)
            return
        }

        try {
            var lastKnown: Location? = null
            if (isGpsEnabled) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (lastKnown == null && isNetworkEnabled) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            if (lastKnown != null) {
                onLocationReceived(lastKnown.latitude, lastKnown.longitude)
                return
            }

            // Single update listener
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationReceived(location.latitude, location.longitude)
                    locationManager.removeUpdates(this)
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            locationManager.requestSingleUpdate(provider, listener, android.os.Looper.getMainLooper())
        } catch (e: SecurityException) {
            onError("يرجى منح إذن الوصول للموقع لالتقاط الإحداثيات الميدانية")
        } catch (e: Exception) {
            // Fallback to Al-Arish center
            onLocationReceived(31.1321, 33.8033)
        }
    }
}
