package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.ProductLoadMoreAdapter
import wrteam.ecart.shop.helper.*
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.buildCounterDrawable
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.model.Cart
import wrteam.ecart.shop.model.FlashSalesList
import wrteam.ecart.shop.model.ProductList
import java.util.*


@SuppressLint("NotifyDataSetChanged", "StaticFieldLeak", "UseCompatLoadingForDrawables")
class ProductListFragment : Fragment() {
    lateinit var root: View
    lateinit var session: Session
    lateinit var nestedScrollView: NestedScrollView
    lateinit var activity: Activity
    lateinit var id: String
    lateinit var filterBy: String
    lateinit var from: String
    lateinit var recyclerView: RecyclerView
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var tvAlert: TextView
    lateinit var tabLayout1: LinearLayout
    private lateinit var lytList: LinearLayout
    private lateinit var lytGrid: LinearLayout
    lateinit var flashSalesLists: ArrayList<FlashSalesList>
    lateinit var tabLayout: TabLayout
    lateinit var listView: ListView
    lateinit var searchView: EditText
    lateinit var noResult: TextView
    lateinit var msg: TextView
    private lateinit var lytSearchView: LinearLayout
    private lateinit var productsName: ArrayList<String>
    lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit var hashMap: HashMap<String, Long>
    lateinit var databaseHelper: DatabaseHelper
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var tab: TabLayout.Tab
    private lateinit var productLoadMoreAdapter: ProductLoadMoreAdapter


