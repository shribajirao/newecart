package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.LoginActivity
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.CartAdapter
import wrteam.ecart.shop.adapter.SaveForLaterAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Cart

class CartFragment : Fragment() {
    lateinit var carts: ArrayList<Cart>
    lateinit var saveForLater: ArrayList<Cart>
    lateinit var activity: Activity
    lateinit var root: View
    lateinit var cartRecycleView: RecyclerView
    lateinit var saveForLaterRecyclerView: RecyclerView
    lateinit var scrollView: NestedScrollView
    var total = 0.0
    private lateinit var btnShowNow: Button
    lateinit var databaseHelper: DatabaseHelper
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var rgOrderType: RadioGroup
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_cart, container, false)
        values = HashMap()
        saveForLaterValues = HashMap()
        activity = requireActivity()
        session = Session(activity)
        hashMap = HashMap()
        Constant.countList = ArrayList()
        lytTotal = root.findViewById(R.id.lytTotal)
        lytEmpty = root.findViewById(R.id.lytEmpty)
        btnShowNow = root.findViewById(R.id.btnShowNow)
        tvTotalAmount = root.findViewById(R.id.tvTotalAmount)
        tvTotalItems = root.findViewById(R.id.tvTotalItems)
        scrollView = root.findViewById(R.id.scrollView)
        cartRecycleView = root.findViewById(R.id.cartRecycleView)
        saveForLaterRecyclerView = root.findViewById(R.id.saveForLaterRecyclerView)
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        lytSaveForLater = root.findViewById(R.id.lytSaveForLater)
        tvSaveForLaterTitle = root.findViewById(R.id.tvSaveForLaterTitle)
        rgOrderType = root.findViewById(R.id.rgOrderType)
        databaseHelper = DatabaseHelper(activity)
        cartRecycleView.layoutManager = LinearLayoutManager(activity)
        saveForLaterRecyclerView.layoutManager = LinearLayoutManager(activity)
        if (session.getData(Constant.local_pickup) == "1") {
            rgOrderType.visibility = View.VISIBLE
        }
        setHasOptionsMenu(true)
        Constant.FLOAT_TOTAL_AMOUNT = 0.00
        carts = ArrayList()
        saveForLater = ArrayList()
        if (isConnected(activity)) {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                getSettings(activity)
            } else {
                getOfflineCart()
            }
        }
        tvConfirmOrder.setOnClickListener {
            if (isConnected(requireActivity())) {
                if (!isSoldOut) {
                    if (session.getData(Constant.area_wise_delivery_charge) == "0") {
                        if (session.getData(Constant.min_order_amount)
                                .toFloat() <= Constant.FLOAT_TOTAL_AMOUNT
                        ) {
                            checkout()
                        } else {
                            Toast.makeText(
                                activity,
                                getString(R.string.msg_minimum_order_amount) + session.getData(
                                    Constant.currency
                                ) + stringFormat(
                                    session.getData(Constant.min_order_amount)
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        checkout()
                    }
                } else {
                    Toast.makeText(activity, getString(R.string.msg_sold_out), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        btnShowNow.setOnClickListener { MainActivity.fm.popBackStack() }
        return root
    }

    fun checkout() {
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (rgOrderType.checkedRadioButtonId == R.id.rbDoorStepDelivery) {
                if (values.size > 0) {
                    addMultipleProductInCart(session, activity, values)
                }
                AddressListFragment.selectedAddress = ""
                val fragment: Fragment = AddressListFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "process")
                bundle.putDouble("total", Constant.FLOAT_TOTAL_AMOUNT)
                bundle.putSerializable("data", carts)
                bundle.putStringArrayList("variantIdList", variantIdList)
                bundle.putStringArrayList("qtyList", qtyList)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            } else {
                if (values.size > 0) {
                    addMultipleProductInCart(session, activity, values)
                }
                AddressListFragment.selectedAddress = ""
                val fragment: Fragment = CheckoutFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "cart")
                bundle.putDouble("total", Constant.FLOAT_TOTAL_AMOUNT)
                bundle.putSerializable("data", carts)
                bundle.putStringArrayList("variantIdList", variantIdList)
                bundle.putStringArrayList("qtyList", qtyList)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
        } else {
            startActivity(
                Intent(activity, LoginActivity::class.java).putExtra(
                    "total",
                    Constant.FLOAT_TOTAL_AMOUNT
                ).putExtra(
                    Constant.FROM, "checkout"
                )
            )
        }
    }

    private fun getOfflineCart() {
        variantIdList = ArrayList()
        qtyList = ArrayList()
        carts = ArrayList()
        hashMap = HashMap()
        cartAdapter = CartAdapter(activity, carts, saveForLater, hashMap)
        cartRecycleView.adapter = cartAdapter
        saveForLater = ArrayList()
        saveForLaterAdapter = SaveForLaterAdapter(activity, carts, saveForLater)
        saveForLaterRecyclerView.adapter = cartAdapter
        startShimmer()
        if (databaseHelper.getTotalItemOfCart(activity) > 0) {
            carts = ArrayList()
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_CART_OFFLINE] = Constant.GetVal
            params[Constant.VARIANT_IDs] =
                databaseHelper.cartList.toString().replace("[", "").replace("]", "")
                    .replace("\"", "")
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                session.setData(
                                    Constant.TOTAL,
                                    jsonObject.getString(Constant.TOTAL)
                                )
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)

                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val cart =
                                        Gson().fromJson(jsonObject1.toString(), Cart::class.java)
                                    carts.add(cart)
                                }
                                for (cart1 in carts) {
                                    val unitMeasurement = if (cart1.unit.equals(
                                            "kg",
                                            ignoreCase = true
                                        ) || cart1.unit.equals("ltr", ignoreCase = true)
                                    ) 1000 else 1.toLong()
                                    val unit =
                                        cart1.measurement.toDouble().toLong() * unitMeasurement
                                    if (!hashMap.containsKey(cart1.product_id)) {
                                        hashMap[cart1.product_id] =
                                            (cart1.stock.toDouble() * (if (cart1.stock_unit_name.equals(
                                                    "kg",
                                                    ignoreCase = true
                                                ) || cart1.stock_unit_name.equals(
                                                    "ltr",
                                                    ignoreCase = true
                                                )
                                            ) 1000 else 1) - unit * databaseHelper.checkCartItemExist(
                                                cart1.id, cart1.product_id
                                            ).toDouble()).toLong()
                                    } else {
                                        hashMap.replace(
                                            cart1.product_id,
                                            (hashMap[cart1.product_id]?.minus(
                                                (unit * databaseHelper.checkCartItemExist(
                                                    cart1.id, cart1.product_id
                                                ).toLong())
                                            )!!)
                                        )
                                    }
                                }
                                cartAdapter =
                                    CartAdapter(activity, carts, saveForLater, hashMap)
                                cartRecycleView.adapter = cartAdapter
                                if (databaseHelper.saveForLaterList.size > 0) {
                                    getOfflineSaveForLater()
                                } else {
                                    saveForLater = ArrayList()
                                    saveForLaterAdapter = SaveForLaterAdapter(
                                        activity, carts, saveForLater
                                    )
                                    saveForLaterRecyclerView.adapter = saveForLaterAdapter
                                }
                                if (carts.size > 0) {
                                    lytTotal.visibility = View.VISIBLE
                                }
                                stopShimmer()
                                lytEmpty.visibility = View.GONE
                            } else {
                                getOfflineSaveForLater()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            getOfflineSaveForLater()
                        }
                    }
                }
            }, activity, Constant.GET_OFFLINE_CART_URL, params, false)
        } else {
            getOfflineSaveForLater()
        }
    }

    private fun getOfflineSaveForLater() {
        saveForLater = ArrayList()
        if (databaseHelper.totalItemOfSaveForLater >= 1) {
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_CART_OFFLINE] = Constant.GetVal
            params[Constant.VARIANT_IDs] =
                databaseHelper.saveForLaterList.toString().replace("[", "").replace("]", "")
                    .replace("\"", "")
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                session.setData(
                                    Constant.TOTAL,
                                    jsonObject.getString(Constant.TOTAL)
                                )
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val cart =
                                        Gson().fromJson(jsonObject1.toString(), Cart::class.java)
                                    saveForLater.add(cart)
                                }
                                saveForLaterAdapter =
                                    SaveForLaterAdapter(activity, carts, saveForLater)
                                saveForLaterRecyclerView.adapter = saveForLaterAdapter
                                stopShimmer()
                                if (carts.size > 0) {
                                    lytTotal.visibility = View.VISIBLE
                                }
                                lytSaveForLater.visibility = View.VISIBLE
                                lytEmpty.visibility = View.GONE
                            } else {
                                stopShimmer()
                                lytTotal.visibility = View.GONE
                                lytSaveForLater.visibility = View.GONE
                                lytEmpty.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            stopShimmer()
                            lytTotal.visibility = View.GONE
                            lytSaveForLater.visibility = View.GONE
                            lytEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }, activity, Constant.GET_OFFLINE_CART_URL, params, false)
        } else {
            stopShimmer()
            lytTotal.visibility = View.GONE
            lytSaveForLater.visibility = View.GONE
            lytEmpty.visibility = View.VISIBLE
        }
    }

    fun getSettings(activity: Activity) {
        startShimmer()
        val session = Session(activity)
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SETTINGS] = Constant.GetVal
        params[Constant.GET_TIMEZONE] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val jsonObject = jsonObject.getJSONObject(Constant.SETTINGS)
                            session.setData(
                                Constant.minimum_version_required,
                                jsonObject.getString(Constant.minimum_version_required)
                            )
                            session.setData(
                                Constant.is_version_system_on,
                                jsonObject.getString(Constant.is_version_system_on)
                            )
                            session.setData(
                                Constant.currency,
                                jsonObject.getString(Constant.currency)
                            )
                            session.setData(
                                Constant.min_order_amount,
                                jsonObject.getString(Constant.min_order_amount)
                            )
                            session.setData(
                                Constant.max_cart_items_count,
                                jsonObject.getString(Constant.max_cart_items_count)
                            )
                            session.setData(
                                Constant.area_wise_delivery_charge,
                                jsonObject.getString(Constant.area_wise_delivery_charge)
                            )
                            getData()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false)
    }

    //                    System.out.println("====res "+response);
    @SuppressLint("SetTextI18n")
    private fun getData(){
            variantIdList = ArrayList()
            qtyList = ArrayList()
            hashMap = HashMap()
            saveForLater = ArrayList()
            carts = ArrayList()
            saveForLaterAdapter = SaveForLaterAdapter(activity, carts, saveForLater)
            saveForLaterRecyclerView.adapter = saveForLaterAdapter
            cartAdapter = CartAdapter(activity, carts, saveForLater, hashMap)
            cartRecycleView.adapter = cartAdapter
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_USER_CART] = Constant.GetVal
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
//                    System.out.println("====res "+response);
                            jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                val jsonObject = JSONObject(response)
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                                if (jsonArray.length() > 0) {
                                    for (i in 0 until jsonArray.length()) {
                                        val jsonObject1 = jsonArray.getJSONObject(i)
                                        if (jsonObject1 != null) {
                                            try {
                                                val cart = Gson().fromJson(
                                                    jsonObject1.toString(),
                                                    Cart::class.java
                                                )
                                                Constant.countList.add(
                                                    Cart(
                                                        cart.product_id,
                                                        cart.product_variant_id,
                                                        cart.qty
                                                    )
                                                )
                                                variantIdList.add(cart.product_variant_id)
                                                qtyList.add(cart.qty)
                                                carts.add(cart)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } else {
                                            break
                                        }
                                    }
                                    hashMap.clear()
                                    for (cart1 in carts) {
                                        val unitMeasurement = if (cart1.type.equals(
                                                "kg",
                                                ignoreCase = true
                                            ) || cart1.type.equals("ltr", ignoreCase = true)
                                        ) 1000 else 1.toLong()
                                        val unit =
                                            cart1.measurement.toDouble().toLong() * unitMeasurement
                                        if (!hashMap.containsKey(cart1.product_id)) {
                                            hashMap[cart1.product_id] =
                                                (cart1.stock.toDouble() * (if (cart1.stock_unit_name.equals(
                                                        "kg",
                                                        ignoreCase = true
                                                    ) || cart1.stock_unit_name.equals(
                                                        "ltr",
                                                        ignoreCase = true
                                                    )
                                                ) 1000 else 1) - unit * cart1.qty.toDouble()).toLong()
                                        } else {
                                            val availableStock =
                                                (hashMap[cart1.product_id]?.minus(unit * cart1.qty.toDouble()))?.toLong()!!
                                            hashMap.replace(cart1.product_id, availableStock)
                                        }
                                    }
                                    cartAdapter =
                                        CartAdapter(activity, carts, saveForLater, hashMap)
                                    cartRecycleView.adapter = cartAdapter
                                }
                                val jsonArraySaveForLater =
                                    jsonObject.getJSONArray(Constant.SAVE_FOR_LATER)
                                val count =
                                    if (jsonArraySaveForLater.length() > 1) jsonArraySaveForLater.length()
                                        .toString() + activity.getString(R.string.cart) else jsonArraySaveForLater.length()
                                        .toString() + activity.getString(R.string.item)
                                tvSaveForLaterTitle.text =
                                    activity.getString(R.string.save_for_later) + " (" + count + ")"
                                if (jsonArraySaveForLater.length() > 0) {
                                    for (i in 0 until jsonArraySaveForLater.length()) {
                                        val jsonObject1 = jsonArraySaveForLater.getJSONObject(i)
                                        try {
                                            val cart = Gson().fromJson(
                                                jsonObject1.toString(),
                                                Cart::class.java
                                            )
                                            saveForLater.add(cart)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    saveForLaterAdapter = SaveForLaterAdapter(
                                        activity, carts, saveForLater
                                    )
                                    saveForLaterRecyclerView.adapter = saveForLaterAdapter
                                    lytSaveForLater.visibility = View.VISIBLE
                                }
                                if (carts.size > 0) {
                                    lytTotal.visibility = View.VISIBLE
                                }
                                stopShimmer()
                                lytEmpty.visibility = View.GONE
                                total = jsonObject.getString(Constant.TOTAL).toDouble()
                                
                                Constant.TOTAL_CART_ITEM =
                                    jsonObject.getString(Constant.TOTAL).toInt()
                                setData(activity)
                            } else {
                                stopShimmer()
                                lytSaveForLater.visibility = View.GONE
                                lytEmpty.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            stopShimmer()
                            lytSaveForLater.visibility = View.GONE
                            lytEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }, activity, Constant.CART_URL, params, false)
        }

    override fun onPause() {
        super.onPause()
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (values.size > 0) {
                addMultipleProductInCart(session, activity, values)
            }
        }
    }

    fun startShimmer() {
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
    }

    fun stopShimmer() {
        mShimmerViewContainer.stopShimmer()
        mShimmerViewContainer.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.cart)
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
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var lytEmpty: LinearLayout

        @SuppressLint("StaticFieldLeak")
        lateinit var lytTotal: RelativeLayout

        @SuppressLint("StaticFieldLeak")
        lateinit var cartAdapter: CartAdapter

        @SuppressLint("StaticFieldLeak")
        lateinit var saveForLaterAdapter: SaveForLaterAdapter
        lateinit var values: HashMap<String, String>
        lateinit var saveForLaterValues: HashMap<String, String>
        var isSoldOut = false

        @SuppressLint("StaticFieldLeak")
        lateinit var tvTotalAmount: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var tvTotalItems: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var tvConfirmOrder: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var tvSaveForLaterTitle: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var session: Session
        lateinit var jsonObject: JSONObject

        @SuppressLint("StaticFieldLeak")
        lateinit var lytSaveForLater: LinearLayout
        lateinit var variantIdList: ArrayList<String>
        lateinit var qtyList: ArrayList<String>
        lateinit var hashMap: HashMap<String, Long>
        @SuppressLint("SetTextI18n")
        fun setData(activity: Activity) {
            tvTotalAmount.text = session.getData(Constant.CURRENCY) + stringFormat(
                java.lang.String.valueOf(
                    Constant.FLOAT_TOTAL_AMOUNT
                )
            )
            val count: Int = cartAdapter.itemCount
            tvTotalItems.text =
                count.toString() + if (count == 1) activity.getString(R.string.item) else activity.getString(
                    R.string.cart
                )
        }
    }
}