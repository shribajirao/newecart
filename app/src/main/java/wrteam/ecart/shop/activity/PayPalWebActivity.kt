package wrteam.ecart.shop.activity

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
import wrteam.ecart.shop.helper.ApiConfig
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import java.text.SimpleDateFormat
import java.util.*

class PayPalWebActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var webView: WebView
    lateinit var url: String
    var isTxnInProcess = true
    private lateinit var orderId: String
    lateinit var session: Session
    private lateinit var sendParams: MutableMap<String, String>
    lateinit var from: String
    private lateinit var imageMenu: ImageView
    lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        sendParams = intent.getSerializableExtra(Constant.PARAMS) as MutableMap<String, String>
        orderId = intent.getStringExtra(Constant.ORDER_ID).toString()
        from = intent.getStringExtra(Constant.FROM).toString()
        session = Session(this@PayPalWebActivity)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        imageMenu = findViewById(R.id.imageMenu)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbarTitle.text = getString(R.string.paypal)
        findViewById<View>(R.id.imageHome).visibility = View.GONE
        imageMenu.visibility = View.VISIBLE
        imageMenu.setImageResource(R.drawable.ic_arrow_back)
        imageMenu.setOnClickListener { onBackPressed() }
        webView = findViewById(R.id.webView)
        url = intent.getStringExtra("url").toString()
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
                    val status = jsonObject.getString("status")
                    addTransaction(
                        this@PayPalWebActivity,
                        orderId,
                        getString(R.string.paypal),
                        orderId,
                        status,
                        "",
                        sendParams
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        ) { error: VolleyError -> }
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
                                getWalletBalance(activity, session)
                                Toast.makeText(
                                    activity,
                                    "Amount will be credited in wallet very soon.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (from == Constant.PAYMENT) {
                                if (status == Constant.SUCCESS || status == Constant.AWAITING_PAYMENT) {
                                    val intent = Intent(activity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.putExtra(Constant.FROM, "payment_success")
                                    startActivity(intent)
                                    finish()
                                } else {
                                    finish()
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, transactionParams, true)
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
        val alertDialog = AlertDialog.Builder(this@PayPalWebActivity)
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.txn_cancel_msg))
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()
        alertDialog.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface, which: Int ->
            deleteTransaction(
                this@PayPalWebActivity, intent.getStringExtra(Constant.ORDER_ID).toString()
            )
            alertDialog1.dismiss()
        }
            .setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, which: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    private fun deleteTransaction(activity: Activity, orderId: String) {
        val transactionParams: MutableMap<String, String> = HashMap()
        transactionParams[Constant.DELETE_ORDER] = Constant.GetVal
        transactionParams[Constant.ORDER_ID] = orderId
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    super@PayPalWebActivity.onBackPressed()
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, transactionParams, false)
    }
}