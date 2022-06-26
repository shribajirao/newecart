package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.ProductLoadMoreAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.buildCounterDrawable
import wrteam.ecart.shop.helper.ApiConfig.Companion.getSettings
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Cart
import wrteam.ecart.shop.model.ProductList

@SuppressLint("NotifyDataSetChanged")
class FavoriteFragment : Fragment() {
    lateinit var root: View
    lateinit var session: Session
    var total = 0
    lateinit var nestedScrollView: NestedScrollView
    lateinit var activity: Activity
    var isLogin = false
    lateinit var databaseHelper: DatabaseHelper
    var offset = 0
    lateinit var swipeLayout: SwipeRefreshLayout
    var isLoadMore = false
    private var isGrid = false
    private lateinit var lytList: LinearLayout
    private lateinit var lytGrid: LinearLayout
    var resource = 0
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    lateinit var url: String
    lateinit var hashMap: HashMap<String, Long>
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_favorite, container, false)
        setHasOptionsMenu(true)
        activity = requireActivity()
        hashMap = HashMap()
        session = Session(activity)
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
        databaseHelper = DatabaseHelper(activity)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvAlert = root.findViewById(R.id.tvAlert)
        lytGrid = root.findViewById(R.id.lytGrid)
        lytList = root.findViewById(R.id.lytList)
        nestedScrollView = root.findViewById(R.id.nestedScrollView)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)

        productArrayList=ArrayList()


        url = if (isLogin) {
            Constant.GET_FAVORITES_URL
        } else {
            Constant.GET_OFFLINE_FAVORITES_URL
        }
        if (session.getBoolean("grid")) {
            resource = R.layout.lyt_item_grid
            isGrid = true
            recyclerView = root.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = GridLayoutManager(activity, 2)
        } else {
            resource = R.layout.lyt_item_list
            isGrid = false
            recyclerView = root.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }

        productLoadMoreAdapter = ProductLoadMoreAdapter(
            activity, productArrayList, resource, "favorite", hashMap
        )

        getSettings(activity)
        if (isConnected(activity)) {
            getData()
        }
        swipeLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeLayout.setOnRefreshListener {
            if (isConnected(activity)) {
                offset = 0
                total = 0
                getWalletBalance(activity, Session(activity))
                if (isLogin) {
                    if (Constant.CartValues.size > 0) {
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                    }
                }
                getData()
            }
            swipeLayout.isRefreshing = false
        }
        return root
    }

    fun getData() {
        recyclerView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        if (isLogin) {
            params[Constant.GET_FAVORITES] = Constant.GetVal
            params[Constant.GET_ALL_PRODUCTS] = Constant.GetVal
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
            params[Constant.OFFSET] = offset.toString() + ""
        } else {
            params[Constant.GET_FAVORITES_OFFLINE] = Constant.GetVal
            params[Constant.PRODUCT_IDs] =
                databaseHelper.favorite.toString().replace("[", "").replace("]", "")
        }
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (isLogin) {
                                total = jsonObject.getString(Constant.TOTAL).toInt()
                            }
                            if (offset == 0) {
                                productArrayList = ArrayList()
                                recyclerView.visibility = View.VISIBLE
                                tvAlert.visibility = View.GONE
                            }
                            val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                            try {
                                for (i in 0 until jsonArray.length()) {
                                    val product = Gson().fromJson(
                                        jsonArray.getJSONObject(i).toString(),
                                        ProductList::class.java
                                    )
                                    val variants = product.variants
                                    val variant = variants[0]
                                    if (!isLogin) {
                                        variant.cart_count = databaseHelper.checkCartItemExist(
                                            variant.id,
                                            variant.product_id
                                        )
                                    }
                                    for (variants_ in product.variants) {
                                        val unitMeasurement =
                                            if (variants_.measurement_unit_name.equals(
                                                    "kg",
                                                    ignoreCase = true
                                                ) || variants_.measurement_unit_name.equals(
                                                    "ltr",
                                                    ignoreCase = true
                                                )
                                            ) 1000 else 1.toLong()
                                        val unit = variants_.measurement.toDouble()
                                            .toLong() * unitMeasurement
                                        if (!hashMap.containsKey(variant.product_id)) {
                                            hashMap[variant.product_id] =
                                                (variant.stock.toDouble() * (if (variant.stock_unit_name.equals(
                                                        "kg",
                                                        ignoreCase = true
                                                    ) || variant.stock_unit_name.equals(
                                                        "ltr",
                                                        ignoreCase = true
                                                    )
                                                ) 1000 else 1) - unit * variant.cart_count.toLong()).toLong()
                                        } else {
                                            hashMap.replace(
                                                variant.product_id,
                                                (hashMap[variant.product_id]?.minus(unit * variant.cart_count.toLong())!!)
                                            )
                                        }
                                    }
                                    productArrayList.add(product)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (offset == 0) {
                                productLoadMoreAdapter = ProductLoadMoreAdapter(
                                    activity, productArrayList, resource, "favorite", hashMap
                                )
                                recyclerView.adapter = productLoadMoreAdapter
                                mShimmerViewContainer.stopShimmer()
                                mShimmerViewContainer.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        val linearLayoutManager =
                                            recyclerView.layoutManager as LinearLayoutManager
                                        if (productArrayList.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size - 1) {
                                                    //bottom of list!


                                                    offset += Constant.LOAD_ITEM_LIMIT
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    if (isLogin) {
                                                        params1[Constant.GET_FAVORITES] =
                                                            Constant.GetVal
                                                        if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                            session.getData(
                                                                Constant.ID
                                                            )
                                                        params1[Constant.LIMIT] =
                                                            "" + Constant.LOAD_ITEM_LIMIT
                                                        params1[Constant.OFFSET] =
                                                            offset.toString() + ""
                                                    } else {
                                                        params1[Constant.GET_FAVORITES_OFFLINE] =
                                                            Constant.GetVal
                                                        params1[Constant.PRODUCT_IDs] =
                                                            databaseHelper.favorite.toString()
                                                                .replace("[", "").replace("]", "")
                                                    }
                                                    requestToVolley(object : VolleyCallback {
                                                        override fun onSuccess(
                                                            result: Boolean,
                                                            response: String
                                                        ) {

                                                            if (result) {
                                                                try {
                                                                    val jsonObject1 =
                                                                        JSONObject(response)
                                                                    if (!jsonObject1.getBoolean(
                                                                            Constant.ERROR
                                                                        )
                                                                    ) {
                                                                        val jsonArray1 =
                                                                            jsonObject1.getJSONArray(
                                                                                Constant.DATA
                                                                            )
                                                                        try {
                                                                            for (i in 0 until jsonArray1.length()) {
                                                                                val product =
                                                                                    Gson().fromJson(
                                                                                        jsonArray.getJSONObject(
                                                                                            i
                                                                                        )
                                                                                            .toString(),
                                                                                        ProductList::class.java
                                                                                    )
                                                                                val variants =
                                                                                    product.variants
                                                                                val variant =
                                                                                    variants[0]
                                                                                if (!isLogin) {
                                                                                    variant.cart_count =
                                                                                        databaseHelper.checkCartItemExist(
                                                                                            variant.id,
                                                                                            variant.product_id
                                                                                        )
                                                                                }
                                                                                for (variants_ in product.variants) {
                                                                                    val unitMeasurement =
                                                                                        if (variants_.measurement_unit_name.equals(
                                                                                                "kg",
                                                                                                ignoreCase = true
                                                                                            ) || variants_.measurement_unit_name.equals(
                                                                                                "ltr",
                                                                                                ignoreCase = true
                                                                                            )
                                                                                        ) 1000 else 1.toLong()
                                                                                    val unit =
                                                                                        variants_.measurement.toDouble()
                                                                                            .toLong() * unitMeasurement
                                                                                    if (!hashMap.containsKey(
                                                                                            variant.product_id
                                                                                        )
                                                                                    ) {
                                                                                        hashMap[variant.product_id] =
                                                                                            (variant.stock.toDouble() * (if (variant.stock_unit_name.equals(
                                                                                                    "kg",
                                                                                                    ignoreCase = true
                                                                                                ) || variant.stock_unit_name.equals(
                                                                                                    "ltr",
                                                                                                    ignoreCase = true
                                                                                                )
                                                                                            ) 1000 else 1) - unit * variant.cart_count.toLong()).toLong()
                                                                                    } else {
                                                                                        hashMap.replace(
                                                                                            variant.product_id,
                                                                                            (hashMap[variant.product_id]?.minus(
                                                                                                unit * variant.cart_count.toLong()
                                                                                            )!!)
                                                                                        )
                                                                                    }
                                                                                }
                                                                                productArrayList.add(
                                                                                    product
                                                                                )
                                                                            }
                                                                        } catch (e: Exception) {
                                                                            e.printStackTrace()
                                                                        }
                                                                        productLoadMoreAdapter.notifyDataSetChanged()
                                                                        isLoadMore = false
                                                                    }
                                                                } catch (e: JSONException) {
                                                                    mShimmerViewContainer.stopShimmer()
                                                                    mShimmerViewContainer.visibility =
                                                                        View.GONE
                                                                    recyclerView.visibility =
                                                                        View.VISIBLE
                                                                }
                                                            } else {
                                                                isLoadMore = false
                                                                productLoadMoreAdapter.notifyDataSetChanged()
                                                                mShimmerViewContainer.stopShimmer()
                                                                mShimmerViewContainer.visibility =
                                                                    View.GONE
                                                                recyclerView.visibility =
                                                                    View.VISIBLE
                                                                recyclerView.visibility = View.GONE
                                                                tvAlert.visibility = View.VISIBLE
                                                            }
                                                        }
                                                    }, activity, url, params1, false)
                                                    isLoadMore = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            mShimmerViewContainer.stopShimmer()
                            mShimmerViewContainer.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        tvAlert.visibility = View.VISIBLE
                    }
                } else {
                    mShimmerViewContainer.stopShimmer()
                    mShimmerViewContainer.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    tvAlert.visibility = View.VISIBLE
                }
            }
        }, activity, url, params, false)
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.title_fav)
        requireActivity().invalidateOptionsMenu()
        hideKeyboard()
        val productArrayList1 = productArrayList
        if (Constant.countList.size != 0) {
            if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                productLoadMoreAdapter.notifyDataSetChanged()
            } else if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                for ((indexProduct, product_) in productArrayList1.withIndex()) {
                    for ((indexVariant, variants_) in product_?.variants?.withIndex()!!) {
                        val cart1 =
                            Cart(variants_.product_id, variants_.id, variants_.cart_count)
                        for (cart in Constant.countList) {
                            if (cart.product_id == cart1.product_id && cart.product_variant_id == cart1.product_variant_id) {
                                productArrayList[indexProduct]!!.variants[indexVariant].cart_count =
                                    cart.qty
                            }
                        }
                    }
                }
                productLoadMoreAdapter.notifyDataSetChanged()
            }
        }
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

    override fun onHiddenChanged(hidden: Boolean) {
        recyclerView.visibility = View.GONE
        tvAlert.visibility = View.GONE
        if (!hidden) getData()
        super.onHiddenChanged(hidden)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_layout) {
            if (isGrid) {
                lytGrid.visibility = View.GONE
                lytList.visibility = View.VISIBLE
                isGrid = false
                recyclerView.adapter = null
                resource = R.layout.lyt_item_list
                recyclerView.layoutManager = LinearLayoutManager(activity)
            } else {
                lytGrid.visibility = View.VISIBLE
                lytList.visibility = View.GONE
                isGrid = true
                recyclerView.adapter = null
                resource = R.layout.lyt_item_grid
                recyclerView.layoutManager = GridLayoutManager(context, 2)
            }
            session.setBoolean("grid", isGrid)
            productLoadMoreAdapter = ProductLoadMoreAdapter(
                activity,
                productArrayList,
                resource,
                "favorite",
                hashMap
            )
            recyclerView.adapter =
                productLoadMoreAdapter
            productLoadMoreAdapter.notifyDataSetChanged()
            activity.invalidateOptionsMenu()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        activity.menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = true
        menu.findItem(R.id.toolbar_cart).icon = buildCounterDrawable(
            Constant.TOTAL_CART_ITEM,
            activity
        )
        menu.findItem(R.id.toolbar_layout).isVisible = true
        val myDrawable: Drawable? = if (isGrid) {
            ContextCompat.getDrawable(
                activity,
                R.drawable.ic_list_
            ) // The ID of your drawable
        } else {
            ContextCompat.getDrawable(
                activity,
                R.drawable.ic_grid_
            ) // The ID of your drawable.
        }
        menu.findItem(R.id.toolbar_layout).icon = myDrawable
        super.onPrepareOptionsMenu(menu)
    }

    companion object {
        lateinit var productArrayList: ArrayList<ProductList?>

        @SuppressLint("StaticFieldLeak")
        lateinit var productLoadMoreAdapter: ProductLoadMoreAdapter
        lateinit var recyclerView: RecyclerView

        @SuppressLint("StaticFieldLeak")
        lateinit var tvAlert: RelativeLayout
    }
}