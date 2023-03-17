
package com.example.testopenstreetmap

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.testopenstreetmap.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.PathOverlay
import org.osmdroid.views.overlay.Polyline
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var longitudeMain by Delegates.notNull<Double>()
    private var latitudeMain by Delegates.notNull<Double>()
    private var longitudeMarker by Delegates.notNull<Double>()
    private var latitudeMarker by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lineButton.setOnClickListener{
            val mainGPT = GeoPoint(latitudeMain, longitudeMain)
            val markerGPT = GeoPoint(latitudeMarker,longitudeMarker)
            val line = Polyline(binding.map)
            line.addPoint(mainGPT)
            line.addPoint(markerGPT)
            line.color = Color.GREEN
            binding.map.overlays.add(line)
            binding.map.invalidate()
        }
        binding.centrButton.setOnClickListener {
            latitudeMain = latitudeMarker
            longitudeMain = longitudeMarker
            binding.map.overlays.forEach {
                if (it is Marker && it.id == "Marker1") {
                    binding.map.overlays.remove(it)
                }
            }
            binding.map.overlays.forEach {
                if (it is Marker && it.id == "PizzaSushiWok") {
                    binding.map.overlays.remove(it)
                    val geoPoint = GeoPoint(latitudeMain,longitudeMain)
                    binding.map.controller.setCenter(geoPoint)
                    val marker = Marker(binding.map)
                    marker.position = geoPoint
                    marker.icon = ContextCompat.getDrawable(binding.root.context,R.drawable.logo)
                    marker.title = "PizzaSushiWok"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    binding.map.overlays.add(marker)
                    binding.map.invalidate()
                }
            }
        }
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (2 * 1000).toLong()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        latitudeMain = location.latitude
                        longitudeMain = location.longitude
                        val starterPoint = GeoPoint(latitudeMain,longitudeMain)
                        binding.map.controller.setCenter(starterPoint)
                    }
                }
            }
        }
        getLocation()
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.controller.setZoom(15.5)
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                Log.d("singleTapConfirmedHelper", "${p.latitude} - ${p.longitude}")
                latitudeMarker = p.latitude
                longitudeMarker = p.longitude
                binding.map.overlays.forEach {
                    if (it is Marker && it.id == "Marker1") {
                        binding.map.overlays.remove(it)
                    }
                }
                val gPt = GeoPoint(p.latitude, p.longitude)
                val marker = Marker(binding.map)
                marker.position = gPt
                marker.id = "Marker1"
                marker.icon = ContextCompat.getDrawable(binding.root.context,R.drawable.ic_baseline_check_24)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                binding.map.overlays.add(marker)
                binding.map.invalidate()
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                Log.d("longPressHelper", "${p.latitude} - ${p.longitude}")
                return false
            }
        }
        binding.map.overlays.add(MapEventsOverlay(mReceive))
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            val permissions = arrayOf(ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions,101)
            return
        }
        else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if ( it!= null){
                    latitudeMain = it.latitude
                    longitudeMain = it.longitude
                    val geoPoint = GeoPoint(latitudeMain,longitudeMain)
                    binding.map.controller.setCenter(geoPoint)
                    val marker = Marker(binding.map)
                    marker.position = geoPoint
                    marker.icon = ContextCompat.getDrawable(binding.root.context,R.drawable.logo)
                    marker.title = "PizzaSushiWok"
                    marker.id = "PizzaSushiWok"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    binding.map.overlays.add(marker)
                    binding.map.invalidate()
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
                val geoPoint = GeoPoint(latitudeMain,longitudeMain)
                val marker = Marker(binding.map)
                marker.position = geoPoint
                marker.icon = ContextCompat.getDrawable(binding.root.context,R.drawable.logo)
                marker.title = "PizzaSushiWok"
                marker.id = "PizzaSushiWok"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                binding.map.overlays.add(marker)
                binding.map.invalidate()
            } else {
                //user denied the permission
            }
        }
    }

}