package it.am.gpsmodule.utils

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import it.am.gpsmodule.map.AskiMap
class LocationGpsManager {
    //   public lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    companion object{
        var locationManager:LocationManager? = null

        var currentPosition: Point? = null
        fun getLastKnownLocation(activity:Activity,context: Context): Point? {
            Log.e("POSIZIONE:","USO LASTKNOW")
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    val latitude = lastKnownLocation.latitude
                    val longitude = lastKnownLocation.longitude
                    currentPosition = Point.fromLngLat(longitude, latitude)
                }
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
            return currentPosition
        }

        fun getLocation(activity:Activity,context: Context){
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val providers = listOf(
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.GPS_PROVIDER
                )

                locationManager!!.requestLocationUpdates(
                    providers.get(1),
                    0,
                    0f,
                    locationListener
                )
                locationManager!!.requestLocationUpdates(
                    providers.get(0),
                    0,
                    0f,
                    locationListener
                )



            }else{
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }


        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                // La posizione è stata aggiornata
                val latitude = location.latitude
                val longitude = location.longitude
                currentPosition = Point.fromLngLat(longitude,latitude)
                // Usa le coordinate di latitudine e longitudine come necessario
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }


            override fun onProviderEnabled(provider: String) {
                // Il provider di posizione è stato abilitato

            }

            override fun onProviderDisabled(provider: String) {
                // Il provider di posizione è stato disabilitato
                Log.e("LO USO:","")

                val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                Log.e("PROVIDER:",isNetworkEnabled.toString())
                if (!isNetworkEnabled && !isGPSEnabled) {
                    Log.e("ENTRAMBI","DISABILITATI")
                    getLastKnownLocation(AskiMap.instances()!!,AskiMap.instances()!!.applicationContext)
                }


            }
        }

    }



    public fun requestPermissions(context: Context) {
        val locationPermissionRequest = AskiMap.instances()?.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    false
                ) -> {
                    // Only approximate location access granted.
                }
                else -> {
                    Toast.makeText(context,"Ti consigliamo di acconsentire i permessi della posizione!",Toast.LENGTH_SHORT).show()
                }
            }
        }
        locationPermissionRequest?.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

    }


    public fun getLocation(context: Context, locationManager: LocationManager,ma: AskiMap): Location? {

        if ((ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(ma, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)


    }
    private fun checkPermissions(ma: AskiMap): Boolean {
        if (ActivityCompat.checkSelfPermission(
                ma,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                ma,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }






    fun setPoint(currentPos:Point){
        currentPosition = currentPos
    }
    public fun getPoint() : Point? {
        if(currentPosition != null){
            return currentPosition
        }
        return null
    }




    fun locationEnabled(locationManager: LocationManager): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }




}

