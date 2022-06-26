package wrteam.ecart.shop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.flutterwave.raveandroid.RaveConstants
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RavePayManager
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.sslcommerz.library.payment.model.datafield.MandatoryFieldModel
import com.sslcommerz.library.payment.model.dataset.TransactionInfo
import com.sslcommerz.library.payment.model.util.CurrencyType
import com.sslcommerz.library.payment.model.util.ErrorKeys
import com.sslcommerz.library.payment.model.util.SdkCategory
import com.sslcommerz.library.payment.model.util.SdkType
import com.sslcommerz.library.payment.viewmodel.listener.OnPaymentResultListener
import com.sslcommerz.library.payment.viewmodel.management.PayUsingSSLCommerz
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.DateAdapter
import wrteam.ecart.shop.adapter.SlotAdapter
import wrteam.ecart.shop.helper.*
import wrteam.ecart.shop.helper.ApiConfig.Companion.getDates
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant.randomAlphaNumeric
import wrteam.ecart.shop.helper.Constant.randomNumeric
import wrteam.ecart.shop.model.BookingDate
import wrteam.ecart.shop.model.Slot
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong

@SuppressLint("SetTextI18n")
class PaymentActivity : AppCompatActivity(), PaymentResultListener,
    PaytmPaymentTransactionCallback {
    @SuppressLint("StaticFieldLeak")
    lateinit var toolbar: Toolbar
    lateinit var activity: Activity
    lateinit var session: Session
    private lateinit var razorPayId: String
    lateinit var toolbarTitle: TextView
    private lateinit var imageMenu: ImageView
    private lateinit var imageHome: ImageView
    private lateinit var cardViewHamburger: CardView
    private var paymentMethod = ""
    private var pCode: String = ""
    lateinit var sendParams: MutableMap<String, String>
    private lateinit var paymentLyt: LinearLayout
    lateinit var deliveryTimeLyt: LinearLayout
    private lateinit var lytPayOption: LinearLayout
    private lateinit var processLyt: LinearLayout
    private lateinit var variantIdList: ArrayList<String>
    private lateinit var qtyList: ArrayList<String>
    lateinit var dateList: ArrayList<String>
    private lateinit var lytPayment: RadioGroup
    private lateinit var tvSubTotal: TextView
    lateinit var tvTotalItems: TextView
    private lateinit var tvSelectDeliveryDate: TextView
    private lateinit var tvWltBalance: TextView
    private lateinit var tvProceedOrder: TextView
    var subtotal = 0.0
    private var usedBalance = 0.0
    private var pCodeDiscount = 0.0
    var total = 0.0
    var delivery_charge = 0.0
    var wallet_balance = 0.0
    private lateinit var rbCOD: RadioButton
    private lateinit var rbPayU: RadioButton
    private lateinit var rbPayPal: RadioButton
    private lateinit var rbRazorPay: RadioButton
    private lateinit var rbPayStack: RadioButton
    private lateinit var rbFlutterWave: RadioButton
    private lateinit var rbMidTrans: RadioButton
    private lateinit var rbStripe: RadioButton
    private lateinit var rbPayTm: RadioButton
    private lateinit var rbSslCommerz: RadioButton
    private lateinit var rbBankTransfer: RadioButton
    private lateinit var bookingDates: ArrayList<BookingDate>
    private lateinit var confirmLyt: RelativeLayout
    private lateinit var lytWallet: RelativeLayout
    private lateinit var recyclerViewDates: RecyclerView
    lateinit var StartDate: Calendar
    lateinit var EndDate: Calendar
    private lateinit var scrollPaymentLyt: ScrollView
    lateinit var slotList: ArrayList<Slot>
    private lateinit var dateAdapter: DateAdapter
    var mYear = 0
    var mMonth = 0
    var mDay = 0
    var address: String = ""
    private var area_id: String = ""
    lateinit var chWallet: CheckBox
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var lytAddress: LinearLayout
    lateinit var from: String
    private var doubleBackToExitPressedOnce = false

    @SuppressLint("NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        activity = this@PaymentActivity
        session = Session(activity)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        imageMenu = findViewById(R.id.imageMenu)
        imageHome = findViewById(R.id.imageHome)
        cardViewHamburger = findViewById(R.id.cardViewHamburger)
        lytAddress = findViewById(R.id.lytAddress)
        lytProgressBar = findViewById(R.id.lytProgressBar)
        imageHome.visibility = View.GONE
        imageMenu.visibility = View.VISIBLE
        toolbarTitle.text = getString(R.string.payment)
        total = intent.getDoubleExtra("total", 0.0)
        subtotal = intent.getDoubleExtra("subtotal", 0.0)
        pCodeDiscount = intent.getDoubleExtra("pCodeDiscount", 0.0)
        delivery_charge = intent.getDoubleExtra("delivery_charge", 0.0)
        pCode = intent.getStringExtra("pCode").toString()
        address = intent.getStringExtra("address").toString()
        area_id = intent.getStringExtra("area_id").toString()
        variantIdList = intent.getStringArrayListExtra("variantIdList") as ArrayList<String>
        qtyList = intent.getStringArrayListExtra("qtyList") as ArrayList<String>
        from = intent.getStringExtra("from").toString()
        getAllWidget()
    }

    private fun getAllWidget() {
        try {
            recyclerView = findViewById(R.id.recyclerView)
            rbPayTm = findViewById(R.id.rbPayTm)
            rbSslCommerz = findViewById(R.id.rbSslCommerz)
            rbPayStack = findViewById(R.id.rbPayStack)
            rbFlutterWave = findViewById(R.id.rbFlutterWave)
            rbCOD = findViewById(R.id.rbCOD)
            lytPayment = findViewById(R.id.lytPayment)
            rbPayU = findViewById(R.id.rbPayU)
            rbPayPal = findViewById(R.id.rbPayPal)
            rbRazorPay = findViewById(R.id.rbRazorPay)
            rbMidTrans = findViewById(R.id.rbMidTrans)
            rbStripe = findViewById(R.id.rbStripe)
            rbBankTransfer = findViewById(R.id.rbBankTransfer)
            chWallet = findViewById(R.id.chWallet)
            lytPayOption = findViewById(R.id.lytPayOption)
            lytWallet = findViewById(R.id.lytWallet)
            paymentLyt = findViewById(R.id.paymentLyt)
            tvProceedOrder = findViewById(R.id.tvProceedOrder)
            processLyt = findViewById(R.id.processLyt)
            tvSelectDeliveryDate = findViewById(R.id.tvSelectDeliveryDate)
            deliveryTimeLyt = findViewById(R.id.deliveryTimeLyt)
            recyclerViewDates = findViewById(R.id.recyclerViewDates)
            tvSubTotal = findViewById(R.id.tvSubTotal)
            tvTotalItems = findViewById(R.id.tvTotalItems)
            confirmLyt = findViewById(R.id.confirmLyt)
            scrollPaymentLyt = findViewById(R.id.scrollPaymentLyt)
            tvWltBalance = findViewById(R.id.tvWltBalance)
            mShimmerViewContainer = findViewById(R.id.mShimmerViewContainer)
            lytAddress = findViewById(R.id.lytAddress)
            wallet_balance = session.getData(Constant.WALLET_BALANCE).toDouble()
            lytAddress.visibility = if (from == "cart") View.GONE else View.VISIBLE
            processLyt.weightSum = (if (from == "cart") 2 else 3.toFloat()) as Float
            cardViewHamburger.setOnClickListener { onBackPressed() }
            tvSubTotal.text = session.getData(Constant.currency) + stringFormat("" + subtotal)
            tvTotalItems.text = Constant.TOTAL_CART_ITEM.toString() + " Items"
            if (isConnected(activity)) {
                getWalletBalance(activity, session)
                GetPaymentConfig()
                chWallet.tag = "false"
                tvWltBalance.text = "Total Balance: " + session.getData(Constant.currency) + stringFormat(
                    "" + wallet_balance
                )
                if (wallet_balance == 0.0) {
                    lytWallet.visibility = View.GONE
                } else {
                    lytWallet.visibility = View.VISIBLE
                }
                tvProceedOrder.setOnClickListener {
                    try {
                        if (deliveryDay.isEmpty()) {
                            Toast.makeText(
                                activity,
                                getString(R.string.select_delivery_day),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (deliveryTime.isEmpty()) {
                            Toast.makeText(
                                activity,
                                getString(R.string.select_delivery_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (paymentMethod.isEmpty()) {
                            Toast.makeText(
                                activity,
                                getString(R.string.select_payment_method),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            PlaceOrderProcess()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                chWallet.setOnClickListener {
                    if (chWallet.tag == "false") {
                        chWallet.isChecked = true
                        lytWallet.visibility = View.VISIBLE
                        if (wallet_balance >= subtotal) {
                            usedBalance = subtotal
                            tvWltBalance.text =
                                getString(R.string.remaining_wallet_balance) + session.getData(
                                    Constant.currency
                                ) + stringFormat("" + (wallet_balance - usedBalance))
                            paymentMethod = Constant.WALLET
                            lytPayOption.visibility = View.GONE
                        } else {
                            usedBalance = wallet_balance
                            tvWltBalance.text =
                                getString(R.string.remaining_wallet_balance) + session.getData(
                                    Constant.currency
                                ) + "0.00"
                            lytPayOption.visibility = View.VISIBLE
                        }
                        subtotal -= usedBalance
                        tvSubTotal.text =
                            session.getData(Constant.currency) + stringFormat("" + subtotal)
                        chWallet.tag = "true"
                    } else {
                        walletUncheck()
                    }
                }
            }
            confirmLyt.visibility = View.VISIBLE
            scrollPaymentLyt.visibility = View.VISIBLE
            lytPayment.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
                try {
                    val rb = findViewById<RadioButton>(checkedId)
                    paymentMethod = rb.tag.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun GetPaymentConfig() {
        recyclerView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SETTINGS] = Constant.GetVal
        params[Constant.GET_PAYMENT_METHOD] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (jsonObject.has(Constant.PAYMENT_METHODS)) {
                                val jsonObject = jsonObject.getJSONObject(Constant.PAYMENT_METHODS)
                                if (jsonObject.has(Constant.cod_payment_method)) {
                                    Constant.COD = jsonObject.getString(Constant.cod_payment_method)
                                    Constant.COD_MODE = jsonObject.getString(Constant.cod_mode)
                                }
                                if (jsonObject.has(Constant.payu_method)) {
                                    Constant.PAYUMONEY = jsonObject.getString(Constant.payu_method)
                                    Constant.PAYUMONEY_MODE =
                                        jsonObject.getString(Constant.payumoney_mode)
                                    Constant.MERCHANT_KEY = jsonObject.getString(Constant.PAY_M_KEY)
                                    Constant.MERCHANT_ID = jsonObject.getString(Constant.PAYU_M_ID)
                                    Constant.MERCHANT_SALT =
                                        jsonObject.getString(Constant.PAYU_SALT)
                                    ApiConfig.setAppEnvironment(activity)
                                }
                                if (jsonObject.has(Constant.razor_pay_method)) {
                                    Constant.RAZORPAY =
                                        jsonObject.getString(Constant.razor_pay_method)
                                    Constant.RAZOR_PAY_KEY_VALUE =
                                        jsonObject.getString(Constant.RAZOR_PAY_KEY)
                                }
                                if (jsonObject.has(Constant.paypal_method)) {
                                    Constant.PAYPAL = jsonObject.getString(Constant.paypal_method)
                                }
                                if (jsonObject.has(Constant.paystack_method)) {
                                    Constant.PAY_STACK =
                                        jsonObject.getString(Constant.paystack_method)
                                    Constant.PAY_STACK_KEY =
                                        jsonObject.getString(Constant.pay_stack_public_key)
                                }
                                if (jsonObject.has(Constant.flutter_wave_payment_method)) {
                                    Constant.FLUTTER_WAVE =
                                        jsonObject.getString(Constant.flutter_wave_payment_method)
                                    Constant.FLUTTER_WAVE_ENCRYPTION_KEY_VAL =
                                        jsonObject.getString(Constant.flutter_wave_encryption_key)
                                    Constant.FLUTTER_WAVE_PUBLIC_KEY_VAL =
                                        jsonObject.getString(Constant.flutter_wave_public_key)
                                    Constant.FLUTTER_WAVE_SECRET_KEY_VAL =
                                        jsonObject.getString(Constant.flutter_wave_secret_key)
                                    Constant.FLUTTER_WAVE_SECRET_KEY_VAL =
                                        jsonObject.getString(Constant.flutter_wave_secret_key)
                                    Constant.FLUTTER_WAVE_CURRENCY_CODE_VAL =
                                        jsonObject.getString(Constant.flutter_wave_currency_code)
                                }
                                if (jsonObject.has(Constant.midtrans_payment_method)) {
                                    Constant.MIDTRANS =
                                        jsonObject.getString(Constant.midtrans_payment_method)
                                }
                                if (jsonObject.has(Constant.stripe_payment_method)) {
                                    Constant.STRIPE =
                                        jsonObject.getString(Constant.stripe_payment_method)
                                }
                                if (jsonObject.has(Constant.paytm_payment_method)) {
                                    Constant.PAYTM =
                                        jsonObject.getString(Constant.paytm_payment_method)
                                    Constant.PAYTM_MERCHANT_ID =
                                        jsonObject.getString(Constant.paytm_merchant_id)
                                    Constant.PAYTM_MERCHANT_KEY =
                                        jsonObject.getString(Constant.paytm_merchant_key)
                                    Constant.PAYTM_MODE = jsonObject.getString(Constant.paytm_mode)
                                }
                                if (jsonObject.has(Constant.ssl_method)) {
                                    Constant.SSLECOMMERZ = jsonObject.getString(Constant.ssl_method)
                                    Constant.SSLECOMMERZ_MODE =
                                        jsonObject.getString(Constant.ssl_mode)
                                    Constant.SSLECOMMERZ_STORE_ID =
                                        jsonObject.getString(Constant.ssl_store_id)
                                    Constant.SSLECOMMERZ_SECRET_KEY =
                                        jsonObject.getString(Constant.ssl_store_password)
                                }
                                if (jsonObject.has(Constant.ssl_method)) {
                                    Constant.SSLECOMMERZ = jsonObject.getString(Constant.ssl_method)
                                    Constant.SSLECOMMERZ_MODE =
                                        jsonObject.getString(Constant.ssl_mode)
                                    Constant.SSLECOMMERZ_STORE_ID =
                                        jsonObject.getString(Constant.ssl_store_id)
                                    Constant.SSLECOMMERZ_SECRET_KEY =
                                        jsonObject.getString(Constant.ssl_store_password)
                                }
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
                                setPaymentMethod()
                            } else {
                                mShimmerViewContainer.stopShimmer()
                                mShimmerViewContainer.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                Toast.makeText(
                                    activity,
                                    getString(R.string.alert_payment_methods_blank),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: JSONException) {
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false)
    }

    fun setPaymentMethod() {
        if (subtotal > 0) {
            if (Constant.DIRECT_BANK_TRANSFER == "0" && Constant.FLUTTER_WAVE == "0" && Constant.PAYPAL == "0" && Constant.PAYUMONEY == "0" && Constant.COD == "0" && Constant.RAZORPAY == "0" && Constant.PAY_STACK == "0" && Constant.MIDTRANS == "0" && Constant.STRIPE == "0" && Constant.PAYTM == "0") {
                lytPayOption.visibility = View.GONE
            } else {
                lytPayOption.visibility = View.VISIBLE
                if (Constant.COD == "1") {
                    if (Constant.COD_MODE == Constant.product && !Constant.isCODAllow) {
                        rbCOD.visibility = View.GONE
                    } else {
                        rbCOD.visibility = View.VISIBLE
                    }
                }
                if (Constant.PAYUMONEY == "1") {
                    rbPayU.visibility = View.VISIBLE
                }
                if (Constant.RAZORPAY == "1") {
                    rbRazorPay.visibility = View.VISIBLE
                    val checkout = Checkout()
                    checkout.setKeyID(Constant.RAZOR_PAY_KEY_VALUE)
                    Checkout.preload(Objects.requireNonNull(activity))
                }
                if (Constant.PAY_STACK == "1") {
                    rbPayStack.visibility = View.VISIBLE
                }
                if (Constant.FLUTTER_WAVE == "1") {
                    rbFlutterWave.visibility = View.VISIBLE
                }
                if (Constant.PAYPAL == "1") {
                    rbPayPal.visibility = View.VISIBLE
                }
                if (Constant.MIDTRANS == "1") {
                    rbMidTrans.visibility = View.VISIBLE
                }
                if (Constant.STRIPE == "1") {
                    rbStripe.visibility = View.VISIBLE
                }
                if (Constant.PAYTM == "1") {
                    rbPayTm.visibility = View.VISIBLE
                }
                if (Constant.SSLECOMMERZ == "1") {
                    rbSslCommerz.visibility = View.VISIBLE
                }
                if (Constant.SSLECOMMERZ == "1") {
                    rbSslCommerz.visibility = View.VISIBLE
                }
                if (Constant.DIRECT_BANK_TRANSFER == "1") {
                    rbBankTransfer.visibility = View.VISIBLE
                }
            }
            GetTimeSlotConfig(session, activity)
        } else {
            lytWallet.visibility = View.GONE
            lytPayOption.visibility = View.GONE
            mShimmerViewContainer.stopShimmer()
            mShimmerViewContainer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    fun walletUncheck() {
        paymentMethod = ""
        lytPayOption.visibility = View.VISIBLE
        tvWltBalance.text =
            getString(R.string.total) + session.getData(Constant.currency) + wallet_balance
        subtotal += usedBalance
        tvSubTotal.text =
            session.getData(Constant.currency) + stringFormat("" + subtotal)
        chWallet.isChecked = false
        chWallet.tag = "false"
    }

    private fun GetTimeSlotConfig(session: Session, activity: Activity) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SETTINGS] = Constant.GetVal
        params[Constant.GET_TIME_SLOT_CONFIG] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            val jsonObject = JSONObject(
                                jsonObject1.getJSONObject(Constant.TIME_SLOT_CONFIG).toString()
                            )
                            session.setData(
                                Constant.IS_TIME_SLOTS_ENABLE,
                                jsonObject.getString(Constant.IS_TIME_SLOTS_ENABLE)
                            )
                            session.setData(
                                Constant.DELIVERY_STARTS_FROM,
                                jsonObject.getString(Constant.DELIVERY_STARTS_FROM)
                            )
                            session.setData(
                                Constant.ALLOWED_DAYS,
                                jsonObject.getString(Constant.ALLOWED_DAYS)
                            )
                            if (session.getData(Constant.IS_TIME_SLOTS_ENABLE) == Constant.GetVal) {
                                deliveryTimeLyt.visibility = View.VISIBLE
                                StartDate = Calendar.getInstance()
                                EndDate = Calendar.getInstance()
                                mYear = StartDate.get(Calendar.YEAR)
                                mMonth = StartDate.get(Calendar.MONTH)
                                mDay = StartDate.get(Calendar.DAY_OF_MONTH)
                                val deliveryStartFrom =
                                    session.getData(Constant.DELIVERY_STARTS_FROM)
                                        .toInt() - 1
                                val deliveryAllowFrom = session.getData(Constant.ALLOWED_DAYS)
                                    .toInt()
                                StartDate.add(Calendar.DATE, deliveryStartFrom)
                                EndDate.add(
                                    Calendar.DATE,
                                    deliveryStartFrom + (deliveryAllowFrom - 1)
                                )
                                dateList = getDates(
                                    StartDate.get(Calendar.DATE).toString() + "-" + (StartDate.get(
                                        Calendar.MONTH
                                    ) + 1) + "-" + StartDate.get(Calendar.YEAR), EndDate.get(
                                        Calendar.DATE
                                    )
                                        .toString() + "-" + (EndDate.get(Calendar.MONTH) + 1) + "-" + EndDate.get(
                                        Calendar.YEAR
                                    )
                                )
                                setDateList(dateList)
                                GetTimeSlots()
                            } else {
                                deliveryTimeLyt.visibility = View.GONE
                                deliveryDay = "Date : N/A"
                                deliveryTime = "Time : N/A"
                                mShimmerViewContainer.stopShimmer()
                                mShimmerViewContainer.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                            }
                        }
                    } catch (e: JSONException) {
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false)
    }

    fun GetTimeSlots() {
        slotList = ArrayList()
        val params: MutableMap<String, String> = HashMap()
        params["get_time_slots"] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val jsonArray = jsonObject.getJSONArray("time_slots")
                            for (i in 0 until jsonArray.length()) {
                                val object1 = jsonArray.getJSONObject(i)
                                slotList.add(
                                    Slot(
                                        object1.getString("id"),
                                        object1.getString("title"),
                                        object1.getString("last_order_time")
                                    )
                                )
                            }
                            recyclerView.layoutManager = LinearLayoutManager(activity)
                            adapter = SlotAdapter(activity, slotList)
                            recyclerView.adapter = adapter
                            mShimmerViewContainer.stopShimmer()
                            mShimmerViewContainer.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, true)
    }

    @JvmName("setDateList1")
    private fun setDateList(datesList: ArrayList<String>) {
        bookingDates = ArrayList()
        for (i in datesList.indices) {
            val date = datesList[i].split("-".toRegex()).toTypedArray()
            val bookingDate1 = BookingDate()
            bookingDate1.date = date[0]
            bookingDate1.month = date[1]
            bookingDate1.year = date[2]
            bookingDate1.day = date[3]
            bookingDates.add(bookingDate1)
        }
        dateAdapter = DateAdapter(activity, bookingDates)
        recyclerViewDates.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDates.adapter = dateAdapter
    }

    @SuppressLint("SetTextI18n")
    fun PlaceOrderProcess() {
        try {
            val alertDialog = AlertDialog.Builder(
                activity
            )
            val inflater = activity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_order_confirm, null)
            alertDialog.setView(dialogView)
            alertDialog.setCancelable(true)
            val dialog = alertDialog.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val lytDialogPromo: LinearLayout = dialogView.findViewById(R.id.lytDialogPromo)
            val lytDialogWallet: LinearLayout = dialogView.findViewById(R.id.lytDialogWallet)
            val tvDialogItemTotal: TextView = dialogView.findViewById(R.id.tvDialogItemTotal)
            val tvDialogDeliveryCharge: TextView = dialogView.findViewById(R.id.tvDialogDeliveryCharge)
            val tvDialogTotal: TextView = dialogView.findViewById(R.id.tvDialogTotal)
            val tvDialogPCAmount: TextView = dialogView.findViewById(R.id.tvDialogPCAmount)
            val tvDialogWallet: TextView = dialogView.findViewById(R.id.tvDialogWallet)
            val tvDialogFinalTotal: TextView = dialogView.findViewById(R.id.tvDialogFinalTotal)
            val tvDialogCancel: TextView = dialogView.findViewById(R.id.tvDialogCancel)
            val tvDialogConfirm: TextView = dialogView.findViewById(R.id.tvDialogConfirm)
            val tvSpecialNote: EditText = dialogView.findViewById(R.id.tvSpecialNote)
            if (pCodeDiscount > 0) {
                lytDialogPromo.visibility = View.VISIBLE
                tvDialogPCAmount.text = "- " + session.getData(Constant.currency) + pCodeDiscount
            } else {
                lytDialogPromo.visibility = View.GONE
            }
            if (chWallet.tag.toString() == "true") {
                lytDialogWallet.visibility = View.VISIBLE
                tvDialogWallet.text = "- " + session.getData(Constant.currency) + usedBalance
            } else {
                lytDialogWallet.visibility = View.GONE
            }
            tvDialogItemTotal.text = session.getData(Constant.currency) + stringFormat("" + total)
            tvDialogDeliveryCharge.text =
                if (delivery_charge > 0) session.getData(Constant.currency) + stringFormat("" + delivery_charge) else getString(
                    R.string.free
                )
            tvDialogTotal.text =
                session.getData(Constant.currency) + stringFormat("" + (total + delivery_charge))
            tvDialogFinalTotal.text =
                session.getData(Constant.currency) + stringFormat("" + subtotal)
            tvDialogConfirm.setOnClickListener {
                showProgressDialog()
                sendParams = HashMap()
                sendParams[Constant.PLACE_ORDER] = Constant.GetVal
                if (session.getBoolean(Constant.IS_USER_LOGIN)) sendParams[Constant.USER_ID] =
                    session.getData(
                        Constant.ID
                    )
                sendParams[Constant.TOTAL] = "" + total
                sendParams[Constant.FINAL_TOTAL] = "" + subtotal
                sendParams[Constant.PRODUCT_VARIANT_ID] = variantIdList.toString()
                sendParams[Constant.QUANTITY] = qtyList.toString()
                sendParams[Constant.MOBILE] = session.getData(Constant.MOBILE)
                sendParams[Constant.LOCAL_PICKUP] = if (from == "cart") "1" else "0"
                if (from == "cart" || paymentMethod == getString(R.string.bank_transfer)) {
                    sendParams[Constant.STATUS] = Constant.AWAITING_PAYMENT
                }
                sendParams[Constant.DELIVERY_CHARGE] = "" + delivery_charge
                sendParams[Constant.DELIVERY_TIME] = "$deliveryDay - $deliveryTime"
                sendParams[Constant.KEY_WALLET_USED] = chWallet.tag.toString()
                sendParams[Constant.WALLET_BALANCE] = usedBalance.toString()
                sendParams[Constant.PAYMENT_METHOD] =
                    if (paymentMethod == getString(R.string.bank_transfer)) "bank transfer" else paymentMethod
                if (pCode != "") {
                    sendParams[Constant.PROMO_CODE] = pCode
                    sendParams[Constant.PROMO_DISCOUNT] = stringFormat("" + pCodeDiscount)
                }
                sendParams[Constant.ADDRESS] = if (address == null) "" else address
                if (area_id != "") sendParams[Constant.AREA_ID] = area_id
                sendParams[Constant.LONGITUDE] = session.getCoordinates(Constant.LONGITUDE)
                sendParams[Constant.LATITUDE] = session.getCoordinates(Constant.LATITUDE)
                sendParams[Constant.EMAIL] = session.getData(Constant.EMAIL)
                sendParams[Constant.ORDER_NOTE] = tvSpecialNote.text.toString()
                if (paymentMethod == resources.getString(R.string.cash_on_delivery) || paymentMethod == getString(
                        R.string.wallet_type
                    ) || paymentMethod == getString(R.string.bank_transfer)
                ) {
                    requestToVolley(object : VolleyCallback {
                        override fun onSuccess(result: Boolean, response: String) {
                            if (result) {
                                try {
                                    val jsonObject = JSONObject(response)
                                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                                        if (chWallet.tag.toString() == "true") {
                                            getWalletBalance(activity, session)
                                        }
                                        hideProgressDialog()
                                        val intent = Intent(activity, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.putExtra(Constant.FROM, "payment_success")
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        hideProgressDialog()
                                        Toast.makeText(
                                            activity,
                                            jsonObject.getString(Constant.MESSAGE),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }, activity, Constant.ORDER_PROCESS_URL, sendParams, false)
                    dialog.dismiss()
                } else {
                    sendParams[Constant.USER_NAME] = session.getData(Constant.NAME)
                    if (paymentMethod == getString(R.string.pay_u)) {
                        dialog.dismiss()
                        sendParams[Constant.MOBILE] = session.getData(Constant.MOBILE)
                        sendParams[Constant.USER_NAME] = session.getData(Constant.NAME)
                        sendParams[Constant.EMAIL] = session.getData(Constant.EMAIL)
                        PaymentModelClass(activity).OnPayClick(
                            activity,
                            sendParams,
                            Constant.PAYMENT,
                            sendParams[Constant.FINAL_TOTAL].toString()
                        )
                    } else if (paymentMethod == getString(R.string.paypal)) {
                        dialog.dismiss()
                        sendParams[Constant.FROM] = Constant.PAYMENT
                        sendParams[Constant.STATUS] = Constant.AWAITING_PAYMENT
                        placeOrder(
                            activity,
                            getString(R.string.midtrans),
                            System.currentTimeMillis().toString() + randomNumeric(3),
                            true,
                            sendParams,
                            "paypal"
                        )
                    } else if (paymentMethod == getString(R.string.razor_pay)) {
                        dialog.dismiss()
                        createOrderId(stringFormat("" + subtotal).toDouble())
                    } else if (paymentMethod == getString(R.string.paystack)) {
                        dialog.dismiss()
                        sendParams[Constant.FROM] = Constant.PAYMENT
                        val intent = Intent(activity, PayStackActivity::class.java)
                        intent.putExtra(Constant.PARAMS, sendParams as Serializable)
                        startActivity(intent)
                    } else if (paymentMethod == getString(R.string.midtrans)) {
                        dialog.dismiss()
                        sendParams[Constant.FROM] = Constant.PAYMENT
                        sendParams[Constant.STATUS] = Constant.AWAITING_PAYMENT
                        placeOrder(
                            activity,
                            getString(R.string.midtrans),
                            System.currentTimeMillis().toString() + randomNumeric(3),
                            true,
                            sendParams,
                            "midtrans"
                        )
                    } else if (paymentMethod == getString(R.string.stripe)) {
                        dialog.dismiss()
                        sendParams[Constant.FROM] = Constant.PAYMENT
                        sendParams[Constant.STATUS] = Constant.AWAITING_PAYMENT
                        placeOrder(
                            activity,
                            getString(R.string.stripe),
                            System.currentTimeMillis().toString() + randomNumeric(3),
                            true,
                            sendParams,
                            "stripe"
                        )
                    } else if (paymentMethod == getString(R.string.flutterwave)) {
                        dialog.dismiss()
                        startFlutterWavePayment()
                    } else if (paymentMethod == getString(R.string.paytm)) {
                        dialog.dismiss()
                        startPayTmPayment()
                    } else if (paymentMethod == getString(R.string.sslecommerz)) {
                        dialog.dismiss()
                        startSslCommerzPayment(
                            activity,
                            sendParams[Constant.FINAL_TOTAL].toString(),
                            System.currentTimeMillis().toString() + randomNumeric(3),
                            sendParams
                        )
                    }
                }
            }
            tvDialogCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressDialog() {
        try {
            lytProgressBar.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideProgressDialog() {
        if (lytProgressBar != null && lytProgressBar.visibility == View.VISIBLE) {
            lytProgressBar.visibility = View.GONE
        }
    }

    private fun startSslCommerzPayment(
        activity: Activity,
        amount: String,
        transId: String,
        sendParams: MutableMap<String, String>
    ) {
        val mode: String = if (Constant.SSLECOMMERZ_MODE == "sandbox") {
            SdkType.TESTBOX
        } else {
            SdkType.LIVE
        }
        val mandatoryFieldModel = MandatoryFieldModel(
            Constant.SSLECOMMERZ_STORE_ID,
            Constant.SSLECOMMERZ_SECRET_KEY,
            amount,
            transId,
            CurrencyType.BDT,
            mode,
            SdkCategory.BANK_LIST
        )

        /* Call for the payment */PayUsingSSLCommerz.getInstance()
            .setData(activity, mandatoryFieldModel, object : OnPaymentResultListener {
                override fun transactionSuccess(transactionInfo: TransactionInfo) {
                    // If payment is success and risk label is 0.
                    placeOrder(
                        activity,
                        getString(R.string.sslecommerz),
                        transactionInfo.tranId,
                        true,
                        sendParams,
                        "SSLECOMMERZ"
                    )
                }

                override fun transactionFail(sessionKey: String) {
                    hideProgressDialog()
                    Toast.makeText(activity, sessionKey, Toast.LENGTH_LONG).show()
                }

                override fun error(errorCode: Int) {
                    hideProgressDialog()
                    when (errorCode) {
                        ErrorKeys.USER_INPUT_ERROR -> Toast.makeText(
                            activity,
                            activity.getString(R.string.user_input_error),
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.INTERNET_CONNECTION_ERROR -> Toast.makeText(
                            activity,
                            activity.getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.DATA_PARSING_ERROR -> Toast.makeText(
                            activity,
                            activity.getString(R.string.data_parsing_error),
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.CANCEL_TRANSACTION_ERROR -> Toast.makeText(
                            activity,
                            activity.getString(R.string.user_cancel_transaction_error),
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.SERVER_ERROR -> Toast.makeText(
                            activity,
                            activity.getString(R.string.server_error),
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.NETWORK_ERROR -> Toast.makeText(
                            activity,
                            activity.getString(R.string.network_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    private fun createOrderId(payable: Double) {
        val params: MutableMap<String, String> = HashMap()
        params["amount"] = "" + payable.roundToLong() + "00"
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            startPayment(jsonObject.getString("id"), jsonObject.getString("amount"))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.Get_RazorPay_OrderId, params, true)
    }

    fun startPayment(orderId: String, payAmount: String) {
        val checkout = Checkout()
        checkout.setKeyID(Constant.RAZOR_PAY_KEY_VALUE)
        checkout.setImage(R.mipmap.ic_launcher)
        try {
            val options = JSONObject()
            options.put(Constant.NAME, session.getData(Constant.NAME))
            options.put(Constant.ORDER_ID, orderId)
            options.put(Constant.CURRENCY, "INR")
            options.put(Constant.AMOUNT, payAmount)
            val preFill = JSONObject()
            preFill.put(Constant.EMAIL, session.getData(Constant.EMAIL))
            preFill.put(Constant.CONTACT, session.getData(Constant.MOBILE))
            options.put("prefill", preFill)
            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.d(tag, "Error in starting Razorpay Checkout", e)
        }
    }

    fun placeOrder(
        activity: Activity,
        paymentType: String,
        txnid: String,
        isSuccess: Boolean,
        sendParams: MutableMap<String, String>,
        status: String
    ) {
        if (isSuccess) {
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                when (status) {
                                    "stripe" -> CreateStripePayment(jsonObject.getString(Constant.ORDER_ID))
                                    "midtrans" -> createMidtransPayment(
                                        jsonObject.getString(Constant.ORDER_ID),
                                        stringFormat("" + subtotal).split("\\.".toRegex())
                                            .toTypedArray()[0]
                                    )
                                    "paypal" -> startPayPalPayment(sendParams)
                                    else -> {
                                        addTransaction(
                                            activity,
                                            jsonObject.getString(Constant.ORDER_ID),
                                            paymentType,
                                            txnid,
                                            status,
                                            activity.getString(R.string.order_success),
                                            sendParams
                                        )
                                        val intent = Intent(activity, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.putExtra(Constant.FROM, "payment_success")
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, sendParams, false)
        } else {
            addTransaction(
                activity,
                "",
                getString(R.string.razor_pay),
                txnid,
                status,
                getString(R.string.order_failed),
                sendParams
            )
        }
    }

    fun createMidtransPayment(orderId: String, grossAmount: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.ORDER_ID] = orderId
        params[Constant.GROSS_AMOUNT] = grossAmount.split(",".toRegex()).toTypedArray()[0]
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val intent = Intent(activity, MidtransActivity::class.java)
                            intent.putExtra(
                                Constant.URL, jsonObject.getJSONObject(Constant.DATA).getString(
                                    Constant.REDIRECT_URL
                                )
                            )
                            intent.putExtra(Constant.ORDER_ID, orderId)
                            intent.putExtra(Constant.FROM, Constant.PAYMENT)
                            intent.putExtra(Constant.PARAMS, sendParams as Serializable)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                activity,
                                jsonObject.getString(Constant.MESSAGE),
                                Toast.LENGTH_SHORT
                            ).show()
                            hideProgressDialog()
                        }
                    } catch (e: JSONException) {
                        hideProgressDialog()
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.MIDTRANS_PAYMENT_URL, params, true)
    }

    fun CreateStripePayment(orderId: String) {
        val intent = Intent(activity, StripeActivity::class.java)
        intent.putExtra(Constant.ORDER_ID, orderId)
        intent.putExtra(Constant.FROM, Constant.PAYMENT)
        intent.putExtra(Constant.PARAMS, sendParams as Serializable)
        startActivity(intent)
    }

    fun addTransaction(
        activity: Activity,
        orderId: String,
        paymentType: String,
        txnid: String,
        status: String,
        message: String,
        sendParams: MutableMap<String, String>
    ) {
        val transactionParams: MutableMap<String, String> = HashMap()
        transactionParams[Constant.ADD_TRANSACTION] = Constant.GetVal
        transactionParams[Constant.USER_ID] = sendParams[Constant.USER_ID].toString()
        transactionParams[Constant.ORDER_ID] = orderId
        transactionParams[Constant.TYPE] = paymentType
        transactionParams[Constant.TRANS_ID] = txnid
        transactionParams[Constant.AMOUNT] = sendParams[Constant.FINAL_TOTAL].toString()
        transactionParams[Constant.STATUS] = status
        transactionParams[Constant.MESSAGE] = message
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        transactionParams["transaction_date"] = df.format(c)
        requestToVolley(
            object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {

                }
            },
            activity,
            Constant.ORDER_PROCESS_URL,
            transactionParams,
            false
        )
    }

    fun startPayPalPayment(sendParams: MutableMap<String, String>) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.FIRST_NAME] = sendParams[Constant.USER_NAME].toString()
        params[Constant.LAST_NAME] = sendParams[Constant.USER_NAME].toString()
        params[Constant.PAYER_EMAIL] = sendParams[Constant.EMAIL].toString()
        params[Constant.ITEM_NAME] = "Card Order"
        params[Constant.ITEM_NUMBER] = System.currentTimeMillis().toString() + randomNumeric(3)
        params[Constant.AMOUNT] = sendParams[Constant.FINAL_TOTAL].toString()
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                val intent = Intent(activity, PayPalWebActivity::class.java)
                intent.putExtra(Constant.URL, response)
                intent.putExtra(Constant.ORDER_ID, params[Constant.ITEM_NUMBER])
                intent.putExtra(Constant.FROM, Constant.PAYMENT)
                intent.putExtra(Constant.PARAMS, sendParams as Serializable)
                startActivity(intent)
            }
        }, activity, Constant.PAPAL_URL, params, true)
    }

    private fun startPayTmPayment() {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.ORDER_ID_] = randomAlphaNumeric(20)
        params[Constant.CUST_ID] = randomAlphaNumeric(10)
        params[Constant.TXN_AMOUNT] = "" + stringFormat("" + subtotal)
        if (Constant.PAYTM_MODE == "sandbox") {
            params[Constant.INDUSTRY_TYPE_ID] = Constant.INDUSTRY_TYPE_ID_DEMO_VAL
            params[Constant.CHANNEL_ID] = Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL
            params[Constant.WEBSITE] = Constant.WEBSITE_DEMO_VAL
        } else if (Constant.PAYTM_MODE == "production") {
            params[Constant.INDUSTRY_TYPE_ID] = Constant.INDUSTRY_TYPE_ID_LIVE_VAL
            params[Constant.CHANNEL_ID] = Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL
            params[Constant.WEBSITE] = Constant.WEBSITE_LIVE_VAL
        }

//        System.out.println("====" + params.toString());
        requestToVolley(object : VolleyCallback, PaytmPaymentTransactionCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        val jsonObject = jsonObject1.getJSONObject(Constant.DATA)
                        //                    System.out.println("=======res  " + response);
                        lateinit var service: PaytmPGService
                        if (Constant.PAYTM_MODE == "sandbox") {
                            service =
                                PaytmPGService.getStagingService(Constant.PAYTM_ORDER_PROCESS_DEMO_VAL)
                        } else if (Constant.PAYTM_MODE == "production") {
                            service = PaytmPGService.getProductionService()
                        }
                        customerId = jsonObject.getString(Constant.CUST_ID)
                        //creating a hashmap and adding all the values required
                        val paramMap = HashMap<String, String>()
                        paramMap[Constant.MID] = Constant.PAYTM_MERCHANT_ID
                        paramMap[Constant.ORDER_ID_] = jsonObject1.getString("order id")
                        paramMap[Constant.CUST_ID] = jsonObject.getString(Constant.CUST_ID)
                        paramMap[Constant.TXN_AMOUNT] = stringFormat("" + subtotal)
                        if (Constant.PAYTM_MODE == "sandbox") {
                            paramMap[Constant.INDUSTRY_TYPE_ID] = Constant.INDUSTRY_TYPE_ID_DEMO_VAL
                            paramMap[Constant.CHANNEL_ID] = Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL
                            paramMap[Constant.WEBSITE] = Constant.WEBSITE_DEMO_VAL
                        } else if (Constant.PAYTM_MODE == "production") {
                            paramMap[Constant.INDUSTRY_TYPE_ID] = Constant.INDUSTRY_TYPE_ID_LIVE_VAL
                            paramMap[Constant.CHANNEL_ID] = Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL
                            paramMap[Constant.WEBSITE] = Constant.WEBSITE_LIVE_VAL
                        }
                        paramMap[Constant.CALLBACK_URL] =
                            jsonObject.getString(Constant.CALLBACK_URL)
                        paramMap[Constant.CHECK_SUM_HASH] = jsonObject1.getString("signature")

                        //creating a paytm order object using the hashmap
                        val order = PaytmOrder(paramMap)
                        service.initialize(order, null)

                        //finally starting the payment transaction
                        service.startPaymentTransaction(activity, true, true, this)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onTransactionResponse(bundle: Bundle) {
                val orderId = bundle.getString(Constant.ORDERID)
                val status = bundle.getString(Constant.STATUS_)
                if (status.equals(Constant.TXN_SUCCESS, ignoreCase = true)) {
                    verifyTransaction(orderId.toString())
                }
            }

            override fun networkNotAvailable() {
                hideProgressDialog()
                Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
            }

            override fun clientAuthenticationFailed(s: String) {
                hideProgressDialog()
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
            }

            override fun someUIErrorOccurred(s: String) {
                hideProgressDialog()
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
            }

            override fun onErrorLoadingWebPage(i: Int, s: String, s1: String) {
                hideProgressDialog()
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
            }

            override fun onBackPressedCancelTransaction() {
                hideProgressDialog()
                Toast.makeText(activity, "Back Pressed", Toast.LENGTH_LONG).show()
            }

            override fun onTransactionCancel(s: String, bundle: Bundle) {
                hideProgressDialog()
                Toast.makeText(activity, s + bundle.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }, activity, Constant.GENERATE_PAYTM_CHECKSUM, params, false)
    }

    override fun onTransactionResponse(bundle: Bundle) {
        val orderId = bundle.getString(Constant.ORDERID)
        val status = bundle.getString(Constant.STATUS_)
        if (status.equals(Constant.TXN_SUCCESS, ignoreCase = true)) {
            verifyTransaction(orderId.toString())
        }
    }

    override fun networkNotAvailable() {
        hideProgressDialog()
        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
    }

    override fun clientAuthenticationFailed(s: String) {
        hideProgressDialog()
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
    }

    override fun someUIErrorOccurred(s: String) {
        hideProgressDialog()
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
    }

    override fun onErrorLoadingWebPage(i: Int, s: String, s1: String) {
        hideProgressDialog()
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressedCancelTransaction() {
        hideProgressDialog()
        Toast.makeText(activity, "Back Pressed", Toast.LENGTH_LONG).show()
    }

    override fun onTransactionCancel(s: String, bundle: Bundle) {
        hideProgressDialog()
        Toast.makeText(activity, s + bundle.toString(), Toast.LENGTH_LONG)
            .show()
    }

    /**
     * Verifying the transaction status once PayTM transaction is over
     * This makes server(own) -> server(PayTM) call to verify the transaction status
     */
    fun verifyTransaction(orderId: String) {
        val params: MutableMap<String, String> = HashMap()
        params["orderId"] = orderId
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getJSONObject("body").getJSONObject("resultInfo")
                            .getString("resultStatus")
                        if (status.equals("TXN_SUCCESS", ignoreCase = true)) {
                            val txnId = jsonObject.getJSONObject("body").getString("txnId")
                            placeOrder(
                                activity,
                                getString(R.string.paytm),
                                txnId,
                                true,
                                sendParams,
                                Constant.SUCCESS
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.VALID_TRANSACTION, params, false)
    }


    private fun startFlutterWavePayment() {
        RavePayManager(this)
            .setAmount(stringFormat("" + subtotal).toDouble())
            .setEmail(session.getData(Constant.EMAIL))
            .setCurrency(Constant.FLUTTER_WAVE_CURRENCY_CODE_VAL)
            .setfName(session.getData(Constant.FIRST_NAME))
            .setlName(session.getData(Constant.LAST_NAME))
            .setNarration(getString(R.string.app_name) + getString(R.string.shopping))
            .setPublicKey(Constant.FLUTTER_WAVE_PUBLIC_KEY_VAL)
            .setEncryptionKey(Constant.FLUTTER_WAVE_ENCRYPTION_KEY_VAL)
            .setTxRef(System.currentTimeMillis().toString() + "Ref")
            .acceptAccountPayments(true)
            .acceptCardPayments(true)
            .acceptAccountPayments(true)
            .acceptAchPayments(true)
            .acceptBankTransferPayments(true)
            .acceptBarterPayments(true)
            .acceptGHMobileMoneyPayments(true)
            .acceptRwfMobileMoneyPayments(true)
            .acceptSaBankPayments(true)
            .acceptFrancMobileMoneyPayments(true)
            .acceptZmMobileMoneyPayments(true)
            .acceptUssdPayments(true)
            .acceptUkPayments(true)
            .acceptMpesaPayments(true)
            .shouldDisplayFee(true)
            .onStagingEnv(false)
            .showStagingLabel(false)
            .initialize()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != RaveConstants.RAVE_REQUEST_CODE && data != null) {
            PaymentModelClass(activity).TransactionMethod(data, activity, Constant.PAYMENT)
        } else if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null && data.getStringExtra(
                "response"
            ) != null
        ) {
            try {
                val details = JSONObject(data.getStringExtra("response").toString())
                val jsonObject = details.getJSONObject(Constant.DATA)
                if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                    Toast.makeText(activity, getString(R.string.order_placed1), Toast.LENGTH_LONG)
                        .show()
                    PaymentModelClass(activity).placeOrder(
                        activity,
                        getString(R.string.flutterwave),
                        jsonObject.getString("txRef"),
                        true,
                        sendParams,
                        Constant.SUCCESS
                    )
                } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                    hideProgressDialog()
                    PaymentModelClass(activity).placeOrder(
                        activity,
                        "",
                        "",
                        false,
                        sendParams,
                        Constant.PENDING
                    )
                    Toast.makeText(activity, getString(R.string.order_error), Toast.LENGTH_LONG)
                        .show()
                } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                    hideProgressDialog()
                    PaymentModelClass(activity).placeOrder(
                        activity,
                        "",
                        "",
                        false,
                        sendParams,
                        Constant.FAILED
                    )
                    Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String) {
        try {
            razorPayId = razorpayPaymentID
            placeOrder(
                this@PaymentActivity,
                paymentMethod,
                razorPayId,
                true,
                sendParams,
                Constant.SUCCESS
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(tag, "onPaymentSuccess  ", e)
        }
    }

    override fun onPaymentError(code: Int, response: String) {
        hideProgressDialog()
        try {
            Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(tag, "onPaymentError  ", e)
        }
    }

    override fun onBackPressed() {
        if (lytProgressBar.visibility == View.GONE && !doubleBackToExitPressedOnce) {
            super.onBackPressed()
        } else if (lytProgressBar.visibility == View.VISIBLE && doubleBackToExitPressedOnce) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, getString(R.string.payment_back_press_message), Toast.LENGTH_SHORT)
                .show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
        doubleBackToExitPressedOnce = true
    }

    companion object {
        const val tag = "PAYMENT ACTIVITY"
        lateinit var customerId: String
        var deliveryTime = ""
        var deliveryDay = ""
        lateinit var recyclerView: RecyclerView

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: SlotAdapter
        lateinit var lytProgressBar: RelativeLayout
    }
}