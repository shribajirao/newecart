package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.CategoryAdapter
import wrteam.ecart.shop.adapter.OfferAdapter
import wrteam.ecart.shop.adapter.SectionAdapter
import wrteam.ecart.shop.adapter.SliderAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMarkers
import wrteam.ecart.shop.helper.ApiConfig.Companion.getOfferImage
import wrteam.ecart.shop.helper.ApiConfig.Companion.getSettings
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Category
import wrteam.ecart.shop.model.Section
import wrteam.ecart.shop.model.Slider
import java.util.*

class HomeFragment : Fragment() {
    lateinit var session: Session
    lateinit var sliderArrayList: ArrayList<Slider>
    lateinit var activity: Activity
    lateinit var nestedScrollView: NestedScrollView
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var root: View
    private var timerDelay = 0
    private var timerWaiting = 0
    private lateinit var searchView: EditText
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var sectionView: RecyclerView
    lateinit var lytTopOfferImages: RecyclerView
    lateinit var lytBelowSliderOfferImages: RecyclerView
    lateinit var lytBelowCategoryOfferImages: RecyclerView
    lateinit var lytBelowFlashSaleOfferImages: RecyclerView
    lateinit var tabLayout: TabLayout
    private lateinit var mPager: ViewPager
    lateinit var viewPager: ViewPager
    lateinit var mMarkersLayout: LinearLayout
    var size = 0
    private lateinit var swipeTimer: Timer
    lateinit var handler: Handler
    lateinit var update: Runnable
    private var currentPage = 0
    private lateinit var lytCategory: LinearLayout
    private lateinit var lytSearchView: LinearLayout
    lateinit var lytFlashSale: LinearLayout
    lateinit var menu: Menu
    private lateinit var tvMore: TextView
    private lateinit var tvMoreFlashSale: TextView
    private var searchVisible = false
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_home, container, false)
        activity = requireActivity()
        session = Session(activity)
        timerDelay = 3000
        timerWaiting = 3000
        setHasOptionsMenu(true)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        categoryRecyclerView = root.findViewById(R.id.categoryRecyclerView)
        sectionView = root.findViewById(R.id.sectionView)
        sectionView.layoutManager = LinearLayoutManager(activity)
        sectionView.isNestedScrollingEnabled = false
        lytTopOfferImages = root.findViewById(R.id.lytTopOfferImages)
        lytTopOfferImages.layoutManager = LinearLayoutManager(activity)
        lytTopOfferImages.isNestedScrollingEnabled = false
        lytBelowSliderOfferImages = root.findViewById(R.id.lytBelowSliderOfferImages)
        lytBelowSliderOfferImages.layoutManager = LinearLayoutManager(activity)
        lytBelowSliderOfferImages.isNestedScrollingEnabled = false
        lytBelowCategoryOfferImages = root.findViewById(R.id.lytBelowCategoryOfferImages)
        lytBelowCategoryOfferImages.layoutManager = LinearLayoutManager(activity)
        lytBelowCategoryOfferImages.isNestedScrollingEnabled = false
        lytBelowFlashSaleOfferImages = root.findViewById(R.id.lytBelowFlashSaleOfferImages)
        lytBelowFlashSaleOfferImages.layoutManager = LinearLayoutManager(activity)
        lytBelowFlashSaleOfferImages.isNestedScrollingEnabled = false
        tabLayout = root.findViewById(R.id.tabLayout)
        viewPager = root.findViewById(R.id.viewPager)
        nestedScrollView = root.findViewById(R.id.nestedScrollView)
        mMarkersLayout = root.findViewById(R.id.layout_markers)
        lytCategory = root.findViewById(R.id.lytCategory)
        lytSearchView = root.findViewById(R.id.lytSearchView)
        lytSearchView = root.findViewById(R.id.lytSearchView)
        tvMoreFlashSale = root.findViewById(R.id.tvMoreFlashSale)
        lytFlashSale = root.findViewById(R.id.lytFlashSale)
        tvMore = root.findViewById(R.id.tvMore)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        searchView = root.findViewById(R.id.searchView)
        nestedScrollView.setOnScrollChangeListener { _: NestedScrollView, _: Int, _: Int, _: Int, _: Int ->
            val scrollBounds = Rect()
            nestedScrollView.getHitRect(scrollBounds)
            if (!lytSearchView.getLocalVisibleRect(scrollBounds) || scrollBounds.height() > lytSearchView.height) {
                searchVisible = true
                menu.findItem(R.id.toolbar_search).isVisible = true
            } else {
                searchVisible = false
                menu.findItem(R.id.toolbar_search).isVisible = false
            }
            activity.invalidateOptionsMenu()
        }
        tvMore.setOnClickListener {
            if (!MainActivity.categoryClicked) {
                MainActivity.fm.beginTransaction()
                    .add(R.id.container, MainActivity.categoryFragment).show(
                        MainActivity.categoryFragment
                    ).hide(MainActivity.active).commit()
                MainActivity.categoryClicked = true
            } else {
                MainActivity.fm.beginTransaction().show(MainActivity.categoryFragment).hide(
                    MainActivity.active
                ).commit()
            }
            MainActivity.bottomNavigationView.selectedItemId = R.id.navCategory
            MainActivity.active = MainActivity.categoryFragment
        }
        tvMoreFlashSale.setOnClickListener {
            val fragment: Fragment = ProductListFragment()
            val bundle = Bundle()
            bundle.putString("id", "")
            bundle.putString("cat_id", "")
            bundle.putString(Constant.FROM, "flash_sale_all")
            bundle.putString("name", activity.getString(R.string.flash_sales))
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        searchView.setOnTouchListener { _: View, _: MotionEvent ->
            val fragment: Fragment = ProductListFragment()
            val bundle = Bundle()
            bundle.putString(Constant.FROM, "search")
            bundle.putString(Constant.NAME, activity.getString(R.string.search))
            bundle.putString(Constant.ID, "")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
            false
        }
        lytSearchView.setOnClickListener {
            val fragment: Fragment = ProductListFragment()
            val bundle = Bundle()
            bundle.putString(Constant.FROM, "search")
            bundle.putString(Constant.NAME, activity.getString(R.string.search))
            bundle.putString(Constant.ID, "")
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        mPager = root.findViewById(R.id.pager)
        mPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(position: Int) {
                addMarkers(position, sliderArrayList, mMarkersLayout, activity)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
        categoryArrayList = ArrayList()
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            swipeTimer.cancel()
            if (isConnected(activity)) {
                getWalletBalance(activity, Session(activity))
                getHomeData()
            }
            swipeLayout.isRefreshing = false
        }
        if (isConnected(activity)) {
            getWalletBalance(activity, Session(activity))
            getHomeData()
        } else {
            nestedScrollView.visibility = View.VISIBLE
            mShimmerViewContainer.visibility = View.GONE
            mShimmerViewContainer.stopShimmer()
        }
        return root
    }

    private fun getHomeData() {
        nestedScrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(Constant.ID)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (jsonObject.getJSONArray(Constant.FLASH_SALES).length() != 0) {
                                lytFlashSale.visibility = View.VISIBLE
                                getOfferImage(
                                    activity,
                                    jsonObject.getJSONArray(Constant.FLASH_SALE_OFFER_IMAGES),
                                    lytBelowFlashSaleOfferImages
                                )
                                getFlashSale(jsonObject.getJSONArray(Constant.FLASH_SALES))
                            } else {
                                lytFlashSale.visibility = View.GONE
                            }
                            getData(jsonObject)
                            if (jsonObject.getJSONArray(Constant.SECTIONS).length() != 0) {
                                sectionProductRequest(jsonObject.getJSONArray(Constant.SECTIONS))
                            }
                            getSlider(jsonObject.getJSONArray(Constant.SLIDER_IMAGES))
                            getOfferImage(
                                activity,
                                jsonObject.getJSONArray(Constant.OFFER_IMAGES),
                                lytTopOfferImages
                            )
                            getOfferImage(
                                activity,
                                jsonObject.getJSONArray(Constant.SLIDER_OFFER_IMAGES),
                                lytBelowSliderOfferImages
                            )
                            getOfferImage(
                                activity,
                                jsonObject.getJSONArray(Constant.CATEGORY_OFFER_IMAGES),
                                lytBelowCategoryOfferImages
                            )
                        } else {
                            nestedScrollView.visibility = View.VISIBLE
                            mShimmerViewContainer.visibility = View.GONE
                            mShimmerViewContainer.stopShimmer()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        nestedScrollView.visibility = View.VISIBLE
                        mShimmerViewContainer.visibility = View.GONE
                        mShimmerViewContainer.stopShimmer()
                    }
                }
            }
        }, activity, Constant.GET_ALL_DATA_URL, params, false)
    }

    fun getFlashSale(jsonArray: JSONArray) {
        try {
            tabLayout.removeAllTabs()
            for (i in 0 until jsonArray.length()) {
                tabLayout.addTab(
                    tabLayout.newTab().setText(
                        jsonArray.getJSONObject(i).getString(
                            Constant.TITLE
                        )
                    )
                )
            }
            val tabAdapter = TabAdapter(MainActivity.fm, tabLayout.tabCount, jsonArray)
            viewPager.adapter = tabAdapter
            viewPager.offscreenPageLimit = 1
            viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
            tabLayout.setOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewPager.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

//            tabLayout.setupWithViewPager(viewPager);
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getData(jsonObject: JSONObject) {
        categoryArrayList = ArrayList()
        try {
            val visibleCount: Int
            val columnCount: Int
            val jsonArray = jsonObject.getJSONArray(Constant.CATEGORIES)
            if (jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val jsonObject1 = jsonArray.getJSONObject(i)
                    val category = Gson().fromJson(jsonObject1.toString(), Category::class.java)
                    categoryArrayList.add(category)
                }
                if (jsonObject.getString("style") != "") {
                    if (jsonObject.getString("style") == "style_1") {
                        visibleCount = jsonObject.getString("visible_count").toInt()
                        columnCount = jsonObject.getString("column_count").toInt()
                        categoryRecyclerView.layoutManager =
                            GridLayoutManager(activity, columnCount)
                        categoryRecyclerView.adapter = CategoryAdapter(
                            activity,
                            categoryArrayList,
                            R.layout.lyt_category_grid,
                            "home",
                            visibleCount
                        )
                    } else if (jsonObject.getString("style") == "style_2") {
                        visibleCount = jsonObject.getString("visible_count").toInt()
                        categoryRecyclerView.layoutManager =
                            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                        categoryRecyclerView.adapter = CategoryAdapter(
                            activity,
                            categoryArrayList,
                            R.layout.lyt_category_list,
                            "home",
                            visibleCount
                        )
                    }
                } else {
                    categoryRecyclerView.layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    categoryRecyclerView.adapter = CategoryAdapter(
                        activity,
                        categoryArrayList,
                        R.layout.lyt_category_list,
                        "home",
                        6
                    )
                }
            } else {
                lytCategory.visibility = View.GONE
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sectionProductRequest(jsonArray: JSONArray) {
        //json request for product search
        sectionList = ArrayList()
        sectionView.visibility = View.VISIBLE
        try {
            for (i in 0 until jsonArray.length()) {
                val section =
                    Gson().fromJson(jsonArray.getJSONObject(i).toString(), Section::class.java)
                if (section.products.size > 0) {
                    sectionList.add(section)
                }
            }
            val sectionAdapter = SectionAdapter(activity, sectionList, jsonArray)
            sectionView.adapter = sectionAdapter
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun getSlider(jsonArray: JSONArray) {
        sliderArrayList = ArrayList()
        try {
            size = jsonArray.length()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val slider = Gson().fromJson(jsonObject.toString(), Slider::class.java)
                sliderArrayList.add(slider)
            }
            mPager.adapter =
                SliderAdapter(sliderArrayList, activity, R.layout.lyt_slider, "home")
            addMarkers(0, sliderArrayList, mMarkersLayout, activity)
            handler = Handler()
            update = Runnable {
                if (currentPage == size) {
                    currentPage = 0
                }
                try {
                    mPager.setCurrentItem(currentPage++, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            swipeTimer = Timer()
            swipeTimer.schedule(object : TimerTask() {
                override fun run() {
                    handler.post(update)
                }
            }, timerDelay.toLong(), timerWaiting.toLong())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        nestedScrollView.visibility = View.VISIBLE
        mShimmerViewContainer.visibility = View.GONE
        mShimmerViewContainer.stopShimmer()
    }

    override fun onResume() {
        super.onResume()
        activity.invalidateOptionsMenu()
        getSettings(activity)
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
        this.menu = menu
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = searchVisible
    }

    class TabAdapter(fm: FragmentManager, private val mNumOfTabs: Int, val jsonArray: JSONArray) :
        FragmentStatePagerAdapter(
            fm
        ) {
        override fun getItem(position: Int): Fragment {
            lateinit var fragment: Fragment
            try {
                fragment = FlashSaleFragment.addFragment(jsonArray.getJSONObject(position))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return fragment
        }

        override fun getCount(): Int {
            return mNumOfTabs
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
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
    }

    companion object {
        lateinit var categoryArrayList: ArrayList<Category>
        lateinit var sectionList: ArrayList<Section>
    }
}