    var isLogin = false
    var url = ""
    var total = 0
    var offset = 0
    private var offsetFlashSaleNames = 0
    var filterIndex = 0
    var isSort = false
    var isLoadMore = false
    private var isGrid = false
    var resource = 0
    var totalFlashSales = 0
    var tabLoading = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_product_list, container, false)

        try {
            setHasOptionsMenu(true)
            offset = 0
            activity = requireActivity()
            databaseHelper = DatabaseHelper(activity)
            session = Session(activity)
            productArrayList = ArrayList()
            productsName = ArrayList()
            isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
            hashMap = HashMap()
            from = arguments?.getString(Constant.FROM).toString()
            id = arguments?.getString(Constant.ID).toString()
            setHasOptionsMenu(true)

            getWidgets()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return root
    }

    private fun getProductData() {
        when (from) {
            "regular", "sub_cate", "similar", "section", "flash_sale" -> getData()
            "search" -> {
                stopShimmer()
                lytSearchView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Constant.CartValues = HashMap()
                searchView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_search,
                    0,
                    R.drawable.ic_close_,
                    0
                )
                arrayAdapter = ArrayAdapter(
                    activity,
                    R.layout.spinner_search_item,
                    R.id.tvTitle,
                    removeDuplicates(productsName)
                )
                listView.adapter = arrayAdapter
            }
            "flash_sale_all" -> {
                tabLayout1.visibility = View.VISIBLE
                tab = tabLayout.newTab().setText(activity.getString(R.string.loading))
                    .setTag("0")
                tabLayout.addTab(tab)
                getFlashSales(offsetFlashSaleNames, tab)
                tabLayout.viewTreeObserver.addOnScrollChangedListener {
                    val windowSize = Point()
                    val scrollX = tabLayout.scrollX
                    val maxScrollWidth = tabLayout.getChildAt(0).measuredWidth - windowSize.x
                    if (maxScrollWidth == scrollX && !tabLoading) {
                        tab =
                            tabLayout.newTab().setText(activity.getString(R.string.loading))
                                .setTag("0")
                        tabLayout.addTab(tab)
                        offsetFlashSaleNames += Constant.LOAD_ITEM_LIMIT
                        getFlashSales(offsetFlashSaleNames, tab)
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getWidgets() {
        try {
            recyclerView = root.findViewById(R.id.recyclerView)
            swipeLayout = root.findViewById(R.id.swipeLayout)
            tvAlert = root.findViewById(R.id.tvAlert)
            nestedScrollView = root.findViewById(R.id.nestedScrollView)
            mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
            tabLayout1 = root.findViewById(R.id.tabLayout1)
            lytList = root.findViewById(R.id.lytList)
            lytGrid = root.findViewById(R.id.lytGrid)
            swipeLayout = root.findViewById(R.id.swipeLayout)
            tvAlert = root.findViewById(R.id.tvAlert)
            nestedScrollView = root.findViewById(R.id.nestedScrollView)
            tabLayout = root.findViewById(R.id.tabLayout)
            listView = root.findViewById(R.id.listView)
            searchView = root.findViewById(R.id.searchView)
            noResult = root.findViewById(R.id.noResult)
            msg = root.findViewById(R.id.msg)
            lytSearchView = root.findViewById(R.id.lytSearchView)

            if (session.getBoolean("grid")) {
                lytGrid.visibility = View.VISIBLE
                lytList.visibility = View.GONE
                resource = R.layout.lyt_item_grid
                isGrid = true
                recyclerView.layoutManager = GridLayoutManager(activity, 2)
            } else {
                lytGrid.visibility = View.GONE
                lytList.visibility = View.VISIBLE
                resource = R.layout.lyt_item_list
                isGrid = false
                recyclerView.layoutManager = LinearLayoutManager(activity)
            }

            val string = session.getData(Constant.GET_ALL_PRODUCTS_NAME).replace("[\"", "")
                .replace("[\"", "").split("\",\"")
            for (item in string) {
                productsName.add(item)
            }

            recyclerView.isNestedScrollingEnabled = false
            recyclerView.visibility = View.GONE
            mShimmerViewContainer.visibility = View.VISIBLE
            mShimmerViewContainer.startShimmer()

            ApiConfig.getSettings(activity)

            filterIndex = -1
            getProductData()
            swipeLayout.setColorSchemeResources(R.color.colorPrimary)
            swipeLayout.setOnRefreshListener {
                swipeLayout.isRefreshing = false
                offset = 0
                offsetFlashSaleNames = 0
                startShimmer()
                when (from) {
                    "regular", "sub_cate", "similar", "section", "flash_sale", "flash_sale_all" -> getData()
                    "search" -> {
                        stopShimmer()
                        lytSearchView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        Constant.CartValues = HashMap()
                        searchView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_search,
                            0,
                            R.drawable.ic_close_,
                            0
                        )
                        arrayAdapter = ArrayAdapter(
                            activity,
                            R.layout.spinner_search_item,
                            R.id.tvTitle,
                            removeDuplicates(productsName)
                        )
                        listView.adapter = arrayAdapter
                    }
                }
            }
            flashSalesLists = ArrayList()

            productLoadMoreAdapter = ProductLoadMoreAdapter(
                activity, productArrayList, resource, from, hashMap
            )

            tabLayout.viewTreeObserver.addOnScrollChangedListener {
                val windowSize = Point()
                val scrollX = tabLayout.scrollX
                val maxScrollWidth = tabLayout.getChildAt(0).measuredWidth - windowSize.x
                if (maxScrollWidth == scrollX && !tabLoading) {
                    tab = tabLayout.newTab().setText("Loading...").setTag("0")
                    tabLayout.addTab(tab)
                    offsetFlashSaleNames += Constant.LOAD_ITEM_LIMIT
                    getFlashSales(offsetFlashSaleNames, tab)
                }
            }

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (flashSalesLists.size != 0) {
                        offset = 0
                        tabLayout1.visibility = View.INVISIBLE
                        id = flashSalesLists[tab.position].id
                        getData()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            searchView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    arrayAdapter.filter.filter(
                        searchView.text.toString()
                            .lowercase(Locale.getDefault())
                    )
                    if (searchView.text.toString().isNotEmpty()) {
                        listView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        searchView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_search,
                            0,
                            R.drawable.ic_close,
                            0
                        )
                    } else {
                        listView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        searchView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_search,
                            0,
                            R.drawable.ic_close_,
                            0
                        )
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    try {
                        searchRequest("$s")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

            listView.onItemClickListener =
                AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
                    searchView.setText(arrayAdapter.getItem(position))

                    searchRequest(arrayAdapter.getItem(position).toString())
                }

            searchView.setOnTouchListener { _: View, event: MotionEvent ->
                val drawableRight = 2
                if (event.action == MotionEvent.ACTION_UP)
                    if (searchView.text.toString().isNotEmpty()) {
                        if (event.rawX >= searchView.right - searchView.compoundDrawables[drawableRight].bounds.width()) {
                            searchView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search,
                                0,
                                R.drawable.ic_close_,
                                0
                            )
                            searchView.setText("")
                        }
                        //                        return true;
                    }
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun getFlashSales(offset: Int, tab: TabLayout.Tab) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_ALL_FLASH_SALES] = Constant.GetVal
        params[Constant.OFFSET] = "" + offset
        params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        tabLayout.removeTab(tab)
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            totalFlashSales = jsonObject.getString(Constant.TOTAL).toInt()
                            if (flashSalesLists.size == 0 || flashSalesLists.size < totalFlashSales) {
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                                tabLayout.visibility = View.VISIBLE
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val flashSale = Gson().fromJson(
                                        jsonObject1.toString(),
                                        FlashSalesList::class.java
                                    )
                                    flashSalesLists.add(flashSale)
                                    val tab1 = tabLayout.newTab().setText(flashSale.title)
                                        .setTag(flashSale.id)
                                    tabLayout.addTab(tab1)
                                }
                            }
                        } else {
                            tabLoading = true
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    tabLoading = true
                }
            }
        }, activity, Constant.FLASH_SALES_URL, params, false)
    }

    private fun searchRequest(query: String) {
        listView.visibility = View.GONE
        productArrayList = ArrayList()
        startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.TYPE] = Constant.PRODUCT_SEARCH
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        params[Constant.SEARCH] = query
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
                                stopShimmer()
                                recyclerView.visibility = View.GONE
                                noResult.visibility = View.VISIBLE
                                msg.visibility = View.VISIBLE
                            }
                            if (offset == 0) {
                                productLoadMoreAdapter = ProductLoadMoreAdapter(
                                    activity, productArrayList, resource, from, hashMap
                                )
                                recyclerView.adapter = productLoadMoreAdapter
                                stopShimmer()
                                recyclerView.visibility = View.VISIBLE
                                noResult.visibility = View.GONE
                                msg.visibility = View.GONE
                                with(nestedScrollView) {
                                    setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
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
                                                        params1[Constant.TYPE] =
                                                            Constant.PRODUCT_SEARCH
                                                        if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                            session.getData(
                                                                Constant.ID
                                                            )
                                                        params1[Constant.SEARCH] = query
                                                        requestToVolley(
                                                            object :
                                                                VolleyCallback {
                                                                override fun onSuccess(
                                                                    result: Boolean,
                                                                    response: String
                                                                ) {
                                                                    if (result) {


                                                                        try {
                                                                            val jsonObject11 =
                                                                                JSONObject(
                                                                                    response
                                                                                )
                                                                            if (!jsonObject11.getBoolean(
                                                                                    Constant.ERROR
                                                                                )
                                                                            ) {
                                                                                val object1 =
                                                                                    JSONObject(
                                                                                        response
                                                                                    )
                                                                                val jsonArray1 =
                                                                                    object1.getJSONArray(
                                                                                        Constant.DATA
                                                                                    )
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
                                                                                productLoadMoreAdapter.notifyDataSetChanged()
                                                                                isLoadMore =
                                                                                    false
                                                                            }
                                                                        } catch (e: JSONException) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            activity,
                                                            Constant.PRODUCT_SEARCH_URL,
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
                            }
                        } else {
                            stopShimmer()
                            recyclerView.visibility = View.GONE
                            noResult.visibility = View.VISIBLE
                            msg.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        stopShimmer()
                        recyclerView.visibility = View.GONE
                        noResult.visibility = View.VISIBLE
                        msg.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, Constant.PRODUCT_SEARCH_URL, params, false)
    }

    fun getData() {
        productArrayList = ArrayList()
        recyclerView.adapter =
            ProductLoadMoreAdapter(activity, productArrayList, resource, from, hashMap)
        startShimmer()
        val params: MutableMap<String, String> = HashMap()
        when (from) {
            "regular", "sub_cate" -> {
                url = Constant.GET_PRODUCT_BY_SUB_CATE
                params[Constant.SUB_CATEGORY_ID] = id
                if (filterIndex != -1) {
                    params[Constant.SORT] = filterBy
                }
                isSort = true
            }
            "similar" -> {
                url = Constant.GET_SIMILAR_PRODUCT_URL
                assert(arguments != null)
                params[Constant.GET_SIMILAR_PRODUCT] = Constant.GetVal
                params[Constant.PRODUCT_ID] = id
                params[Constant.CATEGORY_ID] = arguments?.getString("cat_id").toString()
            }
            "section" -> {
                url = Constant.GET_SECTION_URL
                params[Constant.GET_ALL_SECTIONS] = Constant.GetVal
                params[Constant.SECTION_ID] = id
            }
            "flash_sale", "flash_sale_all" -> {
                url = Constant.FLASH_SALES_URL
                params[Constant.GET_ALL_FLASH_SALES_PRODUCTS] = Constant.GetVal
                params[Constant.FLASH_SALES_ID] = id
            }
        }
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
        params[Constant.OFFSET] = "" + offset
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
                                recyclerView.visibility = View.GONE
                                tvAlert.visibility = View.VISIBLE
                            }
                            if (offset == 0) {
                                productLoadMoreAdapter = ProductLoadMoreAdapter(
                                    activity, productArrayList, resource, from, hashMap
                                )
                                recyclerView.adapter = productLoadMoreAdapter
                                stopShimmer()
                                recyclerView.visibility = View.VISIBLE
                                tvAlert.visibility = View.GONE
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
                                                    when (from) {
                                                        "regular", "sub_cate" -> {
                                                            params1[Constant.SUB_CATEGORY_ID] = id
                                                            if (filterIndex != -1) {
                                                                params1[Constant.SORT] = filterBy
                                                            }
                                                            isSort = true
                                                        }
                                                        "similar" -> {
                                                            assert(arguments != null)
                                                            params1[Constant.GET_SIMILAR_PRODUCT] =
                                                                Constant.GetVal
                                                            params1[Constant.PRODUCT_ID] = id
                                                            params1[Constant.CATEGORY_ID] =
                                                                arguments?.getString("cat_id")
                                                                    .toString()
                                                        }
                                                        "section" -> {
                                                            params1[Constant.GET_ALL_SECTIONS] =
                                                                Constant.GetVal
                                                            params1[Constant.SECTION_ID] = id
                                                        }
                                                        "flash_sale", "flash_sale_all" -> {
                                                            params1[Constant.GET_ALL_FLASH_SALES_PRODUCTS] =
                                                                Constant.GetVal
                                                            params1[Constant.FLASH_SALES_ID] = id
                                                        }
                                                    }
                                                    if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                        session.getData(
                                                            Constant.ID
                                                        )
                                                    params1[Constant.LIMIT] =
                                                        "" + Constant.LOAD_ITEM_LIMIT
                                                    params1[Constant.OFFSET] = "" + offset
                                                    requestToVolley(object : VolleyCallback {
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
                                                                        productLoadMoreAdapter.notifyDataSetChanged()
                                                                        isLoadMore = false
                                                                    }
                                                                } catch (e: JSONException) {
                                                                    e.printStackTrace()
                                                                }
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
                            stopShimmer()
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        stopShimmer()
                        recyclerView.visibility = View.GONE
                        tvAlert.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, url, params, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                if (item1 != -1) getData()
                dialog.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        } else if (item.itemId == R.id.toolbar_layout) {
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
            productLoadMoreAdapter =
                ProductLoadMoreAdapter(activity, productArrayList, resource, from, hashMap)
            recyclerView.adapter = productLoadMoreAdapter
            productLoadMoreAdapter.notifyDataSetChanged()
            activity.invalidateOptionsMenu()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        activity.menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.toolbar_sort).isVisible = isSort
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
        assert(arguments != null)
        Constant.TOOLBAR_TITLE = arguments?.getString(Constant.NAME).toString()
        activity.invalidateOptionsMenu()
        if (arguments?.getString(Constant.FROM) == "search") {
            recyclerView.visibility = View.GONE
            searchView.requestFocus()
            showSoftKeyboard(searchView)
        } else {
            hideKeyboard()
        }
        val productArrayList1 = productArrayList
        if (Constant.countList.size != 0) {
            if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                productLoadMoreAdapter.notifyDataSetChanged()
            } else if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                productArrayList1.withIndex().forEach { (indexProduct, product_) ->
                    product_!!.variants.withIndex().forEach { (indexVariant, variants_) ->
                        val cart1 =
                            Cart(variants_.product_id, variants_.id, variants_.cart_count)
                        for (cart in Constant.countList) {
                            if (cart.product_id == cart1.product_id && cart.product_variant_id == cart1.product_variant_id) {
                                productArrayList[indexProduct]?.variants?.get(indexVariant)!!.cart_count =
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

    // Function to remove duplicates from an ArrayList
    private fun <T : Any> removeDuplicates(list: ArrayList<T>): ArrayList<T> {

        // Create a new ArrayList
        val newList = ArrayList<T>()

        // Traverse through the first list
        for (element in list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {
                newList.add(element)
            }
        }

        // return the new list
        return newList
    }

    override fun onPause() {
        super.onPause()
        addMultipleProductInCart(session, activity, Constant.CartValues)
    }

    companion object {
        lateinit var productArrayList: ArrayList<ProductList?>
        lateinit var productLoadMoreAdapter: ProductLoadMoreAdapter
    }
}