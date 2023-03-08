package com.example.locationcheckapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.locationcheckapp.databinding.ActivityMapsBinding
import com.example.locationcheckapp.viewModel.LocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: LocationViewModel
    private var firstMap = true
    private var polyline: Polyline? = null
    private var latidute: Double? = null
    private var longidute: Double? = null

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.apply {
                latidute = getDoubleExtra(SEND_LATITUDE_TO_ACTIVITY, 0.001)
                longidute = getDoubleExtra(SEND_LONGITUDE_TO_ACTIVITY, 0.001)
                if (latidute != 0.001 && longidute != 0.001) {
                    val currentLocation = LatLng(latidute!!, longidute!!)
                    val markerOptions = MarkerOptions().position(currentLocation)
                        .title(getString(R.string.title_location))
                    if (firstMap) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f))
                        firstMap = false
                    }
                    mMap.clear()
                    mMap.addMarker(markerOptions)
                    polyline()
                    saveLocation()
                }
                Toast.makeText(applicationContext, "$latidute \n $longidute", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
        const val KEY_ACTION_TO_ACTIVITY = "KEY_ACTION_TO_ACTIVITY"
        const val SEND_LATITUDE_TO_ACTIVITY = "SEND_LATITUDE_TO_ACTIVITY"
        const val SEND_LONGITUDE_TO_ACTIVITY = "SEND_LONGITUDE_TO_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, IntentFilter(KEY_ACTION_TO_ACTIVITY))
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapsActivity)
    }

    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }else{
            mMap.isMyLocationEnabled = true
//            mMap.isBuildingsEnabled = true
//            mMap.isTrafficEnabled =true
//            mMap.isIndoorEnabled = true
            startMapService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocationUser()
                }
            }
        }
    }

    private fun startMapService() {
        startService(Intent(this, MapService::class.java))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val uiSettings = googleMap.uiSettings
        uiSettings.isMyLocationButtonEnabled = true
        getCurrentLocationUser()

    }

    private fun saveLocation() {
        viewModel.insert(com.example.locationcheckapp.local.Location(null,
            latidute,
            longidute,
            timestamp()))
    }

    private fun polyline() {
        viewModel.readAllData.observe(this) {
            viewModel.deleteAfterLast10Records()
            if (polyline != null) polyline!!.remove()
            val location = PolylineOptions().apply {
                it.forEach { location ->
                    add(LatLng(location.latitude!!, location.longitude!!))
                }
                color(R.color.black)
                width(20F)
            }
            polyline = mMap.addPolyline(location)
        }
    }

    fun timestamp(): Long {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentTimestamp = dateFormat.format(currentDate)
        return dateFormat.parse(currentTimestamp)?.time ?: 0L
    }

    override fun onDestroy() {
        stopService(Intent(this, MapService::class.java))
        super.onDestroy()
    }

}