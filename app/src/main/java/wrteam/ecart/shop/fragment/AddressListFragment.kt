package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.AddressAdapter
import wrteam.ecart.shop.helper.ApiConfig
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Address

class AddressListFragment : Fragment() {
    lateinit var activity: Activity
    var total = 0
    private var totalAmount = 0.0
    private lateinit var fabAddAddress: FloatingActionButton
    lateinit var root: View
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var tvTotalItems: TextView
    private lateinit var tvSubTotal: TextView
    private lateinit var tvConfirmOrder: TextView
    private lateinit var processLyt: LinearLayout
    private lateinit var confirmLyt: RelativeLayout
    private lateinit var session: Session
    private lateinit var bundle: Bundle
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_address_list, container, false)
        activity = requireActivity()
        session = Session(activity)
        bundle = requireArguments()
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder)
        tvAlert = root.findViewById(R.id.tvAlert)
        fabAddAddress = root.findViewById(R.id.fabAddAddress)
        processLyt = root.findViewById(R.id.processLyt)
        tvSubTotal = root.findViewById(R.id.tvSubTotal)
        tvTotalItems = root.findViewById(R.id.tvTotalItems)
        confirmLyt = root.findViewById(R.id.confirmLyt)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        totalAmount = bundle.getDouble(Constant.TOTAL)
        if (ApiConfig.isConnected(activity)) {
            getData()
        }

        if (bundle.getString(Constant.FROM)
                .equals("process", ignoreCase = true) || requireArguments().getString(
                Constant.FROM
            ).equals("login", ignoreCase = true)
        ) {
            processLyt.visibility = View.VISIBLE
            confirmLyt.visibility = View.VISIBLE
            tvSubTotal.text = session.getData(Constant.currency) + stringFormat(
                "" + bundle.getDouble(
                    "total"
                )
            )

            tvTotalItems.text = Constant.TOTAL_CART_ITEM.toString() +"Items"

            tvConfirmOrder.setOnClickListener {
                if (selectedAddress.isNotEmpty()) {
                    if (session.getData(Constant.area_wise_delivery_charge) == "1") {
                        if (minimum_amount_for_place_order <= totalAmount) {
                            proceedOrder()
                        } else {
                            Toast.makeText(activity, getString(R.string.msg_minimum_order_amount) + session.getData(Constant.currency) + stringFormat("" + minimum_amount_for_place_order), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        proceedOrder()
                    }
                } else {
                    Toast.makeText(activity, R.string.select_delivery_address, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            processLyt.visibility = View.GONE
            confirmLyt.visibility = View.GONE
        }
        setHasOptionsMenu(true)
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            getData()
        }
        fabAddAddress.setOnClickListener { addNewAddress() }

        return root
    }

    private fun proceedOrder() {
        val fragment: Fragment = CheckoutFragment()
        val bundle = Bundle()
        bundle.putString("address", selectedAddress)
        bundle.putString("area_id", area_id)
        bundle.putString("from", bundle.getString("from"))
        bundle.putSerializable("data", bundle.getSerializable("data"))
        bundle.putStringArrayList("variantIdList", bundle.getStringArrayList("variantIdList"))
        bundle.putStringArrayList("qtyList", bundle.getStringArrayList("qtyList"))
        bundle.putDouble("minimum_amount_for_free_delivery", minimum_amount_for_free_delivery)
        bundle.putDouble("delivery_charge", delivery_charge)
        fragment.arguments = bundle
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
            .commit()
    }

    private fun addNewAddress() {
        val fragment: Fragment = AddressAddUpdateFragment()
        val bundle = Bundle()
        bundle.putSerializable("model", "")
        bundle.putString("for", "add")
        bundle.putInt("position", 0)
        fragment.arguments = bundle
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
            .commit()
    }

    private fun getData() {
        addresses = ArrayList()
        recyclerView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        recyclerView.layoutManager = LinearLayoutManager(activity)
        addressAdapter = AddressAdapter(activity, addresses)
        recyclerView.adapter = addressAdapter
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
                        Constant.selectedAddressId = ""
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            total = jsonObject.getString(Constant.TOTAL).toInt()

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
                                        Constant.selectedAddressId = address.id
                                    }
                                    addresses.add(address)
                                } else {
                                    break
                                }
                            }
                            addressAdapter = AddressAdapter(activity, addresses)
                            recyclerView.adapter = addressAdapter
                        } else {
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } catch (e: JSONException) {
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, Constant.GET_ADDRESS_URL, params, false)
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.addresses)
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
        lateinit var recyclerView: RecyclerView
        lateinit var addresses: ArrayList<Address?>

        @SuppressLint("StaticFieldLeak")
        lateinit var addressAdapter: AddressAdapter

        @SuppressLint("StaticFieldLeak")
        lateinit var tvAlert: TextView
        var selectedAddress = ""
        var area_id = ""
        var minimum_amount_for_free_delivery = 0.0
        var delivery_charge = 0.0
        var minimum_amount_for_place_order = 0.0
    }
}