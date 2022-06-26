package wrteam.ecart.shop.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Uri
import android.text.Html
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.paystack.android.PaystackSdk
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.OfferAdapter
import wrteam.ecart.shop.model.OfferImages
import wrteam.ecart.shop.model.OrderList
import wrteam.ecart.shop.model.Slider
import java.security.Key
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess

@SuppressLint("SetTextI18n")
class ApiConfig : Application() {
    private lateinit var mRequestQueue: RequestQueue
    override fun onCreate() {
        super.onCreate()
        instance = this
        mRequestQueue = Volley.newRequestQueue(applicationContext)
    }

    fun getAppEnvironment(): AppEnvironment {
        return appEnvironment
    }

    fun getRequestQueue(): RequestQueue {
        return mRequestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = tag
        mRequestQueue.add(req)
    }

    companion object {
        val tag: String = ApiConfig::class.java.simpleName

        @get:Synchronized
        lateinit var instance: ApiConfig
        lateinit var appEnvironment: AppEnvironment
        private var isDialogOpen = false
        fun volleyErrorMessage(error: VolleyError): String {
            var message = ""
            try {
                message = when (error) {
                    is NetworkError -> {
                        "Cannot connect to Internet...Please check your connection!"
                    }
                    is ServerError -> {
                        "The server could not be found. Please try again after some time"
                    }
                    is AuthFailureError -> {
                        "Cannot connect to Internet...Please check your connection!"
                    }
                    is ParseError -> {
                        "Parsing error! Please try again after some time"
                    }
                    is TimeoutError -> {
                        "Connection TimeOut! Please check your internet connection."
                    }
                    else -> ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return message
        }

        @JvmStatic
        fun calculateDays(activity: Activity, days: Long): String {
            val weeks = days / 7
            val months = weeks / 4
            val years = months / 12
            return if (days == 1L) {
                days.toString() + activity.getString(R.string.day)
            } else {
                if (days <= 6) {
                    days.toString() + activity.getString(R.string.days)
                } else {
                    if (weeks <= 3) {
                        weeks.toString() + if (weeks == 1L) activity.getString(R.string.week) else activity.getString(
                            R.string.weeks
                        )
                    } else {
                        if (months <= 12) {
                            months.toString() + if (months == 1L) activity.getString(R.string.month) else activity.getString(
                                R.string.months
                            )
                        } else {
                            years.toString() + if (years == 1L) activity.getString(R.string.year) else activity.getString(
                                R.string.years
                            )
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun getOrders(jsonArray: JSONArray): ArrayList<OrderList?> {
            val orderTrackerArrayList = ArrayList<OrderList?>()
            try {
                for (i in 0 until jsonArray.length()) {
                    val orderTracker =
                        Gson().fromJson(jsonArray[i].toString(), OrderList::class.java)
                    orderTrackerArrayList.add(orderTracker)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return orderTrackerArrayList
        }

        @JvmStatic
        @SuppressLint("DefaultLocale")
        fun stringFormat(number: String): String {
            return String.format("%.2f", number.toDouble())
        }

        @JvmStatic
        fun getMonth(activity: Activity, monthNo: Int): String {
            var month = ""
            when (monthNo) {
                1 -> month = activity.getString(R.string.january)
                2 -> month = activity.getString(R.string.february)
                3 -> month = activity.getString(R.string.march)
                4 -> month = activity.getString(R.string.april)
                5 -> month = activity.getString(R.string.may)
                6 -> month = activity.getString(R.string.june)
                7 -> month = activity.getString(R.string.july)
                8 -> month = activity.getString(R.string.august)
                9 -> month = activity.getString(R.string.september)
                10 -> month = activity.getString(R.string.october)
                11 -> month = activity.getString(R.string.november)
                12 -> month = activity.getString(R.string.december)
                else -> {}
            }
            return month
        }

        @JvmStatic
        fun getDayOfWeek(activity: Activity, dayNo: Int): String {
            var month = ""
            when (dayNo) {
                1 -> month = activity.getString(R.string.sunday)
                2 -> month = activity.getString(R.string.monday)
                3 -> month = activity.getString(R.string.tuesday)
                4 -> month = activity.getString(R.string.wednesday)
                5 -> month = activity.getString(R.string.thursday)
                6 -> month = activity.getString(R.string.friday)
                7 -> month = activity.getString(R.string.saturday)
                else -> {}
            }
            return month
        }

        @JvmStatic
        fun getDates(startDate: String, endDate: String): ArrayList<String> {
            val dates = ArrayList<String>()
            @SuppressLint("SimpleDateFormat") val df1: DateFormat = SimpleDateFormat("dd-MM-yyyy")
            lateinit var date1: Date
            lateinit var date2: Date
            try {
                date1 = df1.parse(startDate)!!
                date2 = df1.parse(endDate)!!
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val cal1 = Calendar.getInstance()
            cal1.time = date1
            val cal2 = Calendar.getInstance()
            cal2.time = date2
            while (!cal1.after(cal2)) {
                dates.add(cal1[Calendar.DATE].toString() + "-" + (cal1[Calendar.MONTH] + 1) + "-" + cal1[Calendar.YEAR] + "-" + cal1[Calendar.DAY_OF_WEEK])
                cal1.add(Calendar.DATE, 1)
            }
            return dates
        }

        @JvmStatic
        fun removeAddress(activity: Activity, addressId: String) {
            val params: MutableMap<String, String> = HashMap()
            params[Constant.DELETE_ADDRESS] = Constant.GetVal
            params[Constant.ID] = addressId
            requestToVolley(
                object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {

                    }
                },
                activity,
                Constant.GET_ADDRESS_URL,
                params,
                false
            )
        }

        @JvmStatic
        fun getCartItemCount(activity: Activity, session: Session) {
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_USER_CART] = Constant.GetVal
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                Constant.TOTAL_CART_ITEM =
                                    jsonObject.getString(Constant.TOTAL).toInt()
                            } else {
                                Constant.TOTAL_CART_ITEM = 0
                            }
                            activity.invalidateOptionsMenu()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.CART_URL, params, false)
        }

        @JvmStatic
        fun addOrRemoveFavorite(
            activity: Activity,
            session: Session,
            productID: String,
            isAdd: Boolean
        ) {
            val params: MutableMap<String, String> = HashMap()
            if (isAdd) {
                params[Constant.ADD_TO_FAVORITES] = Constant.GetVal
            } else {
                params[Constant.REMOVE_FROM_FAVORITES] = Constant.GetVal
            }
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            params[Constant.PRODUCT_ID] = productID
            requestToVolley(
                object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {

                    }
                },
                activity,
                Constant.GET_FAVORITES_URL,
                params,
                false
            )
        }

        fun requestToVolley(
            callback: VolleyCallback,
            activity: Activity,
            url: String,
            params: MutableMap<String, String>,
            isProgress: Boolean
        ) {
            if (ProgressDisplay.mProgressBar != null) {
                ProgressDisplay.mProgressBar!!.visibility = View.GONE
            }
            val progressDisplay = ProgressDisplay(activity)
            progressDisplay.hideProgress()
            if (isConnected(activity)) {
                if (isProgress) progressDisplay.showProgress()
                val stringRequest: StringRequest = object : StringRequest(
                    Method.POST, url, Response.Listener { response: String ->
                        callback.onSuccess(true, response)
                        if (isProgress) progressDisplay.hideProgress()
                    },
                    Response.ErrorListener { error: VolleyError ->
                        if (isProgress) progressDisplay.hideProgress()
                        callback.onSuccess(false, "")
                        val message = volleyErrorMessage(error)
                        if (message != "") Toast.makeText(activity, message, Toast.LENGTH_SHORT)
                            .show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val params1: MutableMap<String, String> = HashMap()
                        params1[Constant.AUTHORIZATION] =
                            "Bearer " + createJWT("eKart", "eKart Authentication")
                        return params1
                    }

                    override fun getParams(): MutableMap<String, String> {
                        params[Constant.AccessKey] = Constant.AccessKeyVal
                        return params
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(0, 0, 0F)
                instance.getRequestQueue().cache.clear()
                instance.addToRequestQueue(stringRequest)
            }
        }

        @JvmStatic
        fun toTitleCase(str: String): String {
            var space = true
            val builder = StringBuilder(str)
            val len = builder.length
            for (i in 0 until len) {
                val c = builder[i]
                if (space) {
                    if (!Character.isWhitespace(c)) {
                        // Convert to title case and switch out of whitespace mode.
                        builder.setCharAt(i, Character.toTitleCase(c))
                        space = false
                    }
                } else if (Character.isWhitespace(c)) {
                    space = true
                } else {
                    builder.setCharAt(i, Character.toLowerCase(c))
                }
            }
            return builder.toString()
        }

        @JvmStatic
        fun createJWT(issuer: String, subject: String): String {
            var key = ""
            try {
                val signatureAlgorithm = SignatureAlgorithm.HS256
                val nowMillis = System.currentTimeMillis()
                val now = Date(nowMillis)
                val apiKeySecretBytes = Constant.JWT_KEY.toByteArray()
                val signingKey: Key = SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.jcaName)
                val builder = Jwts.builder()
                    .setIssuedAt(now)
                    .setSubject(subject)
                    .setIssuer(issuer)
                    .signWith(signatureAlgorithm, signingKey)
                key = builder.compact()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return key
        }

        @JvmStatic
        fun checkValidation(
            item: String,
            isMailValidation: Boolean,
            isMobileValidation: Boolean
        ): Boolean {
            var result = false
            if (item.isEmpty()) {
                result = true
            } else if (isMailValidation) {
                if (!Patterns.EMAIL_ADDRESS.matcher(item).matches()) {
                    result = true
                }
            } else if (isMobileValidation) {
                if (!Patterns.PHONE.matcher(item).matches()) {
                    result = true
                }
            }
            return result
        }

        @JvmStatic
        @SuppressLint("DefaultLocale")
        fun getDiscount(originalPrice: Double, discountedPrice: Double): String {
            return String.format(
                "%.0f",
                ("" + ((originalPrice - discountedPrice + originalPrice) / originalPrice - 1) * 100).toDouble()
            ) + "%"
        }

        @JvmStatic
        fun addMultipleProductInCart(
            session: Session,
            activity: Activity,
            map: HashMap<String, String>
        ) {
            try {
                if (map.size > 0) {
                    val ids = map.keys.toString().replace("[", "").replace("]", "").replace(" ", "")
                    val qty =
                        map.values.toString().replace("[", "").replace("]", "").replace(" ", "")
                    val params: MutableMap<String, String> = HashMap()
                    params[Constant.ADD_MULTIPLE_ITEMS] = Constant.GetVal
                    if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                        session.getData(
                            Constant.ID
                        )
                    params[Constant.PRODUCT_VARIANT_ID] = ids
                    params[Constant.QTY] = qty
                    requestToVolley(object : VolleyCallback {
                        override fun onSuccess(result: Boolean, response: String) {
                            if (result) {
                                getCartItemCount(activity, session)
                            }
                        }
                    }, activity, Constant.CART_URL, params, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun addMultipleProductInSaveForLater(
            session: Session,
            activity: Activity,
            map: HashMap<String, String>
        ) {
            if (map.size > 0) {
                val ids = map.keys.toString().replace("[", "").replace("]", "").replace(" ", "")
                val qty = map.values.toString().replace("[", "").replace("]", "").replace(" ", "")
                val params: MutableMap<String, String> = HashMap()
                params[Constant.SAVE_FOR_LATER_ITEMS] = Constant.GetVal
                if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                    session.getData(
                        Constant.ID
                    )
                params[Constant.PRODUCT_VARIANT_ID] = ids
                params[Constant.QTY] = qty
                requestToVolley(object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {
                        if (result) {
                            getCartItemCount(activity, session)
                        }
                    }
                }, activity, Constant.CART_URL, params, false)
            }
        }

        @JvmStatic
        fun addMarkers(
            currentPage: Int,
            imageList: ArrayList<Slider>,
            mMarkersLayout: LinearLayout,
            activity: Activity
        ) {
            val markers = arrayOfNulls<TextView>(imageList.size)
            mMarkersLayout.removeAllViews()
            for (i in markers.indices) {
                markers[i] = TextView(activity)
                markers[i]?.text = Html.fromHtml("&#8226;", 0)
                markers[i]?.textSize = 35f
                markers[i]?.setTextColor(ContextCompat.getColor(activity, R.color.gray))
                mMarkersLayout.addView(markers[i])
            }
            if (markers.isNotEmpty()) markers[currentPage]?.setTextColor(
                ContextCompat.getColor(
                    activity,
                    R.color.colorPrimary
                )
            )
        }

        @JvmStatic
        fun buildCounterDrawable(count: Int, activity: Activity): Drawable {
            val inflater = LayoutInflater.from(activity)
            @SuppressLint("InflateParams") val view =
                inflater.inflate(R.layout.counter_menuitem_layout, null)
            val textView = view.findViewById<TextView>(R.id.tvCounter)
            val lytCount = view.findViewById<RelativeLayout>(R.id.lytCount)
            if (count == 0) {
                lytCount.visibility = View.GONE
            } else {
                lytCount.visibility = View.VISIBLE
                textView.text = "" + count
            }
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            view.isDrawingCacheEnabled = true
            view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            return BitmapDrawable(activity.resources, bitmap)
        }

        @JvmStatic
        fun getSettings(activity: Activity) {
            val session = Session(activity)
            val params: MutableMap<String, String> = HashMap()
            params[Constant.SETTINGS] = Constant.GetVal
            params[Constant.GET_TIMEZONE] = Constant.GetVal
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject1 = JSONObject(response)
                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                val jsonObject = jsonObject1.getJSONObject(Constant.SETTINGS)
                                session.setData(
                                    Constant.minimum_version_required, jsonObject.getString(
                                        Constant.minimum_version_required
                                    )
                                )
                                session.setData(
                                    Constant.is_version_system_on,
                                    jsonObject.getString(Constant.is_version_system_on)
                                )
                                session.setData(
                                    Constant.support_system,
                                    jsonObject.getString(Constant.support_system)
                                )
                                session.setData(
                                    Constant.currency,
                                    jsonObject.getString(Constant.currency)
                                )
                                session.setData(
                                    Constant.current_date,
                                    jsonObject.getString(Constant.current_date)
                                )
                                session.setData(
                                    Constant.delivery_charge,
                                    jsonObject.getString(Constant.delivery_charge)
                                )
                                session.setData(
                                    Constant.min_order_amount_for_free_delivery,
                                    jsonObject.getString(
                                        Constant.min_order_amount_for_free_delivery
                                    )
                                )
                                session.setData(
                                    Constant.min_order_amount,
                                    jsonObject.getString(Constant.min_order_amount)
                                )
                                session.setData(
                                    Constant.max_cart_items_count,
                                    jsonObject.getString(Constant.max_cart_items_count)
                                )
                                session.setData(
                                    Constant.area_wise_delivery_charge, jsonObject.getString(
                                        Constant.area_wise_delivery_charge
                                    )
                                )
                                session.setData(
                                    Constant.is_refer_earn_on,
                                    jsonObject.getString(Constant.is_refer_earn_on)
                                )
                                session.setData(
                                    Constant.refer_earn_bonus,
                                    jsonObject.getString(Constant.refer_earn_bonus)
                                )
                                session.setData(
                                    Constant.refer_earn_bonus,
                                    jsonObject.getString(Constant.refer_earn_bonus)
                                )
                                session.setData(
                                    Constant.refer_earn_method,
                                    jsonObject.getString(Constant.refer_earn_method)
                                )
                                session.setData(
                                    Constant.max_refer_earn_amount,
                                    jsonObject.getString(Constant.max_refer_earn_amount)
                                )
                                session.setData(
                                    Constant.max_product_return_days, jsonObject.getString(
                                        Constant.max_product_return_days
                                    )
                                )
                                session.setData(
                                    Constant.user_wallet_refill_limit, jsonObject.getString(
                                        Constant.user_wallet_refill_limit
                                    )
                                )
                                session.setData(
                                    Constant.min_refer_earn_order_amount, jsonObject.getString(
                                        Constant.min_refer_earn_order_amount
                                    )
                                )
                                session.setData(
                                    Constant.ratings,
                                    jsonObject.getString(Constant.ratings)
                                )
                                session.setData(
                                    Constant.local_pickup,
                                    jsonObject.getString(Constant.local_pickup)
                                )
                                session.setData(
                                    Constant.support_number,
                                    jsonObject.getString(Constant.support_number)
                                )
                                session.setData(
                                    Constant.map_latitude,
                                    jsonObject.getString(Constant.map_latitude)
                                )
                                session.setData(
                                    Constant.map_longitude,
                                    jsonObject.getString(Constant.map_longitude)
                                )
                                session.setData(
                                    Constant.store_address,
                                    jsonObject.getString(Constant.store_address)
                                )
                                session.setData(
                                    Constant.under_maintenance_system, jsonObject.getString(
                                        Constant.under_maintenance_system
                                    )
                                )
                                openUnderMaintenanceDialog(activity, session)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.SETTING_URL, params, false)
        }

        private fun openBottomDialog(activity: Activity) {
            try {
                @SuppressLint("InflateParams") val sheetView =
                    activity.layoutInflater.inflate(R.layout.dialog_update_app, null)
                val parentViewGroup = sheetView.parent as ViewGroup
                parentViewGroup.removeAllViews()
                val mBottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetTheme)
                mBottomSheetDialog.setContentView(sheetView)
                if (!Session(activity).getBoolean("update_skip")) {
                    mBottomSheetDialog.show()
                }
                mBottomSheetDialog.window
                    ?.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                val imgClose = sheetView.findViewById<ImageView>(R.id.imgClose)
                val btnNotNow = sheetView.findViewById<Button>(R.id.btnNotNow)
                val btnUpdateNow = sheetView.findViewById<Button>(R.id.btnUpdateNow)
                if (Session(activity).getData(Constant.is_version_system_on) == "0") {
                    btnNotNow.visibility = View.VISIBLE
                    imgClose.visibility = View.VISIBLE
                    mBottomSheetDialog.setCancelable(true)
                } else {
                    mBottomSheetDialog.setCancelable(false)
                }
                imgClose.setOnClickListener {
                    if (mBottomSheetDialog.isShowing) Session(activity).setBoolean(
                        "update_skip",
                        true
                    )
                    mBottomSheetDialog.dismiss()
                }
                btnNotNow.setOnClickListener {
                    Session(activity).setBoolean("update_skip", true)
                    if (mBottomSheetDialog.isShowing) mBottomSheetDialog.dismiss()
                }
                btnUpdateNow.setOnClickListener {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse(
                                Constant.PLAY_STORE_LINK + activity.packageName
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun getWalletBalance(activity: Activity, session: Session) {
            try {
                if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                    val params: MutableMap<String, String> = HashMap()
                    params[Constant.GET_USER_DATA] = Constant.GetVal
                    if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                        session.getData(
                            Constant.ID
                        )
                    requestToVolley(object : VolleyCallback {
                        override fun onSuccess(result: Boolean, response: String) {
                            if (result) {
                                try {
                                    val jsonObject = JSONObject(response)
                                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                                        session.setData(
                                            Constant.WALLET_BALANCE,
                                            jsonObject.getString(Constant.KEY_BALANCE)
                                        )
                                        session.setData(
                                            Constant.STATUS,
                                            jsonObject.getString(Constant.STATUS)
                                        )
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }, activity, Constant.USER_DATA_URL, params, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun getAddress(lat: Double, lng: Double, activity: Activity): String {
            val addresses: List<Address>
            val geocoder = Geocoder(activity, Locale.getDefault())
            return try {
                addresses = geocoder.getFromLocation(lat, lng, 1)
                addresses[0].getAddressLine(0)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        private fun compareVersion(version1: String, version2: String): Int {
            val arr1 = version1.split("\\.".toRegex()).toTypedArray()
            val arr2 = version2.split("\\.".toRegex()).toTypedArray()
            var i = 0
            while (i < arr1.size || i < arr2.size) {
                if (i < arr1.size && i < arr2.size) {
                    if (arr1[i].toInt() < arr2[i].toInt()) {
                        return -1
                    } else if (arr1[i].toInt() > arr2[i].toInt()) {
                        return 1
                    }
                } else if (i < arr1.size) {
                    if (arr1[i].toInt() != 0) {
                        return 1
                    }
                } else {
                    if (arr2[i].toInt() != 0) {
                        return -1
                    }
                }
                i++
            }
            return 0
        }

        fun openUnderMaintenanceDialog(activity: Activity, session: Session) {
            if (session.getData(Constant.under_maintenance_system) != "1") {
                try {
                    val packageInfo =
                        activity.packageManager.getPackageInfo(activity.packageName, 0)
                    val versionName = packageInfo.versionName
                    if (compareVersion(
                            versionName,
                            session.getData(Constant.minimum_version_required)
                        ) < 0
                    ) {
                        openBottomDialog(activity)
                    }
                } catch (e: NameNotFoundException) {
                    e.printStackTrace()
                }
            } else {
                @SuppressLint("InflateParams") val sheetView =
                    activity.layoutInflater.inflate(R.layout.dialog_under_maintenance, null)
                val mBottomSheetDialog = Dialog(activity)
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mBottomSheetDialog.show()
                val lottieAnimationView: LottieAnimationView =
                    sheetView.findViewById(R.id.lottieAnimationView)
                val tvCloseApp = sheetView.findViewById<TextView>(R.id.tvCloseApp)
                lottieAnimationView.setAnimation("under_maintenance.json")
                lottieAnimationView.repeatCount = LottieDrawable.INFINITE
                lottieAnimationView.playAnimation()
                tvCloseApp.setOnClickListener {
                    activity.finish()
                    exitProcess(0)
                }
                mBottomSheetDialog.setCancelable(false)
            }
        }

        @JvmStatic
        fun isConnected(activity: Activity): Boolean {
            var check = false
            try {
                val connectionManager =
                    activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectionManager.activeNetworkInfo!!
                if (networkInfo.isConnected) {
                    check = true
                } else {
                    try {
                        if (!isDialogOpen) {
                            @SuppressLint("InflateParams") val sheetView =
                                activity.layoutInflater.inflate(R.layout.dialog_no_internet, null)
                            val parentViewGroup = sheetView.parent as ViewGroup
                            parentViewGroup.removeAllViews()
                            val mBottomSheetDialog = Dialog(activity)
                            mBottomSheetDialog.setContentView(sheetView)
                            mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            mBottomSheetDialog.show()
                            isDialogOpen = true
                            val btnRetry = sheetView.findViewById<Button>(R.id.btnRetry)
                            mBottomSheetDialog.setCancelable(false)
                            btnRetry.setOnClickListener {
                                if (isConnected(activity)) {
                                    isDialogOpen = false
                                    mBottomSheetDialog.dismiss()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return check
        }

        @JvmStatic
        fun getOfferImage(
            activity: Activity,
            jsonArray: JSONArray,
            lytTopOfferImages: RecyclerView
        ) {
            try {
                val images = ArrayList<OfferImages>()
                for (i in 0 until jsonArray.length()) {
                    try {
                        images.add(
                            Gson().fromJson(
                                jsonArray.getJSONObject(i).toString(),
                                OfferImages::class.java
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (images.size > 0) {
                    lytTopOfferImages.adapter = OfferAdapter(activity, images)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun setAppEnvironment(activity: Activity) {
            appEnvironment = if (Constant.PAYUMONEY_MODE == "production") {
                AppEnvironment.PRODUCTION
            } else {
                AppEnvironment.SANDBOX
            }
            PaystackSdk.initialize(activity)
        }
    }
}