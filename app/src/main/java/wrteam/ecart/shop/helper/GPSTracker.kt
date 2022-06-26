package wrteam.ecart.shop.helper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

open class GPSTracker(private val mContext: Context) : LocationListener {
    // flag for GPS status
    private var isGPSEnabled = false

    // flag for network status
    private var isNetworkEnabled = false

    // flag for GPS status
    private var canGetLocation = false
    lateinit var location : Location
    private var latitude  = 0.0
    private var longitude  = 0.0

    // Declaring a Location Manager
    private lateinit var locationManager: LocationManager

    /**
     * Function to get the user's current location
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        try {
            locationManager = mContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // getting GPS status
            isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
            Log.v("isGPSEnabled", "=$isGPSEnabled")

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.v("isNetworkEnabled", "=$isNetworkEnabled")
            if (isGPSEnabled && isNetworkEnabled) {
                canGetLocation = true
                if (ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                )
                Log.d("Network", "Network")
                location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
                latitude = location.latitude
                longitude = location.longitude
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    Log.d("GPS Enabled", "GPS Enabled")
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Function to get latitude
     */
    @JvmName("getLatitude1")
    fun getLatitude(): Double {
        latitude = location.latitude

        // return latitude
        return latitude
    }

    /**
     * Function to get longitude
     */
    @JvmName("getLongitude1")
    fun getLongitude(): Double {
        longitude = location.longitude

        // return longitude
        return longitude
    }

    override fun onLocationChanged(location: Location) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    companion object {
        // The minimum distance to change Updates in meters
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 1 // 10 meters

        // The minimum time between updates in milliseconds
        private const val MIN_TIME_BW_UPDATES: Long = 1 // 1 minute
    }

    init {
        getLocation()
    }
}