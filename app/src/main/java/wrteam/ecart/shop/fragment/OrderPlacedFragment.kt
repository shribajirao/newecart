package wrteam.ecart.shop.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback

class OrderPlacedFragment : Fragment() {
    lateinit var root: View
    lateinit var activity: Activity
    lateinit var progressBar: ProgressBar
    lateinit var btnShopping: Button
    lateinit var btnSummary: Button
    lateinit var lottieAnimationView: LottieAnimationView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_order_placed, container, false)
        activity = requireActivity()
        val session = Session(activity)
        progressBar = root.findViewById(R.id.progressBar)
        btnShopping = root.findViewById(R.id.btnShopping)
        btnSummary = root.findViewById(R.id.btnSummary)
        lottieAnimationView = root.findViewById(R.id.lottieAnimationView)
        setHasOptionsMenu(true)
        btnShopping.isEnabled = false
        btnSummary.isEnabled = false
        removeAllItemFromCart(activity, session)
        return root
    }

    private fun removeAllItemFromCart(activity: Activity, session: Session) {
        progressBar.visibility = View.VISIBLE
        val params: MutableMap<String, String> = HashMap()
        params[Constant.REMOVE_FROM_CART] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] = session.getData(
            Constant.ID
        )
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            getCartItemCount(activity, session)
                        }
                        progressBar.visibility = View.GONE
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }, activity, Constant.CART_URL, params, false)
    }

    fun getCartItemCount(activity: Activity, session: Session) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_USER_CART] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] = session.getData(
            Constant.ID
        )
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            Constant.TOTAL_CART_ITEM = jsonObject.getString(Constant.TOTAL).toInt()
                        } else {
                            Constant.TOTAL_CART_ITEM = 0
                        }
                        Constant.CartValues.clear()
                        lottieAnimationView.playAnimation()
                        btnShopping.backgroundTintList = ContextCompat.getColorStateList(
                            activity, R.color.colorPrimary
                        )
                        btnSummary.backgroundTintList = ContextCompat.getColorStateList(
                            activity, R.color.colorPrimary
                        )
                        btnShopping.isEnabled = true
                        btnSummary.isEnabled = true
                        btnShopping.setOnClickListener {
                            startActivity(
                                Intent(activity, MainActivity::class.java).putExtra(
                                    Constant.FROM, ""
                                )
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            activity.finish()
                        }
                        btnSummary.setOnClickListener {
                            startActivity(
                                Intent(activity, MainActivity::class.java).putExtra(
                                    Constant.FROM, "tracker"
                                )
                            )
                            activity.finish()
                        }
                        activity.invalidateOptionsMenu()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.CART_URL, params, false)
    }

    override fun onResume() {
        super.onResume()
        MainActivity.toolbar.visibility = View.GONE
        lottieAnimationView.setAnimation("placed-order.json")
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
}