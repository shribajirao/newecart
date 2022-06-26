package wrteam.ecart.shop.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.helper.ApiConfig.Companion.getAddress
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Address
import wrteam.ecart.shop.model.City

@SuppressLint("NotifyDataSetChanged")
class AddressAddUpdateFragment : Fragment(), OnMapReadyCallback {
    lateinit var root: View
    private lateinit var cityArrayList: ArrayList<City?>
    lateinit var areaArrayList: ArrayList<City?>
    private lateinit var btnSubmit: Button
    lateinit var progressBar: ProgressBar
    private lateinit var chIsDefault: CheckBox
    private lateinit var rdHome: RadioButton
    private lateinit var rdOffice: RadioButton
    private lateinit var rdOther: RadioButton
    lateinit var session: Session
    private lateinit var tvUpdate: TextView
    private lateinit var edtName: TextView
    private lateinit var edtMobile: TextView
    private lateinit var edtAlternateMobile: TextView
    private lateinit var edtAddress: TextView
    private lateinit var edtLandmark: TextView
    private lateinit var edtPinCode: TextView
    private lateinit var edtState: TextView
    private lateinit var edtCounty: TextView
    lateinit var scrollView: ScrollView
    lateinit var name: String
    lateinit var mobile: String
    private lateinit var alternateMobile: String
    private lateinit var address2: String
    private lateinit var landmark: String
    lateinit var pinCode: String
    lateinit var state: String
    private lateinit var country: String
    private lateinit var addressType: String
    lateinit var activity: Activity
    lateinit var addressFor: String
    lateinit var cityAdapter: CityAdapter
    lateinit var areaAdapter: AreaAdapter
    var total = 0
    private var isDefault = "0"
    var position = 0
    var isLoadMore = false
    var offset = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_address_add_update, container, false)
        activity = requireActivity()
        setHasOptionsMenu(true)
        edtCity = root.findViewById(R.id.edtCity)
        edtArea = root.findViewById(R.id.edtArea)
        edtName = root.findViewById(R.id.edtName)
        edtMobile = root.findViewById(R.id.edtMobile)
        edtAlternateMobile = root.findViewById(R.id.edtAlternateMobile)
        edtLandmark = root.findViewById(R.id.edtLandmark)
        edtAddress = root.findViewById(R.id.edtAddress)
        edtPinCode = root.findViewById(R.id.edtPinCode)
        edtState = root.findViewById(R.id.edtState)
        edtCounty = root.findViewById(R.id.edtCountry)
        btnSubmit = root.findViewById(R.id.btnSubmit)
        scrollView = root.findViewById(R.id.scrollView)
        progressBar = root.findViewById(R.id.progressBar)
        chIsDefault = root.findViewById(R.id.chIsDefault)
        rdHome = root.findViewById(R.id.rdHome)
        rdOffice = root.findViewById(R.id.rdOffice)
        rdOther = root.findViewById(R.id.rdOther)
        tvCurrent = root.findViewById(R.id.tvCurrent)
        tvUpdate = root.findViewById(R.id.tvUpdate)
        session = Session(activity)
        edtName.text = session.getData(Constant.NAME)
        edtAddress.text = session.getData(Constant.ADDRESS)
        edtPinCode.text = session.getData(Constant.PINCODE)
        edtMobile.text = session.getData(Constant.MOBILE)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        cityArrayList = ArrayList()
        areaArrayList = ArrayList()
        val bundle = requireArguments()
        addressFor = bundle.getString("for").toString()
        position = bundle.getInt("position")
        if (addressFor == "update") {
            btnSubmit.text = getString(R.string.update)
            address1 = bundle.getSerializable("model") as Address
            cityId = address1.city_id
            areaId = address1.area_id
            edtCity.text = address1.city_name
            edtArea.text = address1.area_name
            latitude = address1.latitude.toDouble()
            longitude = address1.longitude.toDouble()
            tvCurrent.text = getString(R.string.location_1) + getAddress(
                latitude,
                longitude,
                activity
            )
            mapFragment.getMapAsync(this)
            setData()
        } else {
            edtArea.isEnabled = false
            address1 = Address()
            progressBar.visibility = View.VISIBLE
            scrollView.visibility = View.VISIBLE
            btnSubmit.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
        mapReadyCallback = OnMapReadyCallback { googleMap: GoogleMap ->
            val saveLatitude: Double
            val saveLongitude: Double
            if (latitude <= 0 || longitude <= 0) {
                saveLatitude = session.getCoordinates(Constant.LATITUDE).toDouble()
                saveLongitude = session.getCoordinates(Constant.LONGITUDE).toDouble()
            } else {
                saveLatitude = latitude
                saveLongitude = longitude
            }
            googleMap.clear()
            val latLng = LatLng(saveLatitude, saveLongitude)
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title(getString(R.string.current_location))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
        }
        btnSubmit.setOnClickListener { addUpdateAddress() }

        tvUpdate.setOnClickListener { displayLocationSettingsRequest(activity) }

        edtCity.setOnClickListener { openDialog(activity, "city") }

        edtArea.setOnClickListener {
            if (cityId != "0" && edtArea.isEnabled) {
                openDialog(activity, "area")
            } else {
                edtArea.requestFocus()
                Toast.makeText(activity, getString(R.string.select_city_first), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        chIsDefault.setOnClickListener {
            isDefault = if (isDefault.equals("0", ignoreCase = true)) {
                "1"
            } else {
                "0"
            }
        }
        return root
    }

    @Deprecated("")
    fun displayLocationSettingsRequest(activity: Activity) {
        val googleApiClient = GoogleApiClient.Builder(activity).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = (10000 / 2).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result: LocationSettingsResult ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> if (ContextCompat.checkSelfPermission(
                        activity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        activity, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ), 110
                    )
                } else {
                    val fragment: Fragment = MapFragment()
                    val bundle1 = Bundle()
                    bundle1.putString(Constant.FROM, "address")
                    bundle1.putDouble("latitude", latitude)
                    bundle1.putDouble("longitude", longitude)
                    fragment.arguments = bundle1
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                        .addToBackStack(null).commit()
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(activity, 110)
                } catch (e: SendIntentException) {
                    Log.i("tag", "PendingIntent unable to execute request.")
                }
            }
        }
    }

    private fun setData() {
        name = address1.name
        mobile = address1.mobile
        address2 = address1.address
        alternateMobile = address1.alternate_mobile
        landmark = address1.landmark
        pinCode = address1.pincode
        state = address1.state
        country = address1.country
        isDefault = address1.is_default
        addressType = address1.type
        progressBar.visibility = View.VISIBLE
        edtName.text = name
        edtMobile.text = mobile
        edtAlternateMobile.text = alternateMobile
        edtAddress.text = address2
        edtLandmark.text = landmark
        edtPinCode.text = pinCode
        edtState.text = state
        edtCounty.text = country
        chIsDefault.isChecked = isDefault.equals("1", ignoreCase = true)
        when {
            addressType.equals("home", ignoreCase = true) -> {
                rdHome.isChecked = true
            }
            addressType.equals("office", ignoreCase = true) -> {
                rdOffice.isChecked = true
            }
            else -> {
                rdOther.isChecked = true
            }
        }
        progressBar.visibility = View.GONE
        btnSubmit.visibility = View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addUpdateAddress() {
        val isDefault = if (chIsDefault.isChecked) "1" else "0"
        val type =
            if (rdHome.isChecked) "Home" else if (rdOffice.isChecked) "Office" else "Other"
        when {
            cityId == "0" -> {
                Toast.makeText(activity, "Please select city!", Toast.LENGTH_SHORT).show()
            }
            areaId == "0" -> {
                Toast.makeText(activity, "Please select area!", Toast.LENGTH_SHORT).show()
            }
            edtName.text.toString().isEmpty() -> {
                edtName.requestFocus()
                edtName.error = "Please enter name!"
            }
            edtMobile.text.toString().isEmpty() -> {
                edtMobile.requestFocus()
                edtMobile.error = "Please enter mobile!"
            }
            edtAddress.text.toString().isEmpty() -> {
                edtAddress.requestFocus()
                edtAddress.error = "Please enter address!"
            }
            edtLandmark.text.toString().isEmpty() -> {
                edtLandmark.requestFocus()
                edtLandmark.error = "Please enter landmark!"
            }
            edtPinCode.text.toString().isEmpty() -> {
                edtPinCode.requestFocus()
                edtPinCode.error = "Please enter pin code!"
            }
            edtState.text.toString().isEmpty() -> {
                edtState.requestFocus()
                edtState.error = "Please enter state!"
            }
            edtCounty.text.toString().isEmpty() -> {
                edtCounty.requestFocus()
                edtCounty.error = "Please enter country"
            }
            else -> {
                val params: MutableMap<String, String> = HashMap()
                if (addressFor.equals("add", ignoreCase = true)) {
                    params[Constant.ADD_ADDRESS] = Constant.GetVal
                } else if (addressFor.equals("update", ignoreCase = true)) {
                    params[Constant.UPDATE_ADDRESS] = Constant.GetVal
                    params[Constant.ID] = address1.id
                }
                if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                    session.getData(
                        Constant.ID
                    )
                params[Constant.TYPE] = type
                params[Constant.NAME] = edtName.text.toString()
                params[Constant.COUNTRY_CODE] = session.getData(Constant.COUNTRY_CODE)
                params[Constant.MOBILE] = edtMobile.text.toString()
                params[Constant.ALTERNATE_MOBILE] =
                    edtAlternateMobile.text.toString()
                params[Constant.ADDRESS] = edtAddress.text.toString()
                params[Constant.LANDMARK] = edtLandmark.text.toString()
                params[Constant.CITY_ID] = cityId
                params[Constant.AREA_ID] = areaId
                params[Constant.PINCODE] = edtPinCode.text.toString()
                params[Constant.STATE] = edtState.text.toString()
                params[Constant.COUNTRY] = edtCounty.text.toString()
                params[Constant.IS_DEFAULT] = isDefault
                params[Constant.LONGITUDE] = address1.longitude
                params[Constant.LATITUDE] = address1.latitude
                requestToVolley(object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {
                        if (result) {
                            try {
                                val jsonObject = JSONObject(response)
                                if (!jsonObject.getBoolean(Constant.ERROR)) {
                                    val address =
                                        Gson().fromJson(jsonObject.toString(), Address::class.java)
                                    if (addressFor.equals("add", ignoreCase = true)) {
                                        AddressListFragment.addressAdapter.addAddress(address)
                                    } else if (addressFor.equals("update", ignoreCase = true)) {
                                        AddressListFragment.addressAdapter.updateAddress(
                                            position,
                                            address
                                        )
                                    }
                                    if (address.is_default == "1") {
                                        for (i in 0 until AddressListFragment.addressAdapter.itemCount) {
                                            AddressListFragment.addresses[i]?.is_default = "0"
                                        }
                                        if (addressFor.equals("add", ignoreCase = true)) {
                                            AddressListFragment.addresses[0]?.is_default = "1"
                                            Constant.selectedAddressId =
                                                AddressListFragment.addresses[0]?.id.toString()
                                        } else if (addressFor.equals("update", ignoreCase = true)) {
                                            AddressListFragment.addresses[position]?.is_default = "1"
                                            Constant.selectedAddressId =
                                                AddressListFragment.addresses[position]?.id.toString()
                                                    .toLong().toString()
                                        }
                                    }
                                    MainActivity.fm.popBackStack()
                                    Toast.makeText(
                                        getActivity(),
                                        jsonObject.getString(Constant.MESSAGE),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }, activity, Constant.GET_ADDRESS_URL, params, true)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun openDialog(activity: Activity, from: String) {
        offset = 0
        val alertDialog = AlertDialog.Builder(requireContext())
        val inflater1 =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater1.inflate(R.layout.dialog_city_area_selection, null)
        alertDialog.setView(dialogView)
        alertDialog.setCancelable(true)
        val dialog = alertDialog.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val scrollView: NestedScrollView = dialogView.findViewById(R.id.scrollView)
        val tvSearch: TextView = dialogView.findViewById(R.id.tvSearch)
        val tvAlert: TextView = dialogView.findViewById(R.id.tvAlert)
        val searchView: EditText = dialogView.findViewById(R.id.searchView)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
        val shimmerFrameLayout: ShimmerFrameLayout =
            dialogView.findViewById(R.id.shimmerFrameLayout)
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()
        searchView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_search,
            0,
            R.drawable.ic_close_,
            0
        )
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (searchView.text.toString().isNotEmpty()) {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_search,
                        0,
                        R.drawable.ic_close,
                        0
                    )
                } else {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_search,
                        0,
                        R.drawable.ic_close_,
                        0
                    )
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        if (from == "city") {
            tvAlert.text = getString(R.string.no_cities_found)
            getCityData(
                "",
                recyclerView,
                tvAlert,
                linearLayoutManager,
                scrollView,
                dialog,
                shimmerFrameLayout
            )
            tvSearch.setOnClickListener {
                getCityData(
                    searchView.text.toString(),
                    recyclerView,
                    tvAlert,
                    linearLayoutManager,
                    scrollView,
                    dialog,
                    shimmerFrameLayout
                )
            }
            searchView.setOnTouchListener { _: View, event: MotionEvent ->
                val drawableRight = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    if (searchView.text.toString().isNotEmpty()) {
                        if (event.rawX >= searchView.right - searchView.compoundDrawables[drawableRight].bounds.width()) {
                            searchView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search,
                                0,
                                R.drawable.ic_close_,
                                0
                            )
                            searchView.setText("")
                            getCityData(
                                "",
                                recyclerView,
                                tvAlert,
                                linearLayoutManager,
                                scrollView,
                                dialog,
                                shimmerFrameLayout
                            )
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
        } else {
            tvAlert.text = getString(R.string.no_areas_found)
            getAreaData(
                "",
                recyclerView,
                tvAlert,
                linearLayoutManager,
                scrollView,
                dialog,
                shimmerFrameLayout
            )
            tvSearch.setOnClickListener {
                getAreaData(
                    searchView.text.toString(),
                    recyclerView,
                    tvAlert,
                    linearLayoutManager,
                    scrollView,
                    dialog,
                    shimmerFrameLayout
                )
            }
            searchView.setOnTouchListener { _: View, event: MotionEvent ->
                val drawableRight = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    if (searchView.text.toString().isNotEmpty()) {
                        if (event.rawX >= searchView.right - searchView.compoundDrawables[drawableRight].bounds.width()) {
                            searchView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search,
                                0,
                                R.drawable.ic_close_,
                                0
                            )
                            searchView.setText("")
                            getAreaData(
                                "",
                                recyclerView,
                                tvAlert,
                                linearLayoutManager,
                                scrollView,
                                dialog,
                                shimmerFrameLayout
                            )
                        }
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
        dialog.show()
    }

    private fun getCityData(
        search: String,
        recyclerView: RecyclerView,
        tvAlert: TextView,
        linearLayoutManager: LinearLayoutManager,
        scrollView: NestedScrollView,
        dialog: AlertDialog,
        shimmerFrameLayout: ShimmerFrameLayout
    ) {
        cityArrayList = ArrayList()
        progressBar.visibility = View.VISIBLE
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SEARCH] = search
        params[Constant.OFFSET] = "" + offset
        params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 20)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            try {
                                total = jsonObject.getString(Constant.TOTAL).toInt()
                                val jsonObject = JSONObject(response)
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)

                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val city =
                                        Gson().fromJson(jsonObject1.toString(), City::class.java)
                                    cityArrayList.add(city)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (offset == 0) {
                                progressBar.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                tvAlert.visibility = View.GONE
                                cityAdapter = CityAdapter(activity, cityArrayList, dialog)
                                cityAdapter.setHasStableIds(true)
                                recyclerView.adapter = cityAdapter
                                shimmerFrameLayout.visibility = View.GONE
                                shimmerFrameLayout.stopShimmer()
                                scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        if (cityArrayList.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == cityArrayList.size - 1) {
                                                    //bottom of list!

                                                    cityArrayList.add(City())
                                                    cityAdapter.notifyItemInserted(cityArrayList.size - 1)
                                                    offset += Constant.LOAD_ITEM_LIMIT + 20
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.SEARCH] = search
                                                    params1[Constant.OFFSET] = "" + offset
                                                    params1[Constant.LIMIT] =
                                                        "" + (Constant.LOAD_ITEM_LIMIT + 20)
                                                    requestToVolley(object : VolleyCallback {
                                                        override fun onSuccess(
                                                            result: Boolean,
                                                            response: String
                                                        ) {
                                                            if (result) {
                                                                try {
                                                                    val jsonObject1 =
                                                                        JSONObject(response)
                                                                    if (!jsonObject1.getBoolean(
                                                                            Constant.ERROR
                                                                        )
                                                                    ) {
                                                                        cityArrayList.removeAt(
                                                                            cityArrayList.size - 1
                                                                        )
                                                                        cityAdapter.notifyItemRemoved(
                                                                            cityArrayList.size
                                                                        )

                                                                        val jsonArray =
                                                                            jsonObject1.getJSONArray(
                                                                                Constant.DATA
                                                                            )

                                                                        for (i in 0 until jsonArray.length()) {
                                                                            val jsonObject2 =
                                                                                jsonArray.getJSONObject(
                                                                                    i
                                                                                )
                                                                            val city =
                                                                                Gson().fromJson(
                                                                                    jsonObject2.toString(),
                                                                                    City::class.java
                                                                                )
                                                                            cityArrayList.add(city)
                                                                        }
                                                                        cityAdapter.notifyDataSetChanged()
                                                                        cityAdapter.setLoaded()
                                                                        isLoadMore = false
                                                                    }
                                                                } catch (e: JSONException) {
                                                                    e.printStackTrace()
                                                                    e.printStackTrace()
                                                                }
                                                            }
                                                        }
                                                    }, activity, Constant.CITY_URL, params1, false)
                                                }
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            shimmerFrameLayout.visibility = View.GONE
                            shimmerFrameLayout.stopShimmer()
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        shimmerFrameLayout.visibility = View.GONE
                        shimmerFrameLayout.stopShimmer()
                        progressBar.visibility = View.GONE
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.CITY_URL, params, false)
    }

    private fun getAreaData(
        search: String,
        recyclerView: RecyclerView,
        tvAlert: TextView,
        linearLayoutManager: LinearLayoutManager,
        scrollView: NestedScrollView,
        dialog: AlertDialog,
        shimmerFrameLayout: ShimmerFrameLayout
    ) {
        areaArrayList = ArrayList()
        progressBar.visibility = View.VISIBLE
        val params: MutableMap<String, String> = HashMap()
        params[Constant.CITY_ID] = cityId
        params[Constant.SEARCH] = search
        params[Constant.OFFSET] = "" + offset
        params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 20)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            try {
                                total = jsonObject.getString(Constant.TOTAL).toInt()
                                val jsonObject = JSONObject(response)
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)

                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val area =
                                        Gson().fromJson(jsonObject1.toString(), City::class.java)
                                    areaArrayList.add(area)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (offset == 0) {
                                progressBar.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                tvAlert.visibility = View.GONE
                                areaAdapter = AreaAdapter(activity, areaArrayList, dialog)
                                areaAdapter.setHasStableIds(true)
                                recyclerView.adapter = areaAdapter
                                shimmerFrameLayout.visibility = View.GONE
                                shimmerFrameLayout.stopShimmer()
                                scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        if (areaArrayList.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == areaArrayList.size - 1) {
                                                    //bottom of list!
                                                    areaArrayList.add(City())
                                                    areaAdapter.notifyItemInserted(areaArrayList.size - 1)
                                                    offset += Constant.LOAD_ITEM_LIMIT + 20
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.CITY_ID] = cityId
                                                    params1[Constant.SEARCH] = search
                                                    params1[Constant.OFFSET] = "" + offset
                                                    params1[Constant.LIMIT] =
                                                        "" + (Constant.LOAD_ITEM_LIMIT + 20)
                                                    requestToVolley(
                                                        object : VolleyCallback {
                                                            override fun onSuccess(
                                                                result: Boolean,
                                                                response: String
                                                            ) {
                                                                if (result) {
                                                                    try {
                                                                        val jsonObject1 =
                                                                            JSONObject(response)
                                                                        if (!jsonObject1.getBoolean(
                                                                                Constant.ERROR
                                                                            )
                                                                        ) {
                                                                            areaArrayList.removeAt(
                                                                                areaArrayList.size - 1
                                                                            )
                                                                            areaAdapter.notifyItemRemoved(
                                                                                areaArrayList.size
                                                                            )

                                                                            val jsonArray =
                                                                                jsonObject1.getJSONArray(
                                                                                    Constant.DATA
                                                                                )
                                                                            for (i in 0 until jsonArray.length()) {
                                                                                val jsonObject2 =
                                                                                    jsonArray.getJSONObject(
                                                                                        i
                                                                                    )
                                                                                val area =
                                                                                    Gson().fromJson(
                                                                                        jsonObject2.toString(),
                                                                                        City::class.java
                                                                                    )
                                                                                areaArrayList.add(
                                                                                    area
                                                                                )
                                                                            }
                                                                            areaAdapter.notifyDataSetChanged()
                                                                            areaAdapter.setLoaded()
                                                                            isLoadMore = false
                                                                        }
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                        e.printStackTrace()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        activity,
                                                        Constant.GET_AREA_BY_CITY,
                                                        params1,
                                                        false
                                                    )
                                                }
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            shimmerFrameLayout.visibility = View.GONE
                            shimmerFrameLayout.stopShimmer()
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        shimmerFrameLayout.visibility = View.GONE
                        shimmerFrameLayout.stopShimmer()
                        progressBar.visibility = View.GONE
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.GET_AREA_BY_CITY, params, false)
    }

    class CityAdapter(
        val activity: Activity, private val cities: ArrayList<City?>,
        val dialog: AlertDialog
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // for load more
        val viewTypeItem = 0
        val viewTypeLoading = 1
        private var isLoading = false
        val session = Session(activity)
        fun add(position: Int, city: City) {
            cities.add(position, city)
            notifyItemInserted(position)
        }

        fun setLoaded() {
            isLoading = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View
            return when (viewType) {
                viewTypeItem -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.lyt_city_area_list, parent, false)
                    ItemHolder(view)
                }
                viewTypeLoading -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_progressbar, parent, false)
                    ViewHolderLoading(view)
                }
                else -> throw IllegalArgumentException("unexpected viewType: $viewType")
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
            if (holderParent is ItemHolder) {
                try {
                    val city = cities[position]!!
                    holderParent.tvPinCode.text = city.name
                    holderParent.tvPinCode.setOnClickListener {
                        edtCity.text = city.name
                        cityId = city.id
                        areaId = "0"
                        edtArea.text = ""
                        edtArea.isEnabled = true
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (holderParent is ViewHolderLoading) {
                holderParent.progressBar.isIndeterminate = true
            }
        }

        override fun getItemCount(): Int {
            return cities.size
        }

        override fun getItemViewType(position: Int): Int {
            return viewTypeItem
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
            val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

        }

        internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvPinCode: TextView = itemView.findViewById(R.id.tvPinCode)

        }

    }

    class AreaAdapter(
        val activity: Activity,
        private val areas: ArrayList<City?>,
        val dialog: AlertDialog
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // for load more
        val viewTypeItem = 0
        val viewTypeLoading = 1
        private var isLoading = false
        val session = Session(activity)
        fun add(position: Int, area: City) {
            areas.add(position, area)
            notifyItemInserted(position)
        }

        fun setLoaded() {
            isLoading = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View
            return when (viewType) {
                viewTypeItem -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.lyt_city_area_list, parent, false)
                    ItemHolder(view)
                }
                viewTypeLoading -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_progressbar, parent, false)
                    ViewHolderLoading(view)
                }
                else -> throw IllegalArgumentException("unexpected viewType: $viewType")
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
            if (holderParent is ItemHolder) {
                try {
                    val area = areas[position]!!
                    holderParent.tvPinCode.text = area.name
                    holderParent.tvPinCode.setOnClickListener {
                        edtArea.text = area.name
                        areaId = area.id
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (holderParent is ViewHolderLoading) {
                holderParent.progressBar.isIndeterminate = true
            }
        }

        override fun getItemCount(): Int {
            return areas.size
        }

        override fun getItemViewType(position: Int): Int {
            return viewTypeItem
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
            val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

        }

        internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvPinCode: TextView = itemView.findViewById(R.id.tvPinCode)

        }

    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = activity.getString(R.string.address)
        requireActivity().invalidateOptionsMenu()
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

    override fun onMapReady(googleMap: GoogleMap) {
        val saveLatitude: Double
        val saveLongitude: Double
        if (addressFor == "update") {
            btnSubmit.text = getString(R.string.update)
            address1 = arguments?.getSerializable("model") as Address
            cityId = address1.city_id
            areaId = address1.area_id
            latitude = address1.latitude.toDouble()
            longitude = address1.longitude.toDouble()
        }
        if (latitude <= 0 || longitude <= 0) {
            saveLatitude = session.getCoordinates(Constant.LATITUDE).toDouble()
            saveLongitude = session.getCoordinates(Constant.LONGITUDE).toDouble()
        } else {
            saveLatitude = latitude
            saveLongitude = longitude
        }
        googleMap.clear()
        val latLng = LatLng(saveLatitude, saveLongitude)
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.current_location))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18f))
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.toolbar_layout).isVisible = false
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var tvCurrent: TextView
        var latitude = 0.00
        var longitude = 0.00
        lateinit var address1: Address
        lateinit var mapFragment: SupportMapFragment
        lateinit var mapReadyCallback: OnMapReadyCallback
        var cityId: String = "0"
        var areaId: String = "0"

        @SuppressLint("StaticFieldLeak")
        lateinit var edtCity: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var edtArea: TextView
    }
}