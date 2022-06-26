package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.helper.ApiConfig.Companion.getAddress
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.GPSTracker
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMarkerDragListener, OnMapLongClickListener {
    lateinit var root: View
    private lateinit var tvLocation: TextView
    private lateinit var fabSatellite: FloatingActionButton
    private lateinit var fabStreet: FloatingActionButton
    private lateinit var fabCurrent: FloatingActionButton
    private var mapType = GoogleMap.MAP_TYPE_NORMAL
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var btnUpdateLocation: Button
    private lateinit var mapReadyCallback: OnMapReadyCallback
    lateinit var from: String
    lateinit var activity: Activity
    private lateinit var googleApiClient: GoogleApiClient
    private var longitude = 0.00
    private var latitude = 0.00
    private lateinit var mMap: GoogleMap
    private lateinit var gpsTracker: GPSTracker
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_map, container, false)
        activity = requireActivity()
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        Objects.requireNonNull(mapFragment).getMapAsync(this)
        btnUpdateLocation = root.findViewById(R.id.btnUpdateLocation)
        tvLocation = root.findViewById(R.id.tvLocation)
        fabSatellite = root.findViewById(R.id.fabSatellite)
        fabCurrent = root.findViewById(R.id.fabCurrent)
        fabStreet = root.findViewById(R.id.fabStreet)
        setHasOptionsMenu(true)
        from = requireArguments().getString(Constant.FROM).toString()
        if (from.equals("update", ignoreCase = true)) {
            assert(arguments != null)
            latitude = arguments?.getDouble("latitude")!!
            longitude = arguments?.getDouble("longitude")!!
        }
        if (latitude == 0.00 || longitude == 0.00) {
            gpsTracker = GPSTracker(activity)
            latitude = gpsTracker.getLatitude()
            longitude = gpsTracker.getLongitude()
        }
        btnUpdateLocation.setOnClickListener {
            AddressAddUpdateFragment.address1.longitude = "" + longitude
            AddressAddUpdateFragment.address1.latitude = "" + latitude
            AddressAddUpdateFragment.tvCurrent.text = getAddress(
                latitude,
                longitude,
                activity
            )
            AddressAddUpdateFragment.mapFragment.getMapAsync(AddressAddUpdateFragment.mapReadyCallback)
            MainActivity.fm.popBackStack()
        }
        mapReadyCallback = OnMapReadyCallback { googleMap: GoogleMap ->
            googleMap.clear()
            val latLng = LatLng(latitude, longitude)
            googleMap.mapType = mapType
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(getString(R.string.current_location))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
        }
        googleApiClient = GoogleApiClient.Builder(activity)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        fabSatellite.setOnClickListener {
            mapType = GoogleMap.MAP_TYPE_HYBRID
            mapFragment.getMapAsync(mapReadyCallback)
        }
        fabStreet.setOnClickListener {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            mapFragment.getMapAsync(mapReadyCallback)
        }
        fabCurrent.setOnClickListener { setCurrentLocation() }
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mapFragment.getMapAsync(mapReadyCallback)
        return root
    }

    @SuppressLint("SetTextI18n")
    fun setCurrentLocation() {
        // mapType = GoogleMap.MAP_TYPE_NORMAL;
        gpsTracker = GPSTracker(activity)
        latitude = gpsTracker.getLatitude()
        longitude = gpsTracker.getLongitude()
        val latLng = LatLng(latitude, longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(getString(R.string.current_location))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))

        //tvLocation.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
        tvLocation.text = getString(R.string.location_1) + getAddress(
            latitude,
            longitude,
            activity
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
        val latLng = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(latLng).draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.mapType = mapType
        mMap.setOnMarkerDragListener(this)
        mMap.setOnMapLongClickListener(this)
        mMap.setOnMapClickListener { latLng1: LatLng ->
            latitude = latLng1.latitude
            longitude = latLng1.longitude
            //Moving the map
            mMap.clear()
            moveMap(true)
        }
        // text.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
        tvLocation.text =
            getString(R.string.location_1) + getAddress(latitude, longitude, activity)
    }

    @SuppressLint("SetTextI18n")
    private fun moveMap(isFirst: Boolean) {
        val latLng = LatLng(latitude, longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.set_location))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        if (isFirst) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
        }
        tvLocation.text = getString(R.string.location_1) + getAddress(
            latitude,
            longitude,
            activity
        )
        //  text.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
    }

    override fun onConnected(p0: Bundle?) {}

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onMapLongClick(latLng: LatLng) {
        mMap.clear()
        latitude = latLng.latitude
        longitude = latLng.longitude
        //Moving the map
        moveMap(false)
    }

    override fun onMarkerDragStart(marker: Marker) {}
    override fun onMarkerDrag(marker: Marker) {}
    override fun onMarkerDragEnd(marker: Marker) {
        latitude = marker.position.latitude
        longitude = marker.position.longitude
        moveMap(false)
    }

    override fun onStart() {
        googleApiClient.connect()
        super.onStart()
    }

    override fun onStop() {
        googleApiClient.disconnect()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapFragment.getMapAsync(mapReadyCallback)
        Constant.TOOLBAR_TITLE = getString(R.string.app_name)
        activity.invalidateOptionsMenu()
        hideKeyboard()
    }

    fun hideKeyboard() {
        try {
            val inputMethodManager =
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputMethodManager.hideSoftInputFromWindow(root.applicationWindowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_logout).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_layout).isVisible = false
    }
}