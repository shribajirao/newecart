package wrteam.ecart.shop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.razorpay.PaymentResultListener
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.OfferAdapter
import wrteam.ecart.shop.fragment.*
import wrteam.ecart.shop.helper.ApiConfig.Companion.buildCounterDrawable
import wrteam.ecart.shop.helper.ApiConfig.Companion.getCartItemCount
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import java.util.*

class MainActivity : AppCompatActivity(), PaymentResultListener {
    lateinit var session: Session
    private var doubleBackToExitPressedOnce = false
    lateinit var menu: Menu
    lateinit var databaseHelper: DatabaseHelper
    lateinit var from: String
    lateinit var toolbarTitle: TextView
    private lateinit var imageMenu: ImageView
    private lateinit var imageHome: ImageView
    private lateinit var cardViewHamburger: CardView
    @SuppressLint("NonConstantResourceId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        imageMenu = findViewById(R.id.imageMenu)
        imageHome = findViewById(R.id.imageHome)
        cardViewHamburger = findViewById(R.id.cardViewHamburger)
        activity = this@MainActivity
        session = Session(activity)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        from = intent?.getStringExtra(Constant.FROM).toString()
        databaseHelper = DatabaseHelper(activity)
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            getCartItemCount(activity, session)
        } else {
            session.setData(Constant.STATUS, "1")
            databaseHelper.getTotalItemOfCart(activity)
        }
        setAppLocal() //Change you language code here
        fm = supportFragmentManager
        homeFragment = HomeFragment()
        categoryFragment = CategoryFragment()
        favoriteFragment = FavoriteFragment()
        trackOrderFragment = TrackOrderFragment()
        drawerFragment = DrawerFragment()
        val bundle = Bundle()
        bottomNavigationView.selectedItemId = R.id.navMain
        active = homeFragment
        homeClicked = true
        drawerClicked = false
        favoriteClicked = false
        categoryClicked = false
        try {
            if (intent.getStringExtra("json")?.isNotEmpty() == true) {
                bundle.putString("json", intent.getStringExtra("json"))
            }
            homeFragment.arguments = bundle
            fm.beginTransaction().add(R.id.container, homeFragment).commit()
        } catch (e: Exception) {
            fm.beginTransaction().add(R.id.container, homeFragment).commit()
        }
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val run = run setOnItemSelectedListener@{
                    when (item.itemId) {
                        R.id.navMain -> {
                            if (active !== homeFragment) {
                                bottomNavigationView.menu.findItem(item.itemId).isChecked = true
                                if (!homeClicked) {
                                    fm.beginTransaction().add(R.id.container, homeFragment)
                                        .show(homeFragment).hide(
                                            active
                                        ).commit()
                                    homeClicked = true
                                } else {
                                    fm.beginTransaction().show(homeFragment).hide(
                                        active
                                    ).commit()
                                }
                                active = homeFragment
                            }
                        }
                        R.id.navCategory -> {
                            if (active !== categoryFragment) {
                                bottomNavigationView.menu.findItem(item.itemId).isChecked = true
                                if (!categoryClicked) {
                                    fm.beginTransaction().add(R.id.container, categoryFragment).show(
                                        categoryFragment
                                    ).hide(
                                        active
                                    ).commit()
                                    categoryClicked = true
                                } else {
                                    fm.beginTransaction().show(categoryFragment).hide(
                                        active
                                    ).commit()
                                }
                                active = categoryFragment
                            }
                        }
                        R.id.navWishList -> {
                            if (active !== favoriteFragment) {
                                bottomNavigationView.menu.findItem(item.itemId).isChecked = true
                                if (!favoriteClicked) {
                                    fm.beginTransaction().add(R.id.container, favoriteFragment).show(
                                        favoriteFragment
                                    ).hide(
                                        active
                                    ).commit()
                                    favoriteClicked = true
                                } else {
                                    fm.beginTransaction().show(favoriteFragment).hide(
                                        active
                                    ).commit()
                                }
                                active = favoriteFragment
                            }
                        }
                        R.id.navProfile -> {
                            if (active !== drawerFragment) {
                                bottomNavigationView.menu.findItem(item.itemId).isChecked = true
                                if (!drawerClicked) {
                                    fm.beginTransaction().add(R.id.container, drawerFragment)
                                        .show(drawerFragment).hide(
                                            active
                                        ).commit()
                                    drawerClicked = true
                                } else {
                                    fm.beginTransaction().show(drawerFragment).hide(
                                        active
                                    ).commit()
                                }
                                active = drawerFragment
                            }
                        }
                    }
                    return@setOnItemSelectedListener false
                }
            run
        }
        when (from) {
            "checkout" -> {
                bottomNavigationView.visibility = View.GONE
                getCartItemCount(activity, session)
                val fragment: Fragment = AddressListFragment()
                val bundle00 = Bundle()
                bundle00.putString(Constant.FROM, "login")
                bundle00.putDouble(
                    "total",
                    stringFormat("" + Constant.FLOAT_TOTAL_AMOUNT).toDouble()
                )
                fragment.arguments = bundle00
                fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit()
            }
            "share" -> {
                val fragment0: Fragment = ProductDetailFragment()
                val bundle0 = Bundle()
                bundle0.putInt("variantPosition", intent.getIntExtra("variantPosition", 0))
                bundle0.putString("id", intent.getStringExtra("id"))
                bundle0.putString(Constant.FROM, "share")
                fragment0.arguments = bundle0
                fm.beginTransaction().add(R.id.container, fragment0).addToBackStack(null).commit()
            }
            "product" -> {
                val fragment1: Fragment = ProductDetailFragment()
                val bundle1 = Bundle()
                bundle1.putInt("variantPosition", intent.getIntExtra("variantPosition", 0))
                bundle1.putString("id", intent.getStringExtra("id"))
                bundle1.putString(Constant.FROM, "product")
                fragment1.arguments = bundle1
                fm.beginTransaction().add(R.id.container, fragment1).addToBackStack(null).commit()
            }
            "category" -> {
                val fragment2: Fragment = SubCategoryFragment()
                val bundle2 = Bundle()
                bundle2.putString("id", intent.getStringExtra("id"))
                bundle2.putString("name", intent.getStringExtra("name"))
                bundle2.putString(Constant.FROM, "category")
                fragment2.arguments = bundle2
                fm.beginTransaction().add(R.id.container, fragment2).addToBackStack(null).commit()
            }
            "order" -> {
                val fragment3: Fragment = OrderDetailFragment()
                val bundle3 = Bundle()
                bundle3.putSerializable("model", "")
                bundle3.putString("id", intent.getStringExtra("id"))
                fragment3.arguments = bundle3
                fm.beginTransaction().add(R.id.container, fragment3).addToBackStack(null).commit()
            }
            "tracker" -> fm.beginTransaction().add(R.id.container, TrackOrderFragment())
                .addToBackStack(null).commit()
            "payment_success" -> fm.beginTransaction().add(R.id.container, OrderPlacedFragment())
                .addToBackStack(null).commit()
            "wallet" -> fm.beginTransaction().add(R.id.container, WalletTransactionFragment())
                .addToBackStack(null).commit()
            "customer_notification" -> {
                val fragment4: Fragment = SupportTicketFragment()
                val bundle4 = Bundle()
                bundle4.putSerializable("model", intent.getSerializableExtra("model"))
                bundle4.putString("id", intent.getStringExtra("id"))
                bundle4.putString("from", "customer_notification")
                fragment4.arguments = bundle4
                fm.beginTransaction().add(R.id.container, fragment4).addToBackStack(null).commit()
            }
        }
        fm.addOnBackStackChangedListener {
            try {
                if (OfferAdapter.viewHolder.imgPlay.tag == "pause") {
                    OfferAdapter.viewHolder.imgPlay.setImageDrawable(
                        ContextCompat.getDrawable(
                            activity, R.drawable.ic_play
                        )
                    )
                    OfferAdapter.viewHolder.imgPlay.tag = "play"
                    OfferAdapter.mediaPlayer.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            toolbar.visibility = View.VISIBLE
            val currentFragment = fm.findFragmentById(R.id.container)
            currentFragment?.onResume()
        }
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
            session.setData(Constant.FCM_ID, token)
            registerFcm(token)
        }
        getProductNames()
    }

    private fun setAppLocal() {
        val resources = resources
        val dm = resources.displayMetrics
        val configuration = resources.configuration
        configuration.setLocale(Locale(Constant.LANGUAGE_CODE.lowercase(Locale.getDefault())))
        resources.updateConfiguration(configuration, dm)
        bottomNavigationView.layoutDirection =
            activity.resources.configuration.layoutDirection
    }

    private fun registerFcm(token: String) {
        val params: MutableMap<String, String> = HashMap()
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params[Constant.USER_ID] = session.getData(Constant.USER_ID)
        }

        params[Constant.FCM_ID] = token
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            session.setData(Constant.FCM_ID, token)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.REGISTER_DEVICE_URL, params, false)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        if (fm.backStackEntryCount == 0) {
            if (active !== homeFragment) {
                doubleBackToExitPressedOnce = false
                bottomNavigationView.selectedItemId = R.id.navMain
                homeClicked = true
                fm.beginTransaction().hide(active).show(
                    homeFragment
                ).commit()
                active = homeFragment
            } else {
                Toast.makeText(this, getString(R.string.exit_msg), Toast.LENGTH_SHORT).show()
                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_cart -> {
                fm.beginTransaction().add(R.id.container, CartFragment()).addToBackStack(null)
                    .commit()
            }
            R.id.toolbar_search -> {
                val fragment: Fragment = ProductListFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "search")
                bundle.putString(Constant.NAME, activity.getString(R.string.search))
                bundle.putString(Constant.ID, "")
                fragment.arguments = bundle
                fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit()
            }
            R.id.toolbar_logout -> {
                session.logoutUserConfirmation(activity)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_search).isVisible = true
        menu.findItem(R.id.toolbar_cart).icon =
            buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity)
        if (fm.backStackEntryCount > 0) {
            toolbarTitle.text = Constant.TOOLBAR_TITLE
            bottomNavigationView.visibility = View.GONE
            cardViewHamburger.setCardBackgroundColor(getColor(R.color.colorPrimaryLight))
            imageMenu.setOnClickListener {  fm.popBackStack() }
            imageMenu.visibility = View.VISIBLE
            imageHome.visibility = View.GONE
        } else {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                toolbarTitle.text =
                    getString(R.string.hi) + session.getData(Constant.NAME) + "!"
            } else {
                toolbarTitle.text = getString(R.string.hi_user)
            }
            bottomNavigationView.visibility = View.VISIBLE
            cardViewHamburger.setCardBackgroundColor(getColor(R.color.transparent))
            imageMenu.visibility = View.GONE
            imageHome.visibility = View.VISIBLE
        }
        invalidateOptionsMenu()
        return super.onPrepareOptionsMenu(menu)
    }

    private fun getProductNames() {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_ALL_PRODUCTS_NAME] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            session.setData(
                                Constant.GET_ALL_PRODUCTS_NAME,
                                jsonObject.getString(Constant.DATA)
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPaymentSuccess(razorpayPaymentID: String) {
        try {
            WalletTransactionFragment.payFromWallet = false
            WalletTransactionFragment().addWalletBalance(
                activity,
                Session(activity),
                WalletTransactionFragment.amount,
                WalletTransactionFragment.msg
            )
        } catch (e: Exception) {
            Log.d("tag", "onPaymentSuccess  ", e)
        }
    }

    override fun onPaymentError(code: Int, response: String) {
        try {
            Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.d("tag", "onPaymentError  ", e)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var toolbar: Toolbar
        lateinit var bottomNavigationView: BottomNavigationView
        lateinit var active: Fragment
        lateinit var fm: FragmentManager
        lateinit var homeFragment: Fragment
        lateinit var categoryFragment: Fragment
        lateinit var favoriteFragment: Fragment
        lateinit var trackOrderFragment: Fragment
        lateinit var drawerFragment: Fragment
        var homeClicked = false
        var categoryClicked = false
        var favoriteClicked = false
        var drawerClicked = false
        @SuppressLint("StaticFieldLeak")
        lateinit var activity: Activity
    }
}