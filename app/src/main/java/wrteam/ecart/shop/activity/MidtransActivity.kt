package wrteam.ecart.shop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.helper.*
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import java.text.SimpleDateFormat
import java.util.*

class MidtransActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var webView: WebView
    lateinit var url: String
    private lateinit var paymentModelClass: PaymentModelClass
    var isTxnInProcess = true
    private lateinit var orderId: String
    private lateinit var sendParams: MutableMap<String, String>
    lateinit var from: String
    lateinit var toolbarTitle: TextView
    private lateinit var imageMenu: ImageView
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        imageMenu = findViewById(R.id.imageMenu)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        imageMenu = findViewById(R.id.imageMenu)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbarTitle.text = getString(R.string.midtrans)
        findViewById<View>(R.id.imageHome).visibility = View.GONE
        imageMenu.visibility = View.VISIBLE
        imageMenu.setImageResource(R.drawable.ic_arrow_back)
        imageMenu.setOnClickListener { onBackPressed() }
        paymentModelClass = PaymentModelClass(this@MidtransActivity)
        url = intent.getStringExtra("url").toString()
        orderId = intent.getStringExtra(Constant.ORDER_ID).toString()
        sendParams = intent.getSerializableExtra(Constant.PARAMS) as MutableMap<String, String>
        from = intent.getStringExtra(Constant.FROM).toString()
        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                isTxnInProcess = if (url.startsWith(Constant.MainBaseURL)) {
                    GetTransactionResponse(url)
                    return true
                } else true
                return false
            }
        }
        webView.loadUrl(url)
    }

    fun GetTransactionResponse(url: String) {
        val stringRequest = StringRequest(
            Request.Method.POST, url,
            { response: String ->
                isTxnInProcess = false
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getString("transaction_status")
                    addTransaction(
                        this@MidtransActivity,
                        orderId,
                        getString(R.string.midtrans),
                        orderId,
                        status,
                        jsonObject.getString(
                            Constant.MESSAGE
                        ),
                        sendParams
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { obj: VolleyError -> obj.printStackTrace() }
        ApiConfig.instance.getRequestQueue().cache.clear()
        ApiConfig.instance.addToRequestQueue(stringRequest)
    }

    private fun addTransaction(
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
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (from == Constant.WALLET) {
                                onBackPressed()
                                getWalletBalance(activity, Session(activity))
                                Toast.makeText(
                                    activity,
                                    getString(R.string.wallet_message),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (from == Constant.PAYMENT) {
                                if (status == "capture" || status == "challenge" || status == "pending") {
                                    val intent = Intent(activity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.putExtra(Constant.FROM, "payment_success")
                                    startActivity(intent)
                                    finish()
                                } else if (status == "deny" || status == "expire" || status == "cancel") {
                                    finish()
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, this@MidtransActivity, Constant.ORDER_PROCESS_URL, transactionParams, false)
    }

    override fun onBackPressed() {
        if (isTxnInProcess) {
            processAlertDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun processAlertDialog() {
        val alertDialog = AlertDialog.Builder(this@MidtransActivity)
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.txn_cancel_msg))
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()
        alertDialog.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface, which: Int ->
            deleteTransaction(
                this@MidtransActivity, intent?.getStringExtra(Constant.ORDER_ID)!!
            )
            alertDialog1.dismiss()
        }
            .setNegativeButton(getString(R.string.no)) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    private fun deleteTransaction(activity: Activity, orderId: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.DELETE_ORDER] = Constant.GetVal
        params[Constant.ORDER_ID] = orderId
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    super@MidtransActivity.onBackPressed()
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false)
    }
}