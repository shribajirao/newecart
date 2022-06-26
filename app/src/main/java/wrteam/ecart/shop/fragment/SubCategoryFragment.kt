package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import wrteam.ecart.shop.adapter.SubCategoryAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.buildCounterDrawable
import wrteam.ecart.shop.helper.ApiConfig.Companion.getSettings
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Cart
import wrteam.ecart.shop.model.Category
import wrteam.ecart.shop.model.ProductList

class SubCategoryFragment : Fragment() {
    lateinit var productLoadMoreAdapter: ProductLoadMoreAdapter
    lateinit var root: View
    lateinit var session: Session
    lateinit var nestedScrollView: NestedScrollView
    lateinit var activity: Activity
    lateinit var id: String
    lateinit var filterBy: String
    lateinit var from: String
    lateinit var recyclerView: RecyclerView
    lateinit var subCategoryRecycleView: RecyclerView
    lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    lateinit var tvAlert: TextView
    lateinit var hashMap: HashMap<String, Long>
    lateinit var databaseHelper: DatabaseHelper
    lateinit var menu: Menu
    var filterIndex = 0
    var total = 0
    var offset = 0
    var isSort = false
    var isLoadMore = false
    private var isGrid = false
    var resource = 0
    var isLogin = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_sub_category, container, false)
        setHasOptionsMenu(true)
        activity = requireActivity()
        databaseHelper = DatabaseHelper(activity)
        session = Session(activity)
        offset = 0
        hashMap = HashMap()
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
        from = arguments?.getString(Constant.FROM).toString()
        id = arguments?.getString("id").toString()
        productArrayList = ArrayList()
        categoryArrayList = ArrayList()

        getAllWidgets(root)

        if (session.getBoolean("grid")) {
            resource = R.layout.lyt_item_grid
            isGrid = true
            recyclerView.layoutManager = GridLayoutManager(activity, 2)
        } else {
            resource = R.layout.lyt_item_list
            isGrid = false
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }
        filterIndex = -1

        if (isConnected(activity)) {
            getSettings(activity)
            getData()
        }

        swipeLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            getSettings(activity)
            getData()
        }

        return root
    }

    private fun getAllWidgets(root: View) {
        tvAlert = root.findViewById(R.id.tvAlert)
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        nestedScrollView = root.findViewById(R.id.nestedScrollView)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        subCategoryRecycleView = root.findViewById(R.id.subCategoryRecycleView)
        tvAlert = root.findViewById(R.id.tvAlert)
        subCategoryRecycleView.layoutManager = GridLayoutManager(activity, Constant.GRID_COLUMN)
    }

    fun stopShimmer() {
        nestedScrollView.visibility = View.VISIBLE
        mShimmerViewContainer.visibility = View.GONE
        mShimmerViewContainer.stopShimmer()
    }

    fun startShimmer() {
        nestedScrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
    }

    fun getData() {
        startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.CATEGORY_ID] = id
        categoryArrayList = ArrayList()
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            val jsonArray = jsonObject1.getJSONArray(Constant.DATA)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val category = Category()
                                category.id = jsonObject.getString(Constant.ID)
                                category.category_id = jsonObject.getString(Constant.CATEGORY_ID)
                                category.name = jsonObject.getString(Constant.NAME)
                                category.slug = jsonObject.getString(Constant.SLUG)
                                category.subtitle = jsonObject.getString(Constant.SUBTITLE)
                                category.image = jsonObject.getString(Constant.IMAGE)
                                categoryArrayList.add(category)
                            }
                            subCategoryRecycleView.adapter = SubCategoryAdapter(
                                activity,
                                categoryArrayList,
                                R.layout.lyt_subcategory,
                                "sub_cate"
                            )
                        }
                        getProducts()
                    } catch (e: JSONException) {
                        getProducts()
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.SubcategoryUrl, params, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getProducts() {
        startShimmer()
        productArrayList = ArrayList()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.CATEGORY_ID] = id
        params[Constant.GET_ALL_PRODUCTS] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
        params[Constant.OFFSET] = "" + offset
        if (filterIndex != -1) {
            params[Constant.SORT] = filterBy
        }
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            total = jsonObject1.getString(Constant.TOTAL).toInt()
                            val jsonObject = JSONObject(response)
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
                                stopShimmer()
                            }
                            if (offset == 0) {
                                isSort = true
                                productLoadMoreAdapter = ProductLoadMoreAdapter(
                                    activity,
                                    productArrayList,
                                    resource,
                                    from,
                                    hashMap
                                )
                                recyclerView.adapter = productLoadMoreAdapter
                                nestedScrollView.visibility = View.VISIBLE
                                mShimmerViewContainer.visibility = View.GONE
                                mShimmerViewContainer.stopShimmer()
                                nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        val linearLayoutManager =
                                            recyclerView.layoutManager as LinearLayoutManager
                                        if (productArrayList.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == productArrayList.size - 1) {
                                                    //bottom of list!


                                                    offset += ("" + Constant.LOAD_ITEM_LIMIT).toInt()
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.CATEGORY_ID] = id
                                                    params[Constant.GET_ALL_PRODUCTS] =
                                                        Constant.GetVal
                                                    if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                        session.getData(
                                                            Constant.ID
                                                        )
                                                    params1[Constant.LIMIT] =
                                                        "" + Constant.LOAD_ITEM_LIMIT
                                                    params1[Constant.OFFSET] =
                                                        offset.toString() + ""
                                                    if (filterIndex != -1) {
                                                        params1[Constant.SORT] = filterBy
                                                    }
                                                    requestToVolley(
                                                        object : VolleyCallback {
                                                            override fun onSuccess(
                                                                result: Boolean,
                                                                response: String
                                                            ) {
                                                                if (result) {
                                                                    try {


                                                                        val jsonObject11 =
                                                                            JSONObject(response)
                                                                        if (!jsonObject11.getBoolean(
                                                                                Constant.ERROR
                                                                            )
                                                                        ) {
                                                                            val object1 =
                                                                                JSONObject(response)
                                                                            val jsonArray1 =
                                                                                object1.getJSONArray(
                                                                                    Constant.DATA
                                                                                )
                                                                            try {
                                                                                for (i in 0 until jsonArray1.length()) {
                                                                                    val product =
                                                                                        Gson().fromJson(
                                                                                            jsonArray1.getJSONObject(
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
                                                                                        ) hashMap[variant.product_id] =
                                                                                            (variant.stock.toDouble() * (if (variant.stock_unit_name.equals(
                                                                                                    "kg",
                                                                                                    ignoreCase = true
                                                                                                ) || variant.stock_unit_name.equals(
                                                                                                    "ltr",
                                                                                                    ignoreCase = true
                                                                                                )
                                                                                            ) 1000 else 1) - unit * variant.cart_count.toLong()).toLong() else hashMap.replace(
                                                                                            variant.product_id,
                                                                                            (hashMap[variant.product_id]?.minus(
                                                                                                unit * variant.cart_count.toLong()
                                                                                            )!!)
                                                                                        )
                                                                                    }
                                                                                    productArrayList.add(
                                                                                        product
                                                                                    )
                                                                                }
                                                                            } catch (e: Exception) {
                                                                                nestedScrollView.visibility =
                                                                                    View.VISIBLE
                                                                                mShimmerViewContainer.visibility =
                                                                                    View.GONE
                                                                                mShimmerViewContainer.stopShimmer()
                                                                            }
                                                                            productLoadMoreAdapter.notifyDataSetChanged()
                                                                            isLoadMore = false
                                                                        }
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        activity,
                                                        Constant.GET_PRODUCT_BY_CATE,
                                                        params1,
                                                        false
                                                    )
                                                    isLoadMore = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (productArrayList.size == 0 && categoryArrayList.size == 0) {
                                tvAlert.visibility = View.VISIBLE
                            } else {
                                tvAlert.visibility = View.GONE
                            }
                            menu.findItem(R.id.toolbar_layout).isVisible =
                                productArrayList.size == 0
                            activity.invalidateOptionsMenu()
                            stopShimmer()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        if (productArrayList.size == 0 && categoryArrayList.size == 0) {
                            tvAlert.visibility = View.VISIBLE
                        } else {
                            tvAlert.visibility = View.GONE
                        }
                        menu.findItem(R.id.toolbar_layout).isVisible = productArrayList.size == 0
                        activity.invalidateOptionsMenu()
                        stopShimmer()
                    }
                }
            }
        }, activity, Constant.GET_PRODUCT_BY_CATE, params, false)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isSort) {
            if (item.itemId == R.id.toolbar_sort) {
                val builder = AlertDialog.Builder(
                    activity
                )
                builder.setTitle(activity.resources.getString(R.string.filter_by))
                builder.setSingleChoiceItems(
                    Constant.filterValues,
                    filterIndex
                ) { dialog: DialogInterface, item1: Int ->
                    filterIndex = item1
                    when (item1) {
                        0 -> filterBy = Constant.NEW
                        1 -> filterBy = Constant.OLD
                        2 -> filterBy = Constant.HIGH
                        3 -> filterBy = Constant.LOW
                    }
                    if (item1 != -1) {
                        getData()
                        getProducts()
                        dialog.dismiss()
                    }
                }
                val alertDialog = builder.create()
                alertDialog.show()
            } else if (item.itemId == R.id.toolbar_layout) {
                if (isGrid) {
                    isGrid = false
                    recyclerView.adapter = null
                    resource = R.layout.lyt_item_list
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                } else {
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
                    from,
                    hashMap
                )
                recyclerView.adapter = productLoadMoreAdapter
                productLoadMoreAdapter.notifyDataSetChanged()
                activity.invalidateOptionsMenu()
            }
        }
        return false
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        menu.findItem(R.id.toolbar_sort).isVisible = isSort
        menu.findItem(R.id.toolbar_search).isVisible = true
        menu.findItem(R.id.toolbar_cart).icon = buildCounterDrawable(
            Constant.TOTAL_CART_ITEM,
            activity
        )
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

    override fun onResume() {
        super.onResume()
        assert(arguments != null)
        Constant.TOOLBAR_TITLE = arguments?.getString(Constant.NAME).toString()
        activity.invalidateOptionsMenu()
        hideKeyboard()
        val productArrayList1 = productArrayList
        if (Constant.countList.size != 0) {
            if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                productLoadMoreAdapter.notifyDataSetChanged()
            } else if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                productArrayList1.withIndex().forEach { (indexProduct, product_) ->
                    product_?.variants!!.withIndex().forEach { (indexVariant, variants_) ->
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

    override fun onPause() {
        super.onPause()
        addMultipleProductInCart(session, activity, Constant.CartValues)
    }

    companion object {
        lateinit var productArrayList: ArrayList<ProductList?>
        lateinit var categoryArrayList: ArrayList<Category>
    }
}