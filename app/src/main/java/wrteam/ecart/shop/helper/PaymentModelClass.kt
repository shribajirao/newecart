package wrteam.ecart.shop.helper

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.payumoney.core.PayUmoneyConfig
import com.payumoney.core.PayUmoneyConstants
import com.payumoney.core.PayUmoneySdkInitializer.PaymentParam
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.activity.PaymentActivity
import wrteam.ecart.shop.fragment.CheckoutFragment
import wrteam.ecart.shop.fragment.WalletTransactionFragment
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

class PaymentModelClass(val activity: Activity) {
    val tag: String? = CheckoutFragment::class.java.simpleName
    private lateinit var mPaymentParams: PaymentParam
    lateinit var status: String
    private lateinit var udf5: String
    private lateinit var udf4: String
    private lateinit var udf3: String
    private lateinit var udf2: String
    private lateinit var udf1: String
    lateinit var email: String
    private lateinit var firstName: String
    private lateinit var productInfo: String
    lateinit var amount: String
    private lateinit var txnId: String
    private lateinit var key: String
    private lateinit var addedOn: String
    private lateinit var msg: String
    lateinit var ProductList: String
    lateinit var address: String
    private lateinit var mProgressDialog: ProgressBar
    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressBar(activity, null, android.R.attr.progressBarStyleLarge)
            mProgressDialog.isIndeterminate = true
        }
        mProgressDialog.visibility = View.VISIBLE
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.visibility == View.VISIBLE) {
            mProgressDialog.visibility = View.GONE
        }
    }

    fun OnPayClick(
        activity: Activity,
        sendParams: MutableMap<String, String>,
        OrderType: String,
        amount: String
    ) {
        try {
            Companion.sendParams = sendParams
            val payUmoneyConfig = PayUmoneyConfig.getInstance()

            //Use this to set your custom text on result screen button
            payUmoneyConfig.doneButtonText = "Done"
            //Use this to set your custom title for the activity
            payUmoneyConfig.payUmoneyActivityTitle = activity.resources.getString(R.string.app_name)
            val builder = PaymentParam.Builder()
            val txnId = System.currentTimeMillis().toString() + ""
            val phone = sendParams[Constant.MOBILE]
            val firstName = sendParams[Constant.USER_NAME]
            val email = sendParams[Constant.EMAIL]
            val udf1 = ""
            val udf2 = ""
            val udf3 = ""
            val udf4 = ""
            val udf5 = ""
            val udf6 = ""
            val udf7 = ""
            val udf8 = ""
            val udf9 = ""
            val udf10 = ""
            val appEnvironment = (activity.application as ApiConfig).getAppEnvironment()
            //builder.setAmount(amount)
            builder.setAmount(amount.split("\\.".toRegex()).toTypedArray()[0])
                .setTxnId(txnId)
                .setPhone(phone)
                .setProductName(OrderType)
                .setFirstName(firstName)
                .setEmail(email)
                .setsUrl(appEnvironment.surl())
                .setfUrl(appEnvironment.furl())
                .setUdf1(udf1)
                .setUdf2(udf2)
                .setUdf3(udf3)
                .setUdf4(udf4)
                .setUdf5(udf5)
                .setUdf6(udf6)
                .setUdf7(udf7)
                .setUdf8(udf8)
                .setUdf9(udf9)
                .setUdf10(udf10)
                .setIsDebug(appEnvironment.debug())
                .setKey(appEnvironment.merchantKey())
                .setMerchantId(appEnvironment.merchantID())
            try {
                mPaymentParams = builder.build()
                mPaymentParams = calculateServerSideHashAndInitiatePayment1(mPaymentParams)
                PayUmoneyFlowManager.startPayUMoneyFlow(
                    mPaymentParams,
                    activity,
                    R.style.AppTheme,
                    true
                )
                // generateHashFromServer(mPaymentParams);
            } catch (e: Exception) {
                Toast.makeText(activity, "build " + e.message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateServerSideHashAndInitiatePayment1(paymentParam: PaymentParam): PaymentParam {
        val stringBuilder = StringBuilder()
        val params = paymentParam.params
        try {
            stringBuilder.append(params[PayUmoneyConstants.KEY]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.TXNID]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.AMOUNT]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.PRODUCT_INFO]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.FIRSTNAME]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.EMAIL]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.UDF1]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.UDF2]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.UDF3]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.UDF4]).append("|")
            stringBuilder.append(params[PayUmoneyConstants.UDF5]).append("||||||")
            val appEnvironment = (activity.application as ApiConfig).getAppEnvironment()
            stringBuilder.append(appEnvironment.salt())
            val hash = hashCal("SHA-512", stringBuilder.toString())
            paymentParam.setMerchantHash(hash)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return paymentParam
    }

    fun TransactionMethod(data: Intent, activity: Activity, from: String) {
        // System.out.println("========transaction  call");
        val transactionResponse: TransactionResponse? =
            data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE)
        val resultModel: ResultModel? = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT)

        // Check which object is non-null
        if (transactionResponse?.getPayuResponse() != null) {
            //System.out.println("========transaction response "+transactionResponse.getPayuResponse());
            val appEnvironment = (activity.application as ApiConfig).getAppEnvironment()

            // Response from Payumoney
            val payuResponse = transactionResponse.getPayuResponse()
            try {
                val jsonObject = JSONObject(payuResponse)
                //System.out.println("***payURes  " + jsonObject.toString());
                val hashFromResponse = jsonObject.getJSONObject("result").getString("hash")
                status = jsonObject.getJSONObject("result").getString("status")
                email = jsonObject.getJSONObject("result").getString("email")
                firstName = jsonObject.getJSONObject("result").getString("firstname")
                productInfo = jsonObject.getJSONObject("result").getString("productinfo")
                amount = jsonObject.getJSONObject("result").getString("amount")
                txnId = jsonObject.getJSONObject("result").getString("txnid")
                key = jsonObject.getJSONObject("result").getString("key")
                addedOn = jsonObject.getJSONObject("result").getString("addedon")
                msg = jsonObject.getJSONObject("result").getString("error_Message")
                udf1 = jsonObject.getJSONObject("result").getString("udf1")
                udf2 = jsonObject.getJSONObject("result").getString("udf2")
                udf3 = jsonObject.getJSONObject("result").getString("udf3")
                udf4 = jsonObject.getJSONObject("result").getString("udf4")
                udf5 = jsonObject.getJSONObject("result").getString("udf5")
                val hasCal =
                    appEnvironment.salt() + "|" + status + "||||||" + udf5 + "|" + udf4 + "|" + udf3 + "|" + udf2 + "|" + udf1 + "|" + email + "|" + firstName + "|" + productInfo + "|" + amount + "|" + txnId + "|" + key
                val hash = hashCal1(hasCal)
                if (hashFromResponse == hash) {
                    if (status == Constant.SUCCESS) {
                        if (from == Constant.PAYMENT) {
                            placeOrder(
                                activity,
                                activity.resources.getString(R.string.payu_money),
                                txnId,
                                true,
                                sendParams,
                                status
                            )
                        } else if (from == Constant.WALLET) {
                            WalletTransactionFragment().addWalletBalance(
                                activity,
                                Session(activity),
                                WalletTransactionFragment.amount,
                                WalletTransactionFragment.msg
                            )
                        }
                    } else if (status == "failure") {
                        if (PaymentActivity.lytProgressBar != null && PaymentActivity.lytProgressBar.visibility == View.VISIBLE) {
                            PaymentActivity.lytProgressBar.visibility = View.GONE
                        }
                        placeOrder(
                            activity,
                            activity.resources.getString(R.string.payu_money),
                            txnId,
                            false,
                            sendParams,
                            status
                        )
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.transaction_failed_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (PaymentActivity.lytProgressBar != null && PaymentActivity.lytProgressBar.visibility == View.VISIBLE) {
                            PaymentActivity.lytProgressBar.visibility = View.GONE
                        }
                        placeOrder(
                            activity,
                            activity.resources.getString(R.string.payu_money),
                            txnId,
                            false,
                            sendParams,
                            status
                        )
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.transaction_failed_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else if (resultModel != null && resultModel.error != null) {
            Log.d(tag, "Error response : " + resultModel.error.transactionResponse)
        } else {
            Log.d(tag, "Both objects are null!")
        }
    }

    fun placeOrder(
        activity: Activity,
        paymentType: String,
        transactionId: String,
        isSuccess: Boolean,
        sendParams1: MutableMap<String, String>,
        status: String
    ) {
        showProgressDialog()
        if (isSuccess) {
            ApiConfig.requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                addTransaction(
                                    jsonObject.getString(Constant.ORDER_ID),
                                    paymentType,
                                    transactionId,
                                    status,
                                    activity.resources.getString(R.string.order_success),
                                    sendParams1
                                )
                                val intent = Intent(activity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra(Constant.FROM, "payment_success")
                                activity.startActivity(intent)
                                activity.finish()
                            } else {
                                hideProgressDialog()
                            }
                        } catch (e: JSONException) {
                            hideProgressDialog()
                        }
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, sendParams1, false)
        } else {
            hideProgressDialog()
            addTransaction("", paymentType, transactionId, status, "Order Failed", sendParams1)
        }
    }

    fun addTransaction(
        orderId: String,
        paymentType: String,
        treansactionId: String,
        status: String,
        message: String,
        sendParams: MutableMap<String, String>
    ) {
        val transactionParams: MutableMap<String, String> = HashMap()
        transactionParams[Constant.ADD_TRANSACTION] = Constant.GetVal
        transactionParams[Constant.USER_ID] = sendParams[Constant.USER_ID].toString()
        transactionParams[Constant.ORDER_ID] = orderId
        transactionParams[Constant.TYPE] = paymentType
        transactionParams[Constant.TRANS_ID] = treansactionId
        transactionParams[Constant.AMOUNT] = sendParams[Constant.FINAL_TOTAL].toString()
        transactionParams[Constant.STATUS] = status
        transactionParams[Constant.MESSAGE] = message
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        transactionParams["transaction_date"] = df.format(c)
        //System.out.println ("====trans params " + transactionParams);
        ApiConfig.requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                //System.out.println ("=================*transaction- " + response);
                if (result) {
                    try {
                        hideProgressDialog()
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            activity.finish()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, transactionParams, false)
    }

    companion object {
        lateinit var sendParams: MutableMap<String, String>

        //this method calculate hash from sdk
        fun hashCal(type: String, hashString: String): String {
            val hash = StringBuilder()
            val messageDigest: MessageDigest
            try {
                messageDigest = MessageDigest.getInstance(type)
                messageDigest.update(hashString.toByteArray())
                val messageDigestBytes = messageDigest.digest()
                for (hashByte in messageDigestBytes) {
                    hash.append(((hashByte + 0xff) + 0x100).toString(16).substring(1))
                }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return hash.toString()
        }

        fun hashCal1(str: String): String {
            val hashSeq = str.toByteArray()
            val hexString = StringBuilder()
            try {
                val algorithm = MessageDigest.getInstance("SHA-512")
                algorithm.reset()
                algorithm.update(hashSeq)
                val messageDigest = algorithm.digest()
                for (aMessageDigest in messageDigest) {
                    val hex = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    if (hex.length == 1) {
                        hexString.append("0")
                    }
                    hexString.append(hex)
                }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return hexString.toString()
        }
    }
}