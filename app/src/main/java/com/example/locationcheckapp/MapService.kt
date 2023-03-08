package com.example.locationcheckapp

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.locationcheckapp.MapsActivity.Companion.KEY_ACTION_TO_ACTIVITY
import com.example.locationcheckapp.MapsActivity.Companion.SEND_LATITUDE_TO_ACTIVITY
import com.example.locationcheckapp.MapsActivity.Companion.SEND_LONGITUDE_TO_ACTIVITY
import com.google.android.gms.location.*

class MapService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 60000
            fastestInterval = 60000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.lastLocation?.let { location ->
                    setLocation(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
        return START_STICKY
    }

    private fun setLocation(location: android.location.Location) {
        val intent = Intent(KEY_ACTION_TO_ACTIVITY).apply {
            putExtra(SEND_LATITUDE_TO_ACTIVITY, location.latitude)
            putExtra(SEND_LONGITUDE_TO_ACTIVITY, location.longitude)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}