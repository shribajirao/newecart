package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.LoginActivity
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.helper.ApiConfig.Companion.checkValidation
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setHideShowPassword
import wrteam.ecart.shop.helper.VolleyCallback

class DrawerFragment : Fragment() {
    lateinit var root: View
    private lateinit var tvMobile: TextView
    private lateinit var tvMenuHome: TextView
    private lateinit var tvMenuCart: TextView
    private lateinit var tvMenuNotification: TextView
    private lateinit var tvMenuOrders: TextView
    private lateinit var tvMenuWalletHistory: TextView
    private lateinit var tvMenuTransactionHistory: TextView
    private lateinit var tvMenuChangePassword: TextView
    private lateinit var tvMenuManageAddresses: TextView
    private lateinit var tvMenuReferEarn: TextView
    private lateinit var tvMenuSupport: TextView
    private lateinit var tvMenuContactUs: TextView
    private lateinit var tvMenuBlog: TextView
    private lateinit var tvMenuAboutUs: TextView
    private lateinit var tvMenuRateUs: TextView
    private lateinit var tvMenuShareApp: TextView
    private lateinit var tvMenuFAQ: TextView
    private lateinit var tvMenuTermsConditions: TextView
    private lateinit var tvMenuPrivacyPolicy: TextView
    private lateinit var tvMenuLogout: TextView
    private lateinit var imgEditProfile: ImageView
    lateinit var session: Session
    lateinit var activity: Activity
    private lateinit var lytMenuGroup: LinearLayout
    private lateinit var lytProfile: LinearLayout
    lateinit var fragment: Fragment
    lateinit var bundle: Bundle

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_drawer, container, false)
        activity = requireActivity()
        session = Session(activity)
        imgProfile = root.findViewById(R.id.imgProfile)
        imgEditProfile = root.findViewById(R.id.imgEditProfile)
        tvName = root.findViewById(R.id.tvName)
        tvWallet = root.findViewById(R.id.tvWallet)
        tvMobile = root.findViewById(R.id.tvMobile)
        tvMenuHome = root.findViewById(R.id.tvMenuHome)
        tvMenuCart = root.findViewById(R.id.tvMenuCart)
        tvMenuNotification = root.findViewById(R.id.tvMenuNotification)
        tvMenuOrders = root.findViewById(R.id.tvMenuOrders)
        tvMenuWalletHistory = root.findViewById(R.id.tvMenuWalletHistory)
        tvMenuTransactionHistory = root.findViewById(R.id.tvMenuTransactionHistory)
        tvMenuChangePassword = root.findViewById(R.id.tvMenuChangePassword)
        tvMenuManageAddresses = root.findViewById(R.id.tvMenuManageAddresses)
        tvMenuReferEarn = root.findViewById(R.id.tvMenuReferEarn)
        tvMenuContactUs = root.findViewById(R.id.tvMenuContactUs)
        tvMenuBlog = root.findViewById(R.id.tvMenuBlog)
        tvMenuAboutUs = root.findViewById(R.id.tvMenuAboutUs)
        tvMenuRateUs = root.findViewById(R.id.tvMenuRateUs)
        tvMenuShareApp = root.findViewById(R.id.tvMenuShareApp)
        tvMenuFAQ = root.findViewById(R.id.tvMenuFAQ)
        tvMenuTermsConditions = root.findViewById(R.id.tvMenuTermsConditions)
        tvMenuPrivacyPolicy = root.findViewById(R.id.tvMenuPrivacyPolicy)
        tvMenuLogout = root.findViewById(R.id.tvMenuLogout)
        lytMenuGroup = root.findViewById(R.id.lytMenuGroup)
        lytProfile = root.findViewById(R.id.lytProfile)
        tvMenuSupport = root.findViewById(R.id.tvMenuSupport)
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            tvName.text = session.getData(Constant.NAME)
            tvMobile.text = session.getData(Constant.MOBILE)
            tvWallet.visibility = View.VISIBLE
            imgEditProfile.visibility = View.VISIBLE
            Picasso.get()
                .load(session.getData(Constant.PROFILE))
                .fit()
                .centerInside()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(imgProfile)
            tvWallet.text =
                activity.resources.getString(R.string.wallet_balance) + "\t:\t" + session.getData(
                    Constant.currency
                ) + session.getData(Constant.WALLET_BALANCE)
        } else {
            tvWallet.visibility = View.GONE
            imgEditProfile.visibility = View.GONE
            tvName.text = resources.getString(R.string.is_login)
            tvMobile.text = resources.getString(R.string.is_mobile)
            Picasso.get()
                .load("-")
                .fit()
                .centerInside()
                .placeholder(R.drawable.logo_login)
                .error(R.drawable.logo_login)
                .into(imgProfile)
        }
        imgEditProfile.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, ProfileFragment())
                .addToBackStack(null).commit()
        }
        lytProfile.setOnClickListener {
            if (!session.getBoolean(
                    Constant.IS_USER_LOGIN
                )
            ) {
                startActivity(
                    Intent(activity, LoginActivity::class.java).putExtra(
                        Constant.FROM,
                        "drawer"
                    )
                )
            }
        }
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (session.getData(Constant.is_refer_earn_on) == "0") {
                tvMenuReferEarn.visibility = View.GONE
            } else {
                tvMenuReferEarn.visibility = View.VISIBLE
            }
            if (session.getData(Constant.support_system) == "0") {
                tvMenuSupport.visibility = View.GONE
            } else {
                tvMenuSupport.visibility = View.VISIBLE
            }
            tvMenuLogout.visibility = View.VISIBLE
            lytMenuGroup.visibility = View.VISIBLE
        } else {
            tvMenuLogout.visibility = View.GONE
            lytMenuGroup.visibility = View.GONE
        }
        tvMenuHome.setOnClickListener {
            MainActivity.homeClicked = false
            MainActivity.categoryClicked = false
            MainActivity.favoriteClicked = false
            MainActivity.drawerClicked = false
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Constant.FROM, "")
            startActivity(intent)
        }
        tvMenuCart.setOnClickListener {
            fragment = CartFragment()
            bundle = Bundle()
            bundle.putString(Constant.FROM, "mainActivity")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        tvMenuNotification.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, NotificationFragment())
                .addToBackStack(null).commit()
        }
        tvMenuOrders.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, TrackOrderFragment())
                .addToBackStack(null).commit()
        }
        tvMenuWalletHistory.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, WalletTransactionFragment())
                .addToBackStack(null).commit()
        }
        tvMenuTransactionHistory.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, TransactionFragment())
                .addToBackStack(null).commit()
        }
        tvMenuChangePassword.setOnClickListener {
            openBottomDialog(
                activity
            )
        }
        tvMenuManageAddresses.setOnClickListener {
            fragment = AddressListFragment()
            bundle = Bundle()
            bundle.putString(Constant.FROM, "MainActivity")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        tvMenuReferEarn.setOnClickListener {
            if (session.getBoolean(
                    Constant.IS_USER_LOGIN
                )
            ) {
                MainActivity.fm.beginTransaction().add(R.id.container, ReferEarnFragment())
                    .addToBackStack(null).commit()
            } else {
                startActivity(Intent(activity, LoginActivity::class.java))
            }
        }
        tvMenuSupport.setOnClickListener {
            fragment = SupportTicketFragment()
            bundle = Bundle()
            bundle.putString("from", "")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        tvMenuContactUs.setOnClickListener {
            fragment = WebViewFragment()
            bundle = Bundle()
            bundle.putString("type", "Contact Us")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        with(tvMenuBlog) {
            setOnClickListener {
                MainActivity.fm.beginTransaction().add(R.id.container, BlogCategoryFragment())
                    .addToBackStack(null).commit()
            }
        }
        tvMenuAboutUs.setOnClickListener {
            fragment = WebViewFragment()
            bundle = Bundle()
            bundle.putString("type", "About Us")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        tvMenuRateUs.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Constant.PLAY_STORE_RATE_US_LINK + activity.packageName)
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Constant.PLAY_STORE_LINK + activity.packageName)
                    )
                )
            }
        }
        tvMenuShareApp.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.take_a_look) + "\"" + getString(R.string.app_name) + "\" - " + Constant.PLAY_STORE_LINK + activity.packageName
            )
            shareIntent.type = "text/plain"
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
        tvMenuFAQ.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, FaqFragment())
                .addToBackStack(null).commit()
        }
        tvMenuTermsConditions.setOnClickListener {
            fragment = WebViewFragment()
            bundle = Bundle()
            bundle.putString("type", "Terms & Conditions")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        tvMenuPrivacyPolicy.setOnClickListener {
            fragment = WebViewFragment()
            bundle = Bundle()
            bundle.putString("type", "Privacy Policy")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        tvMenuLogout.setOnClickListener {
            session.logoutUserConfirmation(
                activity
            )
        }
        return root
    }

    var dialogVisible = false;
    private fun openBottomDialog(activity: Activity) {
        if (!dialogVisible) {
            dialogVisible = true

            val sheetView = activity.layoutInflater.inflate(R.layout.dialog_change_password, null)

            val mBottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetTheme)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.window
                ?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val edtOldPassword = sheetView.findViewById<EditText>(R.id.edtOldPassword)
            val edtNewPassword = sheetView.findViewById<EditText>(R.id.edtNewPassword)
            val edtConfirmPassword = sheetView.findViewById<EditText>(R.id.edtConfirmPassword)
            val imgChangePasswordClose =
                sheetView.findViewById<ImageView>(R.id.imgChangePasswordClose)
            val btnChangePassword = sheetView.findViewById<Button>(R.id.btnChangePassword)
            edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pass,
                0,
                R.drawable.ic_show,
                0
            )
            edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(
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
            setHideShowPassword(edtOldPassword)
            setHideShowPassword(edtNewPassword)
            setHideShowPassword(edtConfirmPassword)
            mBottomSheetDialog.setCancelable(true)
            imgChangePasswordClose.setOnClickListener { mBottomSheetDialog.dismiss() }
            btnChangePassword.setOnClickListener {
                val oldPassword = edtOldPassword.text.toString()
                val password = edtNewPassword.text.toString()
                val confirmPassword = edtConfirmPassword.text.toString()
                when {
                    password != confirmPassword -> {
                        edtConfirmPassword.requestFocus()
                        edtConfirmPassword.error = activity.getString(R.string.pass_not_match)
                    }
                    checkValidation(
                        oldPassword,
                        isMailValidation = false,
                        isMobileValidation = false
                    ) -> {
                        edtOldPassword.requestFocus()
                        edtOldPassword.error = activity.getString(R.string.enter_old_pass)
                    }
                    checkValidation(
                        password,
                        isMailValidation = false,
                        isMobileValidation = false
                    ) -> {
                        edtNewPassword.requestFocus()
                        edtNewPassword.error = activity.getString(R.string.enter_new_pass)
                    }
                    oldPassword != Session(activity).getData(Constant.PASSWORD) -> {
                        edtOldPassword.requestFocus()
                        edtOldPassword.error = activity.getString(R.string.no_match_old_pass)
                    }
                    isConnected(activity) -> {
                        changePassword(password)
                    }
                }
            }

            mBottomSheetDialog.setOnCancelListener { dialogVisible = false }
            mBottomSheetDialog.show()
        }
    }

    private fun changePassword(password: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.TYPE] = Constant.CHANGE_PASSWORD
        params[Constant.PASSWORD] = password
        params[Constant.ID] = session.getData(Constant.ID)
        val alertDialog = AlertDialog.Builder(activity)
        // Setting Dialog Message
        alertDialog.setTitle(getString(R.string.change_pass))
        alertDialog.setMessage(getString(R.string.reset_alert_msg))
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()

        // Setting OK
        alertDialog.setPositiveButton(getString(R.string.yes)) { _: DialogInterface, _: Int ->
            requestToVolley(
                object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {
                        if (result) {
                            try {
                                val jsonObject = JSONObject(response)
                                if (!jsonObject.getBoolean(Constant.ERROR)) {
                                    session.logoutUser(activity)
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
                }, activity, Constant.RegisterUrl, params, true
            )
        }
        alertDialog.setNegativeButton(getString(R.string.no)) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var tvName: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var tvWallet: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var imgProfile: ImageView
    }
}