package wrteam.ecart.shop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import co.paystack.android.Paystack.TransactionCallback
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.WalletTransactionFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.PaymentModelClass
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.ui.CreditCardEditText
import java.util.*

class PayStackActivity : AppCompatActivity() {
    lateinit var email: String
    private lateinit var cardNumber: String
    private lateinit var cvv: String
    private var expiryMonth = 0
    private var expiryYear = 0
    lateinit var toolbar: Toolbar
    lateinit var session: Session
    lateinit var activity: Activity
    private lateinit var tvPayable: TextView
    lateinit var sendParams: MutableMap<String, String>
    lateinit var paymentModelClass: PaymentModelClass
    private var payableAmount = 0.0
    lateinit var from: String

    //variables
    private lateinit var card: Card
    private lateinit var charge: Charge
    private lateinit var emailField: EditText
    private lateinit var cardNumberField: CreditCardEditText
    private lateinit var expiryMonthField: EditText
    private lateinit var expiryYearField: EditText
    private lateinit var cvvField: EditText
    lateinit var toolbarTitle: TextView
    private lateinit var imageMenu: ImageView
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init payStack sdk
        PaystackSdk.initialize(applicationContext)
        setContentView(R.layout.activity_pay_stack)
        getAllWidget()
        setPaystackKey(Constant.PAY_STACK_KEY)
        activity = this@PayStackActivity
        session = Session(activity)
        paymentModelClass = PaymentModelClass(activity)
        sendParams = intent.getSerializableExtra("params") as MutableMap<String, String>
        payableAmount = sendParams[Constant.FINAL_TOTAL].toString().toDouble()
        from = sendParams[Constant.FROM].toString()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbarTitle.text = getString(R.string.paystack)
        findViewById<View>(R.id.imageHome).visibility = View.GONE
        imageMenu.visibility = View.VISIBLE
        imageMenu.setImageResource(R.drawable.ic_arrow_back)
        imageMenu.setOnClickListener {  onBackPressed() }
        emailField.setText(session.getData(Constant.EMAIL))
        tvPayable.text = session.getData(Constant.currency) + payableAmount
        findViewById<View>(R.id.btnPay).setOnClickListener { 
            if (!validateForm()) {
                return@setOnClickListener
            }
            try {
                email = emailField.text.toString()
                cardNumber =
                    Objects.requireNonNull(cardNumberField.text).toString()
                expiryMonth = expiryMonthField.text.toString().toInt()
                expiryYear = expiryYearField.text.toString().toInt()
                cvv = cvvField.text.toString()

                /*String cardNumber = "4084 0840 8408 4081";
            int expiryMonth = 11; //any month in the future
            int expiryYear = 18; // any year in the future
            String cvv = "408";*/card = Card(cardNumber, expiryMonth, expiryYear, cvv)
                paymentModelClass.showProgressDialog()
                if (card.isValid) {
                    performCharge()
                } else {
                    paymentModelClass.hideProgressDialog()
                    Toast.makeText(this@PayStackActivity, "Card is not Valid", Toast.LENGTH_LONG)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAllWidget() {
            toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            tvPayable = findViewById(R.id.tvPayable)
            emailField = findViewById(R.id.edit_email_address)
            cardNumberField = findViewById(R.id.edit_card_number)
            expiryMonthField = findViewById(R.id.edit_expiry_month)
            expiryYearField = findViewById(R.id.edit_expiry_year)
            cvvField = findViewById(R.id.edit_cvv)
            imageMenu = findViewById(R.id.imageMenu)
            toolbarTitle = findViewById(R.id.toolbarTitle)
            imageMenu = findViewById(R.id.imageMenu)
        }

    /**
     * Method to perform the charging of the card
     */
    private fun performCharge() {
        //create a  Charge object
        val amount = (payableAmount * 100).toString().split("\\.".toRegex()).toTypedArray()
        charge = Charge()
        charge.card = card //set the card to charge
        charge.email = email //dummy email address
        charge.amount = amount[0].toInt() //test amount
        PaystackSdk.chargeCard(this@PayStackActivity, charge, object : TransactionCallback {
            override fun onSuccess(transaction: Transaction) {
                val paymentReference = transaction.reference
                verifyReference(charge.amount.toString(), paymentReference, charge.email)
            }

            override fun beforeValidate(transaction: Transaction) {}
            override fun onError(error: Throwable, transaction: Transaction) {}
        })
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = emailField.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailField.error = "Required."
            valid = false
        } else {
            emailField.error = null
        }
        val cardNumber = Objects.requireNonNull(cardNumberField.text).toString()
        if (TextUtils.isEmpty(cardNumber)) {
            cardNumberField.error = "Required."
            valid = false
        } else {
            cardNumberField.error = null
        }
        val expiryMonth = expiryMonthField.text.toString()
        if (TextUtils.isEmpty(expiryMonth)) {
            expiryMonthField.error = "Required."
            valid = false
        } else {
            expiryMonthField.error = null
        }
        val expiryYear = expiryYearField.text.toString()
        if (TextUtils.isEmpty(expiryYear)) {
            expiryYearField.error = "Required."
            valid = false
        } else {
            expiryYearField.error = null
        }
        val cvv = cvvField.text.toString()
        if (TextUtils.isEmpty(cvv)) {
            cvvField.error = "Required."
            valid = false
        } else {
            cvvField.error = null
        }
        return valid
    }

    fun verifyReference(amount: String, reference: String, email: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.VERIFY_PAY_STACK] = Constant.GetVal
        params[Constant.AMOUNT] = amount
        params[Constant.REFERENCE] = reference
        params[Constant.EMAIL] = email
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getString(Constant.STATUS)
                        if (from == Constant.WALLET) {
                            onBackPressed()
                            WalletTransactionFragment().addWalletBalance(
                                activity,
                                Session(activity),
                                WalletTransactionFragment.amount,
                                WalletTransactionFragment.msg
                            )
                        } else if (from == Constant.PAYMENT) {
                            paymentModelClass.placeOrder(
                                activity,
                                getString(R.string.paystack),
                                reference,
                                status.equals("success", ignoreCase = true),
                                sendParams,
                                status
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.VERIFY_PAYMENT_REQUEST, params, false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    companion object {
        fun setPaystackKey(publicKey: String) {
            PaystackSdk.setPublicKey(publicKey)
        }
    }
}