package wrteam.ecart.shop.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.facebook.shimmer.ShimmerFrameLayout
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.OrderItemsAdapter
import wrteam.ecart.shop.adapter.ProductImagesAdapter
import wrteam.ecart.shop.adapter.SelectedImagesAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.createJWT
import wrteam.ecart.shop.helper.ApiConfig.Companion.getOrders
import wrteam.ecart.shop.helper.ApiConfig.Companion.getSettings
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.OrderList
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class
OrderDetailFragment : Fragment() {
    private val openMediaPicker = 1  // Request code
    private val permissionReadExternalStorage = 100       // Request code for read external storage
    private lateinit var btnReorder: Button
    private lateinit var btnGetSellerDirection: Button
    private lateinit var btnCallToSeller: Button
    private lateinit var btnInvoice: Button
    lateinit var root: View
    lateinit var order: OrderList
    private lateinit var tvOrderOTP: TextView
    private lateinit var tvItemTotal: TextView
    private lateinit var tvDeliveryCharge: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvPromoCode: TextView
    private lateinit var tvPCAmount: TextView
    private lateinit var tvWallet: TextView
    private lateinit var tvFinalTotal: TextView
    private lateinit var tvDPercent: TextView
    private lateinit var tvDAmount: TextView
    private lateinit var tvCancelDetail: TextView
    private lateinit var tvOtherDetails: TextView
    private lateinit var tvOrderID: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvPickUpAddress: TextView
    private lateinit var btnOtherImages: TextView
    private lateinit var btnSubmit: TextView
    private lateinit var tvReceiptStatus: TextView
    private lateinit var tvReceiptStatusReason: TextView
    private lateinit var tvPickupTime: TextView
    private lateinit var tvBankDetail: TextView
    lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewImageGallery: RecyclerView
    private lateinit var recyclerViewReceiptImages: RecyclerView
    private lateinit var relativeLyt: RelativeLayout
    private lateinit var lytReceipt: RelativeLayout
    private lateinit var lytPromo: LinearLayout
    private lateinit var lytWallet: LinearLayout
    private lateinit var lytPriceDetail: LinearLayout
    private lateinit var lytOTP: LinearLayout
    private var totalAfterTax = 0.0
    lateinit var activity: Activity
    lateinit var id: String
    lateinit var session: Session
    lateinit var hashMap: HashMap<String, String>
    private lateinit var lytMainTracker: LinearLayout
    lateinit var scrollView: ScrollView
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var lytReceipt1: LinearLayout
    private lateinit var lytButton: LinearLayout
    private lateinit var productImagesAdapter: ProductImagesAdapter
    private lateinit var receiptImages: ArrayList<String>
    private lateinit var lytOrderNote: LinearLayout
    private lateinit var tvOrderNote: TextView
    private var isCancellable = false
    private var isReturnable = false
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_tracker_detail, container, false)
        activity = requireActivity()
        session = Session(activity)
        getSettings(activity)
        progressBar = root.findViewById(R.id.progressBar)
        lytReceipt = root.findViewById(R.id.lytReceipt)
        lytReceipt1 = root.findViewById(R.id.lytReceipt1)
        lytPriceDetail = root.findViewById(R.id.lytPriceDetail)
        btnOtherImages = root.findViewById(R.id.btnOtherImages)
        btnSubmit = root.findViewById(R.id.btnSubmit)
        lytPromo = root.findViewById(R.id.lytPromo)
        lytWallet = root.findViewById(R.id.lytWallet)
        tvItemTotal = root.findViewById(R.id.tvItemTotal)
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge)
        tvDAmount = root.findViewById(R.id.tvDAmount)
        tvDPercent = root.findViewById(R.id.tvDPercent)
        tvTotal = root.findViewById(R.id.tvTotal)
        tvPromoCode = root.findViewById(R.id.tvPromoCode)
        tvPCAmount = root.findViewById(R.id.tvPCAmount)
        tvWallet = root.findViewById(R.id.tvWallet)
        tvFinalTotal = root.findViewById(R.id.tvFinalTotal)
        tvOrderID = root.findViewById(R.id.tvOrderID)
        tvOrderDate = root.findViewById(R.id.tvOrderDate)
        tvBankDetail = root.findViewById(R.id.tvBankDetail)
        relativeLyt = root.findViewById(R.id.relativeLyt)
        tvOtherDetails = root.findViewById(R.id.tvOtherDetails)
        tvCancelDetail = root.findViewById(R.id.tvCancelDetail)
        tvReceiptStatusReason = root.findViewById(R.id.tvReceiptStatusReason)
        tvPickupTime = root.findViewById(R.id.tvPickupTime)
        lytTracker = root.findViewById(R.id.lytTracker)
        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.isNestedScrollingEnabled = false
        btnCancel = root.findViewById(R.id.btnCancel)
        btnReorder = root.findViewById(R.id.btnReorder)
        tvOrderOTP = root.findViewById(R.id.tvOrderOTP)
        tvReceiptStatus = root.findViewById(R.id.tvReceiptStatus)
        lytOTP = root.findViewById(R.id.lytOTP)
        lytMainTracker = root.findViewById(R.id.lytMainTracker)
        scrollView = root.findViewById(R.id.scrollView)
        lytPickUp = root.findViewById(R.id.lytPickUp)
        btnGetSellerDirection = root.findViewById(R.id.btnGetSellerDirection)
        btnCallToSeller = root.findViewById(R.id.btnCallToSeller)
        tvPickUpAddress = root.findViewById(R.id.tvPickUpAddress)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        btnInvoice = root.findViewById(R.id.btnInvoice)
        lytButton = root.findViewById(R.id.lytButton)
        recyclerViewImageGallery = root.findViewById(R.id.recyclerViewImageGallery)
        recyclerViewReceiptImages = root.findViewById(R.id.recyclerViewReceiptImages)
        lytOrderNote = root.findViewById(R.id.lytOrderNote)
        tvOrderNote = root.findViewById(R.id.tvOrderNote)
        hashMap = HashMap()
        getPaymentConfig()
        assert(arguments != null)
        id = arguments?.getString("id").toString()
        if (id == "") {
            order = arguments?.getSerializable("model") as OrderList
            id = order.id
            setData(order)
        } else {
            getOrderDetails(id)
        }
        tvBankDetail.setOnClickListener { openBankDetails() }
        setHasOptionsMenu(true)
        btnReorder.setOnClickListener {
            AlertDialog.Builder(
                activity
            )
                .setTitle(getString(R.string.re_order))
                .setMessage(getString(R.string.reorder_msg))
                .setPositiveButton(getString(R.string.proceed)) { dialog: DialogInterface, _: Int ->
                    getReOrderData()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .show()
        }
        receiptImages = ArrayList()
        recyclerViewImageGallery.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerViewReceiptImages.layoutManager = GridLayoutManager(activity, 3)
        recyclerViewReceiptImages.isNestedScrollingEnabled = false
        tvBankDetail.setOnClickListener { openBankDetails() }
        btnOtherImages.setOnClickListener {
            lytReceipt1.visibility = View.VISIBLE
            if (!permissionIfNeeded()) {
                val intent = Intent(activity, Gallery::class.java)
                // Set the title
                intent.putExtra("title", getString(R.string.select_media))
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode", 2)
                intent.putExtra("maxSelection", true) // Optional
                intent.putExtra("tabBarHidden", false) //Optional - default value is false
                startActivityForResult(intent, 1)
            }
        }
        btnSubmit.setOnClickListener {
            if (receiptImages.size > 0) {
                progressBar.visibility = View.VISIBLE
                submitReceipt()
            } else {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.no_receipt_select_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        btnCancel.setOnClickListener {
            val alertDialog = AlertDialog.Builder(
                activity
            )
            // Setting Dialog Message
            alertDialog.setTitle(activity.resources.getString(R.string.cancel_order))
            alertDialog.setMessage(activity.resources.getString(R.string.cancel_msg))
            alertDialog.setCancelable(false)
            val alertDialog1 = alertDialog.create()

            // Setting OK Button
            alertDialog.setPositiveButton(activity.resources.getString(R.string.yes)) { _: DialogInterface, _: Int ->
                val params: MutableMap<String, String> = HashMap()
                params[Constant.UPDATE_ORDER_STATUS] = Constant.GetVal
                params[Constant.ID] = order.id
                params[Constant.STATUS] = btnCancel.tag.toString()
                progressBar.visibility = View.VISIBLE
                requestToVolley(object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {
                        // System.out.println("================= " + response);
                        if (result) {
                            try {
                                val jsonObject = JSONObject(response)
                                if (!jsonObject.getBoolean(Constant.ERROR)) {
                                    btnCancel.visibility = View.GONE
                                    getWalletBalance(activity, Session(activity))
                                }
                                Toast.makeText(
                                    activity,
                                    jsonObject.getString(Constant.MESSAGE),
                                    Toast.LENGTH_LONG
                                ).show()
                                progressBar.visibility = View.GONE
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }, activity, Constant.ORDER_PROCESS_URL, params, false)
            }
            alertDialog.setNegativeButton(activity.resources.getString(R.string.no)) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
            // Showing Alert Message
            alertDialog.show()
        }
        return root
    }
    private fun permissionIfNeeded(): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), permissionReadExternalStorage)
            return true
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == openMediaPicker) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")!!
                for (path in selectionResult) {
                    receiptImages.add(path)
                }
                recyclerViewReceiptImages.adapter = SelectedImagesAdapter(activity, receiptImages)
            }
        }
    }

    private fun getPaymentConfig() {
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SETTINGS] = Constant.GetVal
        params[Constant.GET_PAYMENT_METHOD] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            if (jsonObject1.has(Constant.PAYMENT_METHODS)) {
                                val jsonObject = jsonObject1.getJSONObject(Constant.PAYMENT_METHODS)
                                if (jsonObject.has(Constant.direct_bank_transfer_method)) {
                                    Constant.DIRECT_BANK_TRANSFER =
                                        jsonObject.getString(Constant.direct_bank_transfer_method)
                                    Constant.ACCOUNT_NAME =
                                        jsonObject.getString(Constant.account_name)
                                    Constant.ACCOUNT_NUMBER =
                                        jsonObject.getString(Constant.account_number)
                                    Constant.BANK_NAME = jsonObject.getString(Constant.bank_name)
                                    Constant.BANK_CODE = jsonObject.getString(Constant.bank_code)
                                    Constant.NOTES = jsonObject.getString(Constant.notes)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false)
    }

    private fun openBankDetails() {
        run {
            @SuppressLint("InflateParams") val sheetView =
                activity.layoutInflater.inflate(R.layout.dialog_bank_detail, null)
            val parentViewGroup = sheetView.parent as ViewGroup
            parentViewGroup.removeAllViews()
            val mBottomSheetDialog = Dialog(activity)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mBottomSheetDialog.show()
            val tvAccountName = sheetView.findViewById<TextView>(R.id.tvAccountName)
            val tvAccountNumber = sheetView.findViewById<TextView>(R.id.tvAccountNumber)
            val tvBankName = sheetView.findViewById<TextView>(R.id.tvBankName)
            val tvIFSCCode = sheetView.findViewById<TextView>(R.id.tvIFSCCode)
            val tvExtraNote = sheetView.findViewById<TextView>(R.id.tvExtraNote)
            tvAccountName.text = Constant.ACCOUNT_NAME
            tvAccountNumber.text = Constant.ACCOUNT_NUMBER
            tvBankName.text = Constant.BANK_NAME
            tvIFSCCode.text = Constant.BANK_CODE
            tvExtraNote.text = Constant.NOTES
        }
    }

    private fun getReOrderData() {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_REORDER_DATA] = Constant.GetVal
        params[Constant.ID] = id
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        val jsonArray =
                            jsonObject.getJSONObject(Constant.DATA).getJSONArray(Constant.ITEMS)
                        for (i in 0 until jsonArray.length()) {
                            hashMap[jsonArray.getJSONObject(i)
                                .getString(Constant.PRODUCT_VARIANT_ID)] =
                                jsonArray.getJSONObject(i).getString(
                                    Constant.QUANTITY
                                )
                        }
                        addMultipleProductInCart(session, activity, hashMap)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false)
    }

    private fun getOrderDetails(id: String) {
        scrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_ORDERS] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        params[Constant.ORDER_ID] = id
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            setData(getOrders(jsonObject.getJSONArray(Constant.DATA))[0]!!)
                        } else {
                            scrollView.visibility = View.VISIBLE
                            mShimmerViewContainer.visibility = View.GONE
                            mShimmerViewContainer.stopShimmer()
                        }
                    } catch (e: JSONException) {
                        scrollView.visibility = View.VISIBLE
                        mShimmerViewContainer.visibility = View.GONE
                        mShimmerViewContainer.stopShimmer()
                    }
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false)
    }

    @SuppressLint("SetTextI18n")
    fun setData(order: OrderList) {
        try {
            tvOrderID.text = order.id
            if (order.otp == "0" || order.otp == "") {
                lytOTP.visibility = View.GONE
            } else {
                tvOrderOTP.text = order.otp
            }

            if(order.order_note.isNotEmpty()){
                tvOrderNote.text = order.order_note
            }else{
                lytOrderNote.visibility = View.GONE
            }

            for (i in order.items.indices) {
                val orderItem = order.items[i]
                if (orderItem.cancelable_status.equals("1", ignoreCase = true)) {
                    if (order.active_status.equals("received", ignoreCase = true)) {
                        isCancellable = orderItem.till_status.equals("received", ignoreCase = true)
                    }
                    if (order.active_status.equals("processed", ignoreCase = true)) {
                        isCancellable = orderItem.till_status.equals(
                            "received",
                            ignoreCase = true
                        ) || orderItem.till_status.equals("processed", ignoreCase = true)
                    }
                    if (order.active_status.equals("shipped", ignoreCase = true)) {
                        isCancellable = orderItem.till_status.equals(
                            "received",
                            ignoreCase = true
                        ) || orderItem.till_status.equals(
                            "processed",
                            ignoreCase = true
                        ) || orderItem.till_status.equals("shipped", ignoreCase = true)
                    }
                }
                isReturnable = orderItem.return_status.equals(
                    "1",
                    ignoreCase = true
                ) && order.active_status.equals("delivered", ignoreCase = true)
            }
            tvOrderDate.text = order.date_added
            tvOtherDetails.text =
                getString(R.string.name_1) + order.user_name + getString(R.string.mobile_no_1) + order.mobile + getString(
                    R.string.address_1
                ) + order.address
            totalAfterTax =
                order.total.toDouble() + order.delivery_charge.toDouble() + order.tax_amount.toDouble()
            tvItemTotal.text = session.getData(Constant.currency) + stringFormat(
                order.total
            )
            tvDeliveryCharge.text = "+ " + session.getData(Constant.currency) + stringFormat(
                order.delivery_charge
            )
            tvDPercent.text = getString(R.string.discount) + "(" + order.discount + "%) :"
            tvDAmount.text = "- " + session.getData(Constant.currency) + stringFormat(
                order.discount_rupees
            )
            tvTotal.text = session.getData(Constant.currency) + totalAfterTax
            tvPCAmount.text = "- " + session.getData(Constant.currency) + stringFormat(
                order.promo_discount
            )
            tvWallet.text = "- " + session.getData(Constant.currency) + stringFormat(
                order.wallet_balance
            )
            tvFinalTotal.text = session.getData(Constant.currency) + stringFormat(
                order.final_total
            )
            lytTracker.weightSum =
                (order.status_name.size + (order.status_name.size - 1)).toFloat()
            tvReceiptStatus.text = when {
                order.bank_transfer_status.equals(
                    "0",
                    ignoreCase = true
                ) -> getString(R.string.pending)
                order.bank_transfer_status.equals(
                    "1",
                    ignoreCase = true
                ) -> getString(R.string.accepted)
                else -> getString(R.string.rejected)
            }
            if (order.bank_transfer_status.equals("2", ignoreCase = true)) {
                tvReceiptStatusReason.visibility = View.VISIBLE
                tvReceiptStatusReason.text = order.bank_transfer_message
            }
            productImagesAdapter =
                ProductImagesAdapter(activity, order.attachment, "api", order.id)
            recyclerViewImageGallery.adapter = productImagesAdapter
            lytPickUp.visibility = if (order.local_pickup == "1") View.VISIBLE else View.GONE
            lytReceipt.visibility = if (order.payment_method.equals(
                    "bank transfer",
                    ignoreCase = true
                )
            ) View.VISIBLE else View.GONE
            if (order.local_pickup == "1") {
                tvPickupTime.text =
                    if (order.pickup_time == "0000-00-00 00:00:00") activity.getString(R.string.estimate_pickup_time_msg) else order.pickup_time
            }
            tvPickUpAddress.text = session.getData(Constant.store_address)
            btnCallToSeller.setOnClickListener { 
                try {
                    val callIntent = Intent(Intent.ACTION_CALL)
                    if (ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            1
                        )
                    } else {
                        callIntent.data = Uri.parse(
                            "tel:" + session.getData(Constant.support_number)
                                .replace(" ", "")
                        )
                        startActivity(callIntent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            btnGetSellerDirection.setOnClickListener { 
                val builder1 = android.app.AlertDialog.Builder(activity)
                builder1.setMessage(R.string.map_open_message)
                builder1.setCancelable(true)
                builder1.setPositiveButton(
                    getString(R.string.yes)
                ) { _: DialogInterface, _: Int ->
//                                com.google.android.apps.maps
                    try {
                        val googleMapIntentUri = Uri.parse(
                            "google.navigation:q=" + session.getData(
                                Constant.map_latitude
                            ) + "," + session.getData(Constant.map_longitude) + ""
                        )
                        val mapIntent = Intent(Intent.ACTION_VIEW, googleMapIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        activity.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val builder11 = android.app.AlertDialog.Builder(activity)
                        builder11.setMessage("Please install google map first.")
                        builder11.setCancelable(true)
                        builder11.setPositiveButton(getString(R.string.ok)) { dialog1: DialogInterface, _: Int -> dialog1.cancel() }
                        val alert11 = builder11.create()
                        alert11.show()
                    }
                }
                builder1.setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                val alert11 = builder1.create()
                alert11.show()
            }
            btnInvoice.setOnClickListener { 
                val fragment: Fragment = WebViewFragment()
                val bundle = Bundle()
                bundle.putString("type", activity.getString(R.string.order) + "#" + order.id)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            if (isCancellable) {
                lytButton.weightSum = 3f
                btnCancel.tag = "cancelled"
                btnCancel.text = activity.getString(R.string.cancel_order)
                btnCancel.visibility = View.VISIBLE
            } else if (isReturnable) {
                lytButton.weightSum = 3f
                btnCancel.tag = "returned"
                btnCancel.text = activity.getString(R.string.return_order)
                btnCancel.visibility = View.VISIBLE
            }
            for (i in order.status_name.indices) {
                createStatusUi(activity, lytTracker, order.status_name[i], order.status_time[i])
                if (i != order.status_name.size - 1) {
                    val view = View(activity)
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.colorPrimary
                        )
                    )
                    val params1 = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        resources.getDimension(R.dimen._2sdp)
                            .toInt()
                    )
                    params1.weight = 1.0f
                    view.layoutParams = params1
                    lytTracker.addView(view)
                }
            }
            scrollView.visibility = View.VISIBLE
            mShimmerViewContainer.visibility = View.GONE
            mShimmerViewContainer.stopShimmer()
            recyclerView.adapter = OrderItemsAdapter(activity, order, "detail")
            relativeLyt.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
            scrollView.visibility = View.VISIBLE
            mShimmerViewContainer.visibility = View.GONE
            mShimmerViewContainer.stopShimmer()
        }
    }

    private fun createStatusUi(
        activity: Activity,
        linearLayout: LinearLayout,
        status1: String,
        time: String
    ) {
        var status = status1
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.weight = 1.0f
        status = if (status.equals(
                Constant.AWAITING_PAYMENT,
                ignoreCase = true
            )
        ) activity.getString(R.string.awaiting_payment) else if (status.equals(
                "received",
                ignoreCase = true
            )
        ) activity.getString(R.string.order_received) else if (status.equals(
                "processed",
                ignoreCase = true
            )
        ) activity.getString(R.string.order_processed) else if (status.equals(
                "shipped",
                ignoreCase = true
            )
        ) activity.getString(R.string.order_shipped) else if (status.equals(
                "ready_to_pickup",
                ignoreCase = true
            )
        ) activity.getString(R.string.order_ready_to_pickup) else if (status.equals(
                "delivered",
                ignoreCase = true
            )
        ) if (order.local_pickup == "1") activity.getString(R.string.order_picked_up) else activity.getString(
            R.string.order_delivered
        ) else if (status.equals(
                "cancelled",
                ignoreCase = true
            )
        ) activity.getString(R.string.order_cancel_) else activity.getString(R.string.order_returned)
        val layout = LinearLayout(activity)
        layout.layoutParams = params
        layout.orientation = LinearLayout.VERTICAL
        val textView = TextView(activity)
        val textView1 = TextView(activity)
        val imageView = ImageView(activity)
        imageView.setImageResource(R.drawable.ic_tracker_btn)
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.textSize = activity.resources.getDimension(R.dimen._3ssp)
        textView1.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView1.textSize = activity.resources.getDimension(R.dimen._3ssp)
        textView.text = status
        textView1.text = if (time != "") """
     ${time.split("\\s".toRegex()).toTypedArray()[0]}
     ${time.split("\\s".toRegex()).toTypedArray()[1]}
     """.trimIndent() else ""
        imageView.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary))
        textView.setTextColor(ContextCompat.getColor(activity, R.color.txt_color))
        textView1.setTextColor(ContextCompat.getColor(activity, R.color.txt_color))
        layout.addView(textView, 0)
        layout.addView(imageView, 1)
        layout.addView(textView1, 2)
        linearLayout.addView(layout)
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.order_track_detail)
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

    @SuppressLint("NotifyDataSetChanged")
    fun submitReceipt() {
        try {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val client = OkHttpClient().newBuilder().build()
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal)
            builder.addFormDataPart(Constant.UPLOAD_BANK_TRANSFER_ATTACHMENT, Constant.GetVal)
            builder.addFormDataPart(Constant.ORDER_ID, order.id)
            for (i in receiptImages.indices) {
                val file = File(receiptImages[i])
                builder.addFormDataPart(
                    Constant.IMAGES,
                    file.name,
                    RequestBody.create(MediaType.parse("application/octet-stream"), file)
                )
            }
            val body: RequestBody = builder.build()
            val request = Request.Builder()
                .url(Constant.ORDER_PROCESS_URL)
                .method("POST", body)
                .addHeader(
                    Constant.AUTHORIZATION,
                    "Bearer " + createJWT("eKart", "eKart Authentication")
                )
                .build()
            val response = client.newCall(request).execute()
            val jsonObject = JSONObject(response.peekBody(Long.MAX_VALUE).string())
            Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
            progressBar.visibility = View.GONE
            Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.toolbar_layout).isVisible = false
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = true
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var progressBar: ProgressBar

        @SuppressLint("StaticFieldLeak")
        lateinit var btnCancel: Button

        @SuppressLint("StaticFieldLeak")
        lateinit var lytTracker: LinearLayout

        @SuppressLint("StaticFieldLeak")
        lateinit var lytPickUp: LinearLayout
    }
}