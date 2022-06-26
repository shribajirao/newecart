package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.flutterwave.raveandroid.RaveConstants
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RavePayManager
import com.google.gson.Gson
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.razorpay.Checkout
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
import wrteam.ecart.shop.activity.MidtransActivity
import wrteam.ecart.shop.activity.PayPalWebActivity
import wrteam.ecart.shop.activity.PayStackActivity
import wrteam.ecart.shop.activity.StripeActivity
import wrteam.ecart.shop.adapter.WalletTransactionAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.setAppEnvironment
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Constant.randomAlphaNumeric
import wrteam.ecart.shop.helper.Constant.randomNumeric
import wrteam.ecart.shop.helper.PaymentModelClass
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Session.Companion.setCount
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Address
import wrteam.ecart.shop.model.WalletTransaction
import java.io.Serializable
import kotlin.math.roundToInt

@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
class WalletTransactionFragment : Fragment(), PaytmPaymentTransactionCallback {
    lateinit var root: View
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var scrollView: NestedScrollView
    var total = 0
    lateinit var activity: Activity
    var offset = 0
    lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertSubTitle: TextView
    private lateinit var btnRechargeWallet: Button
    lateinit var session: Session
    var isLoadMore = false
    private lateinit var paymentMethod: String
    lateinit var customerId: String
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_wallet_transection, container, false)
        activity = requireActivity()
        session = Session(activity)
        scrollView = root.findViewById(R.id.scrollView)
        recyclerView = root.findViewById(R.id.recyclerView)
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        swipeLayout = root.findViewById(R.id.swipeLayout)
        lytAlert = root.findViewById(R.id.lytAlert)
        tvAlertTitle = root.findViewById(R.id.tvAlertTitle)
        tvAlertSubTitle = root.findViewById(R.id.tvAlertSubTitle)
        tvBalance = root.findViewById(R.id.tvBalance)
        btnRechargeWallet = root.findViewById(R.id.btnRechargeWallet)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        tvAlertTitle.text = getString(R.string.no_wallet_history_found)
        tvAlertSubTitle.text = getString(R.string.you_have_not_any_wallet_history_yet)
        setHasOptionsMenu(true)
        walletTransactions=ArrayList()
        walletTransactionAdapter = WalletTransactionAdapter(
            activity, walletTransactions
        )

        getTransactionData(activity, session)
        swipeLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            offset = 0
            getTransactionData(activity, session)
        }
        getWalletBalance(activity, session)
        tvBalance.text = session.getData(Constant.currency) + session.getData(Constant.WALLET_BALANCE)
        btnRechargeWallet.setOnClickListener {
            val alertDialog = AlertDialog.Builder(
                activity
            )
            val inflater1 =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater1.inflate(R.layout.dialog_wallet_recharge, null)
            alertDialog.setView(dialogView)
            alertDialog.setCancelable(true)
            val dialog = alertDialog.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val edtAmount: TextView = dialogView.findViewById(R.id.edtAmount)
            val edtMsg: TextView = dialogView.findViewById(R.id.edtMsg)
            val tvDialogCancel: TextView = dialogView.findViewById(R.id.tvDialogCancel)
            val tvDialogSend: TextView = dialogView.findViewById(R.id.tvDialogRecharge)
            val lytPayOption: LinearLayout = dialogView.findViewById(R.id.lytPayOption)
            val rbPayStack: RadioButton = dialogView.findViewById(R.id.rbPayStack)
            val rbFlutterWave: RadioButton = dialogView.findViewById(R.id.rbFlutterWave)
            val rbPayPal: RadioButton = dialogView.findViewById(R.id.rbPayPal)
            val rbRazorPay: RadioButton = dialogView.findViewById(R.id.rbRazorPay)
            val rbMidTrans: RadioButton = dialogView.findViewById(R.id.rbMidTrans)
            val rbStripe: RadioButton = dialogView.findViewById(R.id.rbStripe)
            val rbPayTm: RadioButton = dialogView.findViewById(R.id.rbPayTm)
            val rbPayU: RadioButton = dialogView.findViewById(R.id.rbPayU)
            val lytPayment: RadioGroup = dialogView.findViewById(R.id.lytPayment)
            lytPayment.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
                val rb = dialogView.findViewById<RadioButton>(checkedId)
                paymentMethod = rb.tag.toString()
            }
            val params: MutableMap<String, String> = HashMap()
            params[Constant.SETTINGS] = Constant.GetVal
            params[Constant.GET_PAYMENT_METHOD] = Constant.GetVal
            //  System.out.println("=====params " + params.toString());
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject1 = JSONObject(response)
                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                if (jsonObject1.has("payment_methods")) {
                                    val jsonObject =
                                        jsonObject1.getJSONObject(Constant.PAYMENT_METHODS)
                                    if (jsonObject.has(Constant.payu_method)) {
                                        Constant.PAYUMONEY =
                                            jsonObject.getString(Constant.payu_method)
                                        Constant.MERCHANT_KEY =
                                            jsonObject.getString(Constant.PAY_M_KEY)
                                        Constant.MERCHANT_ID =
                                            jsonObject.getString(Constant.PAYU_M_ID)
                                        Constant.MERCHANT_SALT =
                                            jsonObject.getString(Constant.PAYU_SALT)
                                        setAppEnvironment(activity)
                                    }
                                    if (jsonObject.has(Constant.razor_pay_method)) {
                                        Constant.RAZORPAY =
                                            jsonObject.getString(Constant.razor_pay_method)
                                        Constant.RAZOR_PAY_KEY_VALUE =
                                            jsonObject.getString(Constant.RAZOR_PAY_KEY)
                                    }
                                    if (jsonObject.has(Constant.paypal_method)) {
                                        Constant.PAYPAL =
                                            jsonObject.getString(Constant.paypal_method)
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
                                        isAddressAvailable
                                    }
                                    if (jsonObject.has(Constant.paytm_payment_method)) {
                                        Constant.PAYTM =
                                            jsonObject.getString(Constant.paytm_payment_method)
                                        Constant.PAYTM_MERCHANT_ID =
                                            jsonObject.getString(Constant.paytm_merchant_id)
                                        Constant.PAYTM_MERCHANT_KEY =
                                            jsonObject.getString(Constant.paytm_merchant_key)
                                        Constant.PAYTM_MODE =
                                            jsonObject.getString(Constant.paytm_mode)
                                    }
                                    if (jsonObject.has(Constant.ssl_method)) {
                                        Constant.SSLECOMMERZ =
                                            jsonObject.getString(Constant.ssl_method)
                                        Constant.SSLECOMMERZ_MODE =
                                            jsonObject.getString(Constant.ssl_mode)
                                        Constant.SSLECOMMERZ_STORE_ID =
                                            jsonObject.getString(Constant.ssl_store_id)
                                        Constant.SSLECOMMERZ_SECRET_KEY =
                                            jsonObject.getString(Constant.ssl_store_password)
                                    }
                                    if (Constant.FLUTTER_WAVE == "0" && Constant.PAYPAL == "0" && Constant.PAYUMONEY == "0" && Constant.COD == "0" && Constant.RAZORPAY == "0" && Constant.PAY_STACK == "0" && Constant.MIDTRANS == "0" && Constant.STRIPE == "0" && Constant.PAYTM == "0" && Constant.SSLECOMMERZ == "0") {
                                        lytPayOption.visibility = View.GONE
                                    } else {
                                        lytPayOption.visibility = View.VISIBLE
                                        if (Constant.PAYUMONEY == "1") {
                                            rbPayU.visibility = View.VISIBLE
                                        }
                                        if (Constant.RAZORPAY == "1") {
                                            rbRazorPay.visibility = View.VISIBLE
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
                                    }
                                } else {
                                    Toast.makeText(
                                        activity,
                                        getString(R.string.alert_payment_methods_blank),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.SETTING_URL, params, false)
            tvDialogSend.setOnClickListener { v1: View ->
                if (edtAmount.text.toString() == "") {
                    edtAmount.requestFocus()
                    edtAmount.error = getString(R.string.alert_enter_amount)
                } else if (edtAmount.text.toString()
                        .toDouble() > session.getData(Constant.user_wallet_refill_limit)
                        .toDouble()
                ) {
                    Toast.makeText(
                        activity,
                        getString(R.string.max_wallet_amt_error),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (edtAmount.text.toString().toDouble() <= 0) {
                    edtAmount.requestFocus()
                    edtAmount.error = getString(R.string.alert_recharge)
                } else {
                    amount = edtAmount.text.toString()
                    msg = edtMsg.text.toString()
                    rechargeWallet()
                    dialog.dismiss()
                }
            }
            tvDialogCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
        return root
    }

    val isAddressAvailable: Unit
        get() {
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_ADDRESSES] = Constant.GetVal
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
                                total = jsonObject.getString(Constant.TOTAL).toInt()

                                val jsonObject = JSONObject(response)
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)

                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    if (jsonObject1 != null) {
                                        val address =
                                            Gson().fromJson(
                                                jsonObject1.toString(),
                                                Address::class.java
                                            )
                                        if (address.is_default == "1") {
                                            Constant.DefaultAddress =
                                                address.address + ", " + address.landmark + ", " + address.city_name + ", " + address.area_name + ", " + address.state + ", " + address.country + ", " + activity.getString(
                                                    R.string.pincode_
                                                ) + address.pincode
                                            Constant.DefaultCity = address.city_name
                                            Constant.DefaultPinCode = address.pincode
                                        }
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.GET_ADDRESS_URL, params, false)
        }

    @SuppressLint("SetTextI18n")
    fun addWalletBalance(activity: Activity, session: Session, amount: String, msg: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.ADD_WALLET_BALANCE] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] = session.getData(
            Constant.ID
        )
        params[Constant.AMOUNT] = amount
        params[Constant.TYPE] = Constant.CREDIT
        params[Constant.MESSAGE] = msg
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val walletTransaction = Gson().fromJson(
                                jsonObject.getJSONObject(Constant.DATA).toString(),
                                WalletTransaction::class.java
                            )
                            if (walletTransactions.size == 0) {
                                lytAlert.visibility = View.GONE
                            }
                            walletTransactions.add(walletTransaction)
                            walletTransactionAdapter.notifyDataSetChanged()
                            tvBalance.text = session.getData(Constant.currency) + stringFormat(
                                "" + jsonObject.getString(
                                    Constant.NEW_BALANCE
                                ).toDouble()
                            )
                            DrawerFragment.tvWallet.text = session.getData(Constant.currency) + stringFormat(
                                "" + jsonObject.getString(
                                    Constant.NEW_BALANCE
                                ).toDouble()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.TRANSACTION_URL, params, false)
    }

    private fun startPayPalPayment(sendParams: MutableMap<String, String>) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.FIRST_NAME] = session.getData(Constant.NAME)
        params[Constant.LAST_NAME] = session.getData(Constant.NAME)
        params[Constant.PAYER_EMAIL] = session.getData(Constant.EMAIL)
        params[Constant.ITEM_NAME] = getString(R.string.wallet_recharge)
        params[Constant.ITEM_NUMBER] =
            "wallet-refill-user-" + Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis()
        params[Constant.AMOUNT] = sendParams[Constant.FINAL_TOTAL].toString()
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                val intent = Intent(activity, PayPalWebActivity::class.java)
                intent.putExtra(Constant.URL, response)
                intent.putExtra(
                    Constant.ORDER_ID, "wallet-refill-user-" + Session(activity).getData(
                        Constant.ID
                    ) + "-" + System.currentTimeMillis()
                )
                intent.putExtra(Constant.FROM, Constant.WALLET)
                intent.putExtra(Constant.PARAMS, sendParams as Serializable)
                startActivity(intent)
            }
        }, activity, Constant.PAPAL_URL, params, true)
    }

    private fun callPayStack(sendParams: MutableMap<String, String>) {
        val intent = Intent(activity, PayStackActivity::class.java)
        intent.putExtra(Constant.PARAMS, sendParams as Serializable)
        startActivity(intent)
    }

    private fun startFlutterWavePayment() {
        RavePayManager(this)
            .setAmount(amount.toDouble())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != RaveConstants.RAVE_REQUEST_CODE && data != null) {
            PaymentModelClass(activity).TransactionMethod(
                data,
                activity,
                Constant.WALLET
            )
        } else if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    addWalletBalance(activity, Session(activity), amount, msg)
                    Toast.makeText(activity, getString(R.string.wallet_recharged), Toast.LENGTH_LONG)
                        .show()
                }
                RavePayActivity.RESULT_ERROR -> {
                    Toast.makeText(activity, getString(R.string.order_error), Toast.LENGTH_LONG).show()
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun rechargeWallet() {
        val sendParams = HashMap<String, String>()
        if (paymentMethod == getString(R.string.pay_u)) {
            sendParams[Constant.MOBILE] = session.getData(Constant.MOBILE)
            sendParams[Constant.USER_NAME] =
                session.getData(Constant.NAME)
            sendParams[Constant.EMAIL] =
                session.getData(Constant.EMAIL)
            PaymentModelClass(activity).OnPayClick(
                activity,
                sendParams,
                Constant.WALLET,
                amount
            )
        } else if (paymentMethod == getString(R.string.paypal)) {
            sendParams[Constant.FINAL_TOTAL] = amount
            sendParams[Constant.FIRST_NAME] = session.getData(Constant.NAME)
            sendParams[Constant.LAST_NAME] =
                session.getData(Constant.NAME)
            sendParams[Constant.PAYER_EMAIL] =
                session.getData(Constant.EMAIL)
            sendParams[Constant.ITEM_NAME] = getString(R.string.wallet_recharge_)
            sendParams[Constant.ITEM_NUMBER] = System.currentTimeMillis()
                .toString() + randomNumeric(3)
            startPayPalPayment(sendParams)
        } else if (paymentMethod == getString(R.string.razor_pay)) {
            payFromWallet = true
            createOrderId(amount.toDouble())
        } else if (paymentMethod == getString(R.string.paystack)) {
            sendParams[Constant.FINAL_TOTAL] = amount
            sendParams[Constant.FROM] = Constant.WALLET
            callPayStack(sendParams)
        } else if (paymentMethod == getString(R.string.flutterwave)) {
            startFlutterWavePayment()
        } else if (paymentMethod == getString(R.string.midtrans)) {
            sendParams[Constant.FINAL_TOTAL] = amount
            if (session.getBoolean(Constant.IS_USER_LOGIN)) sendParams[Constant.USER_ID] =
                session.getData(Constant.ID)
            createMidtransPayment(amount, sendParams)
        } else if (paymentMethod == getString(R.string.stripe)) {
            if (Constant.DefaultAddress != "") {
                sendParams[Constant.FINAL_TOTAL] = amount
                if (session.getBoolean(Constant.IS_USER_LOGIN)) sendParams[Constant.USER_ID] =
                    session.getData(Constant.ID)
                val intent = Intent(activity, StripeActivity::class.java)
                intent.putExtra(
                    Constant.ORDER_ID, "wallet-refill-user-" + Session(activity).getData(
                        Constant.ID
                    ) + "-" + System.currentTimeMillis()
                )
                intent.putExtra(Constant.FROM, Constant.WALLET)
                intent.putExtra(Constant.PARAMS, sendParams)
                startActivity(intent)
            } else {
                Toast.makeText(activity, getString(R.string.address_msg), Toast.LENGTH_SHORT).show()
            }
        } else if (paymentMethod == getString(R.string.paytm)) {
            startPayTmPayment()
        } else if (paymentMethod == getString(R.string.sslecommerz)) {
            startSslCommerzPayment(
                amount,
                msg,
                System.currentTimeMillis().toString() + randomNumeric(3)
            )
        }
    }

    private fun startSslCommerzPayment(amount: String, msg: String, txnId: String) {
        val mode: String = if (Constant.SSLECOMMERZ_MODE == "sandbox") {
            SdkType.TESTBOX
        } else {
            SdkType.LIVE
        }
        val mandatoryFieldModel = MandatoryFieldModel(
            Constant.SSLECOMMERZ_STORE_ID,
            Constant.SSLECOMMERZ_SECRET_KEY,
            amount,
            txnId,
            CurrencyType.BDT,
            mode,
            SdkCategory.BANK_LIST
        )

        /* Call for the payment */PayUsingSSLCommerz.getInstance()
            .setData(activity, mandatoryFieldModel, object : OnPaymentResultListener {
                override fun transactionSuccess(transactionInfo: TransactionInfo) {
                    // If payment is success and risk label is 0.
                    addWalletBalance(activity, Session(activity), amount, msg)
                }

                override fun transactionFail(sessionKey: String) {
                    Toast.makeText(
                        activity,
                        "transactionFail -> Session : $sessionKey",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun error(errorCode: Int) {
                    when (errorCode) {
                        ErrorKeys.USER_INPUT_ERROR -> Toast.makeText(
                            activity,
                            "User Input Error",
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.INTERNET_CONNECTION_ERROR -> Toast.makeText(
                            activity,
                            "Internet Connection Error",
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.DATA_PARSING_ERROR -> Toast.makeText(
                            activity,
                            "Data Parsing Error",
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.CANCEL_TRANSACTION_ERROR -> Toast.makeText(
                            activity,
                            "User Cancel The Transaction",
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.SERVER_ERROR -> Toast.makeText(
                            activity,
                            "Server Error",
                            Toast.LENGTH_LONG
                        ).show()
                        ErrorKeys.NETWORK_ERROR -> Toast.makeText(
                            activity,
                            "Network Error",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    private fun startPayTmPayment() {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.ORDER_ID_] = randomAlphaNumeric(20)
        params[Constant.CUST_ID] = randomAlphaNumeric(10)
        params[Constant.TXN_AMOUNT] = "" + amount
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
                        paramMap[Constant.ORDER_ID_] = jsonObject.getString("order id")
                        paramMap[Constant.CUST_ID] = jsonObject.getString(Constant.CUST_ID)
                        paramMap[Constant.TXN_AMOUNT] = "" + amount
                        if (Constant.PAYTM_MODE == "sandbox") {
                            paramMap[Constant.INDUSTRY_TYPE_ID] = Constant.INDUSTRY_TYPE_ID_LIVE_VAL
                            paramMap[Constant.CHANNEL_ID] = Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL
                            paramMap[Constant.WEBSITE] = Constant.WEBSITE_DEMO_VAL
                        } else if (Constant.PAYTM_MODE == "production") {
                            paramMap[Constant.INDUSTRY_TYPE_ID] = Constant.INDUSTRY_TYPE_ID_DEMO_VAL
                            paramMap[Constant.CHANNEL_ID] = Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL
                            paramMap[Constant.WEBSITE] = Constant.WEBSITE_LIVE_VAL
                        }
                        paramMap[Constant.CALLBACK_URL] =
                            jsonObject.getString(Constant.CALLBACK_URL)
                        paramMap[Constant.CHECK_SUM_HASH] = jsonObject.getString("signature")

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
                    verifyTransaction(orderId!!)
                }
            }

            override fun networkNotAvailable() {
                Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
            }

            override fun clientAuthenticationFailed(s: String) {
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
            }

            override fun someUIErrorOccurred(s: String) {
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
            }

            override fun onErrorLoadingWebPage(i: Int, s: String, s1: String) {
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
            }

            override fun onBackPressedCancelTransaction() {
                Toast.makeText(activity, "Back Pressed", Toast.LENGTH_LONG).show()
            }

            override fun onTransactionCancel(s: String, bundle: Bundle) {
                Toast.makeText(activity, s + bundle.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }, activity, Constant.GENERATE_PAYTM_CHECKSUM, params, false)
    }

    /**
     * Verifying the transaction status once PayTM transaction is over
     * This makes server(own) -> server(PayTM) call to verify the transaction status
     */
    private fun verifyTransaction(orderId: String) {
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
                            addWalletBalance(activity, Session(activity), amount, msg)
                            Toast.makeText(
                                activity,
                                getString(R.string.wallet_recharged),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.VALID_TRANSACTION, params, false)
    }

    override fun onTransactionResponse(bundle: Bundle) {
        val orderId = bundle.getString(Constant.ORDERID)
        val status = bundle.getString(Constant.STATUS_)
        if (status.equals(Constant.TXN_SUCCESS, ignoreCase = true)) {
            verifyTransaction(orderId!!)
        }
    }

    override fun networkNotAvailable() {
                Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show()
    }

    override fun clientAuthenticationFailed(s: String) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
    }

    override fun someUIErrorOccurred(s: String) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
    }

    override fun onErrorLoadingWebPage(i: Int, s: String, s1: String) {
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressedCancelTransaction() {
        Toast.makeText(activity, "Back Pressed", Toast.LENGTH_LONG).show()
    }

    override fun onTransactionCancel(s: String, bundle: Bundle) {
        Toast.makeText(activity, s + bundle.toString(), Toast.LENGTH_LONG)
            .show()
    }

    private fun createOrderId(payAmount: Double) {
        val amount = (payAmount * 100).toString().split("\\.".toRegex()).toTypedArray()
        val params: MutableMap<String, String> = HashMap()
        params["amount"] = amount[0]
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

    private fun startPayment(orderId: String, payAmount: String) {
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
            Log.d("Payment : ", "Error in starting Razorpay Checkout", e)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getTransactionData(activity: Activity, session: Session) {
        walletTransactions = ArrayList()
        recyclerView.adapter = WalletTransactionAdapter(
            activity, walletTransactions
        )
        walletTransactionAdapter.notifyDataSetChanged()
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_USER_TRANSACTION] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] = session.getData(
            Constant.ID
        )
        params[Constant.TYPE] = Constant.TYPE_WALLET_TRANSACTION
        params[Constant.OFFSET] = "" + offset
        params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            lytAlert.visibility = View.GONE
                            total = jsonObject.getString(Constant.TOTAL).toInt()

                            val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject1 = jsonArray.getJSONObject(i)
                                if (jsonObject1 != null) {
                                    val transaction = Gson().fromJson(
                                        jsonObject1.toString(),
                                        WalletTransaction::class.java
                                    )
                                    walletTransactions.add(transaction)
                                } else {
                                    break
                                }
                            }
                            if (offset == 0) {
                                walletTransactionAdapter = WalletTransactionAdapter(
                                    activity, walletTransactions
                                )
                                recyclerView.adapter = walletTransactionAdapter
                                mShimmerViewContainer.stopShimmer()
                                mShimmerViewContainer.visibility = View.GONE
                                scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        val linearLayoutManager =
                                            recyclerView.layoutManager as LinearLayoutManager
                                        if (walletTransactions.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == walletTransactions.size - 1) {
                                                    //bottom of list!


                                                    offset += Constant.LOAD_ITEM_LIMIT
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.GET_USER_TRANSACTION] =
                                                        Constant.GetVal
                                                    if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                        session.getData(
                                                            Constant.ID
                                                        )
                                                    params1[Constant.TYPE] =
                                                        Constant.TYPE_WALLET_TRANSACTION
                                                    params1[Constant.OFFSET] = "" + offset
                                                    params1[Constant.LIMIT] =
                                                        "" + Constant.LOAD_ITEM_LIMIT
                                                    requestToVolley(
                                                        object : VolleyCallback {
                                                            override fun onSuccess(
                                                                result: Boolean,
                                                                response: String
                                                            ) {
                                                                if (result) {
                                                                    try {
                                                                        // System.out.println("====product  " + response);
                                                                        val jsonObject12 =
                                                                            JSONObject(response)
                                                                        if (!jsonObject12.getBoolean(
                                                                                Constant.ERROR
                                                                            )
                                                                        ) {
                                                                            session.setData(
                                                                                Constant.TOTAL,
                                                                                jsonObject12.getString(
                                                                                    Constant.TOTAL
                                                                                )
                                                                            )


                                                                            val object1 =
                                                                                JSONObject(response)
                                                                            val jsonArray1 =
                                                                                object1.getJSONArray(
                                                                                    Constant.DATA
                                                                                )
                                                                            for (i in 0 until jsonArray1.length()) {
                                                                                val jsonObject1 =
                                                                                    jsonArray1.getJSONObject(
                                                                                        i
                                                                                    )
                                                                                if (jsonObject1 != null) {
                                                                                    val walletTransaction =
                                                                                        Gson().fromJson(
                                                                                            jsonObject1.toString(),
                                                                                            WalletTransaction::class.java
                                                                                        )
                                                                                    walletTransactions.add(
                                                                                        walletTransaction
                                                                                    )
                                                                                } else {
                                                                                    break
                                                                                }
                                                                            }
                                                                            walletTransactionAdapter.notifyDataSetChanged()
                                                                            isLoadMore = false
                                                                        }
                                                                    } catch (e: JSONException) {
                                                                        mShimmerViewContainer.stopShimmer()
                                                                        mShimmerViewContainer.visibility =
                                                                            View.GONE
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        activity,
                                                        Constant.TRANSACTION_URL,
                                                        params1,
                                                        false
                                                    )
                                                    isLoadMore = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            lytAlert.visibility = View.VISIBLE
                            mShimmerViewContainer.stopShimmer()
                            mShimmerViewContainer.visibility = View.GONE
                        }
                    } catch (e: JSONException) {
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                    }
                }
            }
        }, activity, Constant.TRANSACTION_URL, params, false)
    }

    private fun createMidtransPayment(grossAmount: String, sendParams: MutableMap<String, String>) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.ORDER_ID] =
            "wallet-refill-user-" + Session(activity).getData(Constant.ID) + "-" + System.currentTimeMillis()
        params[Constant.GROSS_AMOUNT] = "" + grossAmount.toDouble().roundToInt()
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
                            intent.putExtra(
                                Constant.ORDER_ID,
                                "wallet-refill-user-" + Session(activity).getData(
                                    Constant.ID
                                ) + "-" + System.currentTimeMillis()
                            )
                            intent.putExtra(Constant.FROM, Constant.WALLET)
                            intent.putExtra(Constant.PARAMS, sendParams as Serializable)
                            startActivity(intent)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.MIDTRANS_PAYMENT_URL, params, true)
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.wallet_history)
        setCount(Constant.UNREAD_WALLET_COUNT, 0, activity)
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
        menu.findItem(R.id.toolbar_layout).isVisible = false
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
    }

    companion object {
        lateinit var amount: String
        lateinit var msg: String
        var payFromWallet = false

        @SuppressLint("StaticFieldLeak")
        lateinit var tvBalance: TextView
        lateinit var recyclerView: RecyclerView
        lateinit var walletTransactions: ArrayList<WalletTransaction?>
        @SuppressLint("StaticFieldLeak")
        lateinit var lytAlert: RelativeLayout
        @SuppressLint("StaticFieldLeak")
        lateinit var walletTransactionAdapter: WalletTransactionAdapter
    }
}