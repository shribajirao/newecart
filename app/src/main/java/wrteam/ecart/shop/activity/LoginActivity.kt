package wrteam.ecart.shop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.hbb20.CountryCodePicker
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInSaveForLater
import wrteam.ecart.shop.helper.ApiConfig.Companion.addOrRemoveFavorite
import wrteam.ecart.shop.helper.ApiConfig.Companion.checkValidation
import wrteam.ecart.shop.helper.ApiConfig.Companion.getCartItemCount
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Constant.randomAlphaNumeric
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setHideShowPassword
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.ui.Pinview
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    lateinit var lytOTP: LinearLayout
    private lateinit var edtResetPass: EditText
    private lateinit var edtResetCPass: EditText
    private lateinit var edtRefer: EditText
    private lateinit var edtLoginPassword: EditText
    private lateinit var edtLoginMobile: EditText
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    lateinit var edtMobileVerify: EditText
    lateinit var btnVerify: Button
    private lateinit var btnResetPass: Button
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    lateinit var edtCountryCodePicker: CountryCodePicker
    lateinit var pinViewOTP: Pinview
    private lateinit var tvSignUp: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvWelcome: TextView
    lateinit var tvTimer: TextView
    lateinit var tvResend: TextView
    private lateinit var tvForgotPass: TextView
    private lateinit var tvPrivacyPolicy: TextView
    private lateinit var lytLogin: ScrollView
    private lateinit var lytSignUp: ScrollView
    private lateinit var lytVerify: ScrollView
    private lateinit var lytResetPass: ScrollView
    lateinit var lytWebView: ScrollView
    lateinit var session: Session
    lateinit var toolbar: Toolbar
    private lateinit var chPrivacy: CheckBox
    lateinit var animShow: Animation
    private lateinit var animHide: Animation
    private lateinit var imgVerifyClose: ImageView
    private lateinit var imgResetPasswordClose: ImageView
    private lateinit var imgSignUpClose: ImageView
    private lateinit var imgWebViewClose: ImageView

    ////Firebase
    lateinit var phoneNumber: String
    var firebaseOtp = ""
    var otpFor = ""
    var resendOTP = false
    private lateinit var auth: FirebaseAuth
    private lateinit var mCallback: OnVerificationStateChangedCallbacks
    lateinit var databaseHelper: DatabaseHelper
    lateinit var activity: Activity
    lateinit var img: ImageView
    lateinit var webView: WebView
    lateinit var from: String
    lateinit var mobile: String
    private lateinit var countryCode: String
    lateinit var dialog: ProgressDialog
    private val forMultipleCountryUse = true
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activity = this@LoginActivity
        session = Session(activity)
        databaseHelper = DatabaseHelper(activity)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        animShow = AnimationUtils.loadAnimation(this, R.anim.view_show)
        animHide = AnimationUtils.loadAnimation(this, R.anim.view_hide)
        from = intent?.getStringExtra(Constant.FROM).toString()
        chPrivacy = findViewById(R.id.chPrivacy)
        tvWelcome = findViewById(R.id.tvWelcome)
        edtCountryCodePicker = findViewById(R.id.edtCountryCodePicker)
        edtResetPass = findViewById(R.id.edtResetPass)
        edtResetCPass = findViewById(R.id.edtResetCPass)
        edtLoginPassword = findViewById(R.id.edtLoginPassword)
        edtLoginMobile = findViewById(R.id.edtLoginMobile)
        lytLogin = findViewById(R.id.lytLogin)
        lytResetPass = findViewById(R.id.lytResetPass)
        lytVerify = findViewById(R.id.lytVerify)
        lytSignUp = findViewById(R.id.lytSignUp)
        lytOTP = findViewById(R.id.lytOTP)
        pinViewOTP = findViewById(R.id.pinViewOTP)
        btnVerify = findViewById(R.id.btnVerify)
        edtMobileVerify = findViewById(R.id.edtMobileVerify)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        tvMobile = findViewById(R.id.tvMobile)
        edtPassword = findViewById(R.id.edtPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        edtRefer = findViewById(R.id.edtRefer)
        tvResend = findViewById(R.id.tvResend)
        tvTimer = findViewById(R.id.tvTimer)
        tvForgotPass = findViewById(R.id.tvForgotPass)
        tvPrivacyPolicy = findViewById(R.id.tvPrivacy)
        img = findViewById(R.id.img)
        lytWebView = findViewById(R.id.lytWebView)
        webView = findViewById(R.id.webView)
        btnResetPass = findViewById(R.id.btnResetPass)
        btnVerify = findViewById(R.id.btnVerify)
        tvResend = findViewById(R.id.tvResend)
        tvSignUp = findViewById(R.id.tvSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        imgVerifyClose = findViewById(R.id.imgVerifyClose)
        imgResetPasswordClose = findViewById(R.id.imgResetPasswordClose)
        imgSignUpClose = findViewById(R.id.imgSignUpClose)
        imgWebViewClose = findViewById(R.id.imgWebViewClose)
        tvForgotPass.text = underlineSpannable(getString(R.string.forgot_text))
        edtLoginMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone, 0, 0, 0)
        edtLoginPassword.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        edtConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        edtResetPass.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        edtResetCPass.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        setHideShowPassword(edtPassword)
        setHideShowPassword(edtConfirmPassword)
        setHideShowPassword(edtLoginPassword)
        setHideShowPassword(edtResetPass)
        setHideShowPassword(edtResetCPass)
        lytResetPass.visibility = View.GONE
        lytLogin.visibility = View.VISIBLE
        lytVerify.visibility = View.GONE
        lytSignUp.visibility = View.GONE
        lytOTP.visibility = View.GONE
        lytWebView.visibility = View.GONE
        tvWelcome.text = getString(R.string.welcome) + getString(R.string.app_name)
        edtCountryCodePicker.setCountryForNameCode("IN")

//        forMultipleCountryUse = false;
        when (from) {
            "drawer", "checkout", "tracker" -> {
                lytLogin.visibility = View.VISIBLE
                lytLogin.startAnimation(animShow)
                Handler().postDelayed({ edtLoginMobile.requestFocus() }, 500)
            }
            "refer" -> {
                otpFor = "new_user"
                lytVerify.visibility = View.VISIBLE
                lytVerify.startAnimation(animShow)
                Handler().postDelayed({ edtMobileVerify.requestFocus() }, 500)
            }
            else -> {
                lytVerify.visibility = View.GONE
                lytResetPass.visibility = View.GONE
                lytVerify.visibility = View.GONE
                lytLogin.visibility = View.GONE
                lytSignUp.visibility = View.VISIBLE
                tvMobile.text = mobile
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tvSignUp.setOnClickListener {
            otpFor = "new_user"
            edtMobileVerify.setText("")
            edtMobileVerify.isEnabled = true
            edtCountryCodePicker.setCcpClickable(forMultipleCountryUse)
            lytOTP.visibility = View.GONE
            lytVerify.visibility = View.VISIBLE
            lytVerify.startAnimation(animShow)
        }
        tvForgotPass.setOnClickListener {
            otpFor = "exist_user"
            edtMobileVerify.setText("")
            edtMobileVerify.isEnabled = true
            edtCountryCodePicker.setCcpClickable(forMultipleCountryUse)
            lytOTP.visibility = View.GONE
            lytVerify.visibility = View.VISIBLE
            lytVerify.startAnimation(animShow)
        }
        btnResetPass.setOnClickListener {
            hideKeyboard(activity, btnResetPass)
            resetPassword()
        }
        btnLogin.setOnClickListener {
            mobile = edtLoginMobile.text.toString()
            val password = edtLoginPassword.text.toString()
            when {
                checkValidation(mobile, isMailValidation = false, isMobileValidation = false) -> {
                    edtLoginMobile.requestFocus()
                    edtLoginMobile.error = getString(R.string.enter_mobile_no)
                }
                checkValidation(mobile, isMailValidation = false, isMobileValidation = true) -> {
                    edtLoginMobile.requestFocus()
                    edtLoginMobile.error = getString(R.string.enter_valid_mobile_no)
                }
                checkValidation(password, isMailValidation = false, isMobileValidation = false) -> {
                    edtLoginPassword.requestFocus()
                    edtLoginPassword.error = getString(R.string.enter_pass)
                }
                isConnected(activity) -> {
                    userLogin(mobile, password)
                }
            }
        }
        btnVerify.setOnClickListener {
            if (lytOTP.visibility == View.GONE) {
                hideKeyboard(activity, btnVerify)
                mobile = edtMobileVerify.text.toString()
                countryCode = edtCountryCodePicker.selectedCountryCode
                when {
                    checkValidation(mobile, isMailValidation = false, isMobileValidation = false) -> {
                        edtMobileVerify.requestFocus()
                        edtMobileVerify.error = getString(R.string.enter_mobile_no)
                    }
                    checkValidation(mobile,
                        isMailValidation = false,
                        isMobileValidation = true
                    ) -> {
                        edtMobileVerify.requestFocus()
                        edtMobileVerify.error = getString(R.string.enter_valid_mobile_no)
                    }
                    isConnected(activity) -> {
                        generateOTP()
                    }
                }
            } else {
                val otpText =
                    Objects.requireNonNull(pinViewOTP.value).toString()
                if (checkValidation(otpText, isMailValidation = false, isMobileValidation = false)) {
                    pinViewOTP.requestFocus()
                    Toast.makeText(activity,getString(R.string.enter_otp),Toast.LENGTH_SHORT).show()
                } else {
                    otpVerification(otpText)
                }
            }
        }
        btnRegister.setOnClickListener {
            val name = edtName.text.toString()
            val email = "" + edtEmail.text.toString()
            val password = edtPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()
            if (checkValidation(name, isMailValidation = false, isMobileValidation = false)) {
                edtName.requestFocus()
                edtName.error = getString(R.string.enter_name)
            } else if (checkValidation(email, isMailValidation = false, isMobileValidation = false)) {
                edtEmail.requestFocus()
                edtEmail.error = getString(R.string.enter_email)
            } else if (checkValidation(email, isMailValidation = true, isMobileValidation = false)) {
                edtEmail.requestFocus()
                edtEmail.error = getString(R.string.enter_valid_email)
            } else if (checkValidation(password,
                    isMailValidation = false,
                    isMobileValidation = false
                )) {
                edtConfirmPassword.requestFocus()
                edtPassword.error = getString(R.string.enter_pass)
            } else if (checkValidation(confirmPassword,
                    isMailValidation = false,
                    isMobileValidation = false
                )) {
                edtConfirmPassword.requestFocus()
                edtConfirmPassword.error = getString(R.string.enter_confirm_pass)
            } else if (password != confirmPassword) {
                edtConfirmPassword.requestFocus()
                edtConfirmPassword.error = getString(R.string.pass_not_match)
            } else if (!chPrivacy.isChecked) {
                Toast.makeText(activity, getString(R.string.alert_privacy_msg), Toast.LENGTH_LONG)
                    .show()
            } else if (isConnected(activity)) {
                userSignUpSubmit(name, email, password)
            }
        }
        tvResend.setOnClickListener {
            resendOTP = true
            sentRequest("+" + session.getData(Constant.COUNTRY_CODE) + mobile)
        }
        imgVerifyClose.setOnClickListener {
            lytOTP.visibility = View.GONE
            lytVerify.visibility = View.GONE
            lytVerify.startAnimation(animHide)
            edtMobileVerify.setText("")
            edtMobileVerify.isEnabled = true
            edtCountryCodePicker.setCcpClickable(forMultipleCountryUse)
            pinViewOTP.clearValue()
        }
        imgResetPasswordClose.setOnClickListener {
            edtResetPass.setText("")
            edtResetCPass.setText("")
            lytResetPass.visibility = View.GONE
            lytResetPass.startAnimation(animHide)
        }
        imgSignUpClose.setOnClickListener {
            lytSignUp.visibility = View.GONE
            lytSignUp.startAnimation(animHide)
            tvMobile.text = ""
            edtName.setText("")
            edtEmail.setText("")
            edtPassword.setText("")
            edtConfirmPassword.setText("")
            edtRefer.setText("")
        }
        imgWebViewClose.setOnClickListener {
            lytWebView.visibility = View.GONE
            lytWebView.startAnimation(animHide)
        }
        startFirebaseLogin()
        privacyPolicy()
    }

    private fun generateOTP() {
        dialog = ProgressDialog.show(activity, "", getString(R.string.please_wait), true)
        session.setData(Constant.COUNTRY_CODE, countryCode)
        val params: MutableMap<String, String> = HashMap()
        params[Constant.TYPE] = Constant.VERIFY_USER
        params[Constant.MOBILE] = mobile
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        phoneNumber = "+" + session.getData(Constant.COUNTRY_CODE) + mobile
                        if (otpFor == "new_user") {
                            if (jsonObject.getBoolean(Constant.ERROR)) {
                                dialog.dismiss()
                                setSnackBar(
                                    getString(R.string.alert_register_num1) + getString(R.string.app_name) + getString(
                                        R.string.alert_register_num2
                                    ), getString(R.string.btn_ok), ""
                                )
                            } else {
                                sentRequest(phoneNumber)
                            }
                        } else if (otpFor == "exist_user") {
                            if (jsonObject.getBoolean(Constant.ERROR)) {
                                Constant.U_ID = jsonObject.getString(Constant.ID)
                                sentRequest(phoneNumber)
                            } else {
                                dialog.dismiss()
                                setSnackBar(
                                    getString(R.string.alert_not_register_num1) + getString(R.string.app_name) + getString(
                                        R.string.alert_not_register_num2
                                    ), getString(R.string.btn_ok), ""
                                )
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.RegisterUrl, params, false)
    }

    fun sentRequest(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startFirebaseLogin() {
        auth = FirebaseAuth.getInstance()
        mCallback = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //System.out.println ("====verification complete call  " + phoneAuthCredential.getSmsCode ());
            }

            override fun onVerificationFailed(e: FirebaseException) {
                setSnackBar(e.localizedMessage!!, getString(R.string.btn_ok), "")
            }

            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                dialog.dismiss()
                firebaseOtp = s
                pinViewOTP.requestFocus()
                if (resendOTP) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.otp_resend_alert),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    edtMobileVerify.isEnabled = false
                    edtCountryCodePicker.setCcpClickable(false)
                    btnVerify.text = getString(R.string.verify_otp)
                    lytOTP.visibility = View.VISIBLE
                    lytOTP.startAnimation(animShow)
                    object : CountDownTimer(120000, 1000) {
                        @SuppressLint("SetTextI18n")
                        override fun onTick(millisUntilFinished: Long) {
                            // Used for formatting digit to be in 2 digits only
                            val f: NumberFormat = DecimalFormat("00")
                            val min = millisUntilFinished / 60000 % 60
                            val sec = millisUntilFinished / 1000 % 60
                            tvTimer.text = f.format(min) + ":" + f.format(sec)
                        }

                        override fun onFinish() {
                            resendOTP = false
                            tvTimer.visibility = View.GONE
                            img.setColorFilter(
                                ContextCompat.getColor(
                                    activity, R.color.colorPrimary
                                )
                            )
                            tvResend.setTextColor(
                                ContextCompat.getColor(
                                    activity, R.color.colorPrimary
                                )
                            )
                            tvResend.setOnClickListener { 
                                resendOTP = true
                                sentRequest("+" + session.getData(Constant.COUNTRY_CODE) + mobile)
                                object : CountDownTimer(120000, 1000) {
                                    @SuppressLint("SetTextI18n")
                                    override fun onTick(millisUntilFinished: Long) {
                                        tvTimer.visibility = View.VISIBLE
                                        img.setColorFilter(
                                            ContextCompat.getColor(
                                                activity, R.color.gray
                                            )
                                        )
                                        tvResend.setTextColor(
                                            ContextCompat.getColor(
                                                activity, R.color.gray
                                            )
                                        )

                                        // Used for formatting digit to be in 2 digits only
                                        val f: NumberFormat = DecimalFormat("00")
                                        val min = millisUntilFinished / 60000 % 60
                                        val sec = millisUntilFinished / 1000 % 60
                                        tvTimer.text = f.format(min) + ":" + f.format(sec)
                                    }

                                    override fun onFinish() {
                                        resendOTP = false
                                        tvTimer.visibility = View.GONE
                                        img.setColorFilter(
                                            ContextCompat.getColor(
                                                activity, R.color.colorPrimary
                                            )
                                        )
                                        tvResend.setTextColor(
                                            ContextCompat.getColor(
                                                activity, R.color.colorPrimary
                                            )
                                        )
                                        tvResend.setOnClickListener {
                                            resendOTP = true
                                            sentRequest("+" + session.getData(Constant.COUNTRY_CODE) + mobile)
                                        }
                                    }
                                }.start()
                            }
                        }
                    }.start()
                }
            }
        }
    }

    private fun resetPassword() {
        val resetPassword = edtResetPass.text.toString()
        val resetConfirmPassword = edtResetCPass.text.toString()
        when {
            checkValidation(resetPassword, isMailValidation = false, isMobileValidation = false) -> {
                edtResetPass.requestFocus()
                edtResetPass.error = getString(R.string.enter_new_pass)
            }
            checkValidation(resetConfirmPassword,
                isMailValidation = false,
                isMobileValidation = false
            ) -> {
                edtResetCPass.requestFocus()
                edtResetCPass.error = getString(R.string.enter_confirm_pass)
            }
            resetPassword != resetConfirmPassword -> {
                edtResetCPass.requestFocus()
                edtResetCPass.error = getString(R.string.pass_not_match)
            }
            isConnected(activity) -> {
                val params: MutableMap<String, String> = HashMap()
                params[Constant.TYPE] = Constant.CHANGE_PASSWORD
                params[Constant.PASSWORD] = resetConfirmPassword
                //params.put(Constant.ID, session.getData(Constant.ID));
                params[Constant.ID] = Constant.U_ID
                val alertDialog = AlertDialog.Builder(
                    activity
                )
                // Setting Dialog Message
                alertDialog.setTitle(getString(R.string.reset_pass))
                alertDialog.setMessage(getString(R.string.reset_alert_msg))
                alertDialog.setCancelable(false)
                val alertDialog1 = alertDialog.create()
                // Setting OK Button
                alertDialog.setPositiveButton(getString(R.string.yes)) { _: DialogInterface, _: Int ->
                    requestToVolley(
                        object : VolleyCallback {
                            override fun onSuccess(result: Boolean, response: String) {
                                if (result) {
                                    try {
                                        val jsonObject = JSONObject(response)
                                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                                            setSnackBar(
                                                getString(R.string.msg_reset_pass_success),
                                                getString(R.string.btn_ok),
                                                "reset_pass"
                                            )
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }, activity, Constant.RegisterUrl, params, true
                    )
                }
                alertDialog.setNegativeButton(getString(R.string.no)) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
                // Showing Alert Message
                alertDialog.show()
            }
        }
    }

    private fun userLogin(mobile: String, password: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.MOBILE] = mobile
        params[Constant.PASSWORD] = password
        params[Constant.FCM_ID] = "" + session.getData(Constant.FCM_ID)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                //System.out.println ("============login res " + response);
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            startMainActivity(jsonObject, password)
                        }
                        Toast.makeText(
                            activity,
                            jsonObject.getString(Constant.MESSAGE),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.LoginUrl, params, true)
    }

    fun setSnackBar(message: String, action: String, from: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(action) {
            if (from == "reset_pass") {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            snackBar.dismiss()
        }
        snackBar.setActionTextColor(Color.RED)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 5
        snackBar.show()
    }

    @SuppressLint("SetTextI18n")
    fun otpVerification(OTPText: String) {
        val credential = PhoneAuthProvider.getCredential(firebaseOtp, OTPText)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    if (otpFor == "new_user") {
                        tvMobile.text =
                            "+" + session.getData(Constant.COUNTRY_CODE) + " " + mobile
                        lytSignUp.visibility = View.VISIBLE
                        edtRefer.setText(Constant.FRIEND_CODE_VALUE)
                        lytSignUp.startAnimation(animShow)
                    }
                    if (otpFor == "exist_user") {
                        lytResetPass.visibility = View.VISIBLE
                        lytResetPass.startAnimation(animShow)
                        //                            System.out.println("lytResetPass.getVisibility() : " + lytResetPass.getVisibility() + ", " + View.VISIBLE + ", " + View.GONE);
                    }
                } else {
                    //verification unsuccessful.. display an error message
                    var message = activity.getString(R.string.otp_error_message)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        message = activity.getString(R.string.invalid_otp_enter)
                    }
                    pinViewOTP.requestFocus()
                    Toast.makeText(activity,message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun userSignUpSubmit(name: String, email: String, password: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.TYPE] = Constant.REGISTER
        params[Constant.NAME] = name
        params[Constant.EMAIL] = email
        params[Constant.PASSWORD] = password
        params[Constant.COUNTRY_CODE] = session.getData(Constant.COUNTRY_CODE)
        params[Constant.MOBILE] = mobile
        params[Constant.FCM_ID] = "" + session.getData(Constant.FCM_ID)
        params[Constant.REFERRAL_CODE] = randomAlphaNumeric(8)
        params[Constant.FRIEND_CODE] = edtRefer.text.toString()
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            startMainActivity(jsonObject, password)
                        }
                        Toast.makeText(
                            activity,
                            jsonObject.getString(Constant.MESSAGE),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.RegisterUrl, params, true)
    }

    fun startMainActivity(jsonObject: JSONObject, password: String) {
        try {
            Session(activity).createUserLoginSession(
                jsonObject.getString(Constant.PROFILE),
                session.getData(Constant.FCM_ID),
                jsonObject.getString(Constant.USER_ID),
                jsonObject.getString(Constant.NAME),
                jsonObject.getString(Constant.EMAIL),
                jsonObject.getString(Constant.MOBILE),
                password,
                jsonObject.getString(Constant.REFERRAL_CODE),
                jsonObject.getString(Constant.BALANCE)
            )
            addMultipleProductInCart(session, activity, databaseHelper.cartData)
            addMultipleProductInSaveForLater(
                session,
                activity,
                databaseHelper.saveForLaterData
            )
            getCartItemCount(activity, session)
            val favorites = databaseHelper.favorite
            for (i in favorites.indices) {
                addOrRemoveFavorite(activity, session, favorites[i], true)
            }
            databaseHelper.deleteAllFavoriteData()
            databaseHelper.clearCart()
            databaseHelper.clearSaveForLater()
            session.setData(Constant.COUNTRY_CODE, jsonObject.getString(Constant.COUNTRY_CODE))
            MainActivity.homeClicked = false
            MainActivity.categoryClicked = false
            MainActivity.favoriteClicked = false
            MainActivity.drawerClicked = false
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Constant.FROM, "")
            if (from == "checkout") {
                intent.putExtra("total", stringFormat("" + Constant.FLOAT_TOTAL_AMOUNT))
                intent.putExtra(Constant.FROM, "checkout")
            } else if (from == "tracker") {
                intent.putExtra(Constant.FROM, "tracker")
            }
            startActivity(intent)
            finish()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun underlineSpannable(text: String): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)
        return spannableString
    }

    fun getContent(type: String, key: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SETTINGS] = Constant.GetVal
        params[type] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val obj = JSONObject(response)
                        if (!obj.getBoolean(Constant.ERROR)) {
                            val privacyStr = obj.getString(key)
                            webView.isVerticalScrollBarEnabled = true
                            webView.loadDataWithBaseURL("", privacyStr, "text/html", "UTF-8", "")
                        } else {
                            Toast.makeText(
                                activity,
                                obj.getString(Constant.MESSAGE),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false)
    }

    private fun privacyPolicy() {
        tvPrivacyPolicy.isClickable = true
        tvPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        val message = getString(R.string.msg_privacy_terms)
        val s2 = getString(R.string.terms_conditions)
        val s1 = getString(R.string.privacy_policy)
        val wordToSpan: Spannable = SpannableString(message)
        wordToSpan.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                getContent(Constant.GET_PRIVACY, "privacy")
                try {
                    Thread.sleep(500)
                    lytWebView.visibility = View.VISIBLE
                    lytWebView.startAnimation(animShow)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                ds.isUnderlineText
            }
        }, message.indexOf(s1), message.indexOf(s1) + s1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        wordToSpan.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                getContent(Constant.GET_TERMS, "terms")
                try {
                    Thread.sleep(500)
                    lytWebView.visibility = View.VISIBLE
                    lytWebView.startAnimation(animShow)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                ds.isUnderlineText
            }
        }, message.indexOf(s2), message.indexOf(s2) + s2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvPrivacyPolicy.text = wordToSpan
    }

    fun hideKeyboard(activity: Activity, root: View) {
        try {
            val inputMethodManager =
                (activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            inputMethodManager.hideSoftInputFromWindow(root.applicationWindowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public override fun onPause() {
        super.onPause()
    }
}