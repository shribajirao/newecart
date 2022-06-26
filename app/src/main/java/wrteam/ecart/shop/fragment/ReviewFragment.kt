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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.ReviewAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Review

class ReviewFragment : Fragment() {
    lateinit var root: View
    lateinit var recyclerView: RecyclerView
    lateinit var reviewArrayList: ArrayList<Review?>
    lateinit var reviewAdapter: ReviewAdapter
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var scrollView: NestedScrollView
    lateinit var tvAlert: TextView
    var total = 0
    lateinit var activity: Activity
    var offset = 0
    lateinit var session: Session
    var isLoadMore = false
    lateinit var from: String
    lateinit var productId: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_review, container, false)
        activity = requireActivity()
        session = Session(activity)
        assert(arguments != null)
        from = arguments?.getString(Constant.FROM).toString()
        productId = arguments?.getString(Constant.ID).toString()
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvAlert = root.findViewById(R.id.tvAlert)
        scrollView = root.findViewById(R.id.scrollView)
        setHasOptionsMenu(true)
        if (isConnected(activity)) {
            notificationData
        }
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            reviewArrayList.clear()
            offset = 0
            notificationData
            swipeLayout.isRefreshing = false
        }
        return root
    }//bottom of list!

    // if (diff == 0) {
    @get:SuppressLint("NotifyDataSetChanged")
    val notificationData: Unit
        get() {
            reviewArrayList = ArrayList()
            val linearLayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = linearLayoutManager
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_PRODUCT_REVIEW] = Constant.GetVal
            params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 10)
            params[Constant.OFFSET] = "" + offset
            if (from == "share") {
                params[Constant.SLUG] = productId
            } else {
                params[Constant.PRODUCT_ID] = productId
            }
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                total = jsonObject.getString(Constant.NUMBER_OF_REVIEW).toInt()

                                val jsonArrayReviews =
                                    jsonObject.getJSONArray(Constant.PRODUCT_REVIEW)
                                for (i in 0 until jsonArrayReviews.length().coerceAtMost(5)) {
                                    val review = Gson().fromJson(
                                        jsonArrayReviews.getJSONObject(i).toString(),
                                        Review::class.java
                                    )
                                    reviewArrayList.add(review)
                                }
                                if (offset == 0) {
                                    reviewAdapter = ReviewAdapter(activity, reviewArrayList)
                                    recyclerView.adapter = reviewAdapter
                                    scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                        // if (diff == 0) {
                                        if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                            if (reviewArrayList.size < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == reviewArrayList.size - 1) {
                                                        //bottom of list!


                                                        offset += Constant.LOAD_ITEM_LIMIT + 10
                                                        val params1: MutableMap<String, String> =
                                                            HashMap()
                                                        params1[Constant.GET_PRODUCT_REVIEW] =
                                                            Constant.GetVal
                                                        params1[Constant.LIMIT] =
                                                            "" + (Constant.LOAD_ITEM_LIMIT + 10)
                                                        params1[Constant.OFFSET] = "" + offset
                                                        if (from == "share") {
                                                            params1[Constant.SLUG] = productId
                                                        } else {
                                                            params1[Constant.PRODUCT_ID] = productId
                                                        }
                                                        requestToVolley(
                                                            object : VolleyCallback {
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
                                                                                session.setData(
                                                                                    Constant.TOTAL,
                                                                                    jsonObject1.getString(
                                                                                        Constant.TOTAL
                                                                                    )
                                                                                )
                                                                                val jsonArrayReviews1 =
                                                                                    jsonObject.getJSONArray(
                                                                                        Constant.PRODUCT_REVIEW
                                                                                    )
                                                                                for (i in 0 until jsonArrayReviews1.length()
                                                                                    .coerceAtMost(5)) {
                                                                                    val review =
                                                                                        Gson().fromJson(
                                                                                            jsonArrayReviews1.getJSONObject(
                                                                                                i
                                                                                            )
                                                                                                .toString(),
                                                                                            Review::class.java
                                                                                        )
                                                                                    reviewArrayList.add(
                                                                                        review
                                                                                    )
                                                                                }
                                                                                reviewAdapter =
                                                                                    ReviewAdapter(
                                                                                        activity,
                                                                                        reviewArrayList
                                                                                    )
                                                                                recyclerView.adapter =
                                                                                    reviewAdapter
                                                                                reviewAdapter.notifyDataSetChanged()
                                                                                isLoadMore = false
                                                                            }
                                                                        } catch (e: JSONException) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            activity,
                                                            Constant.GET_ALL_PRODUCTS_URL,
                                                            params1,
                                                            false
                                                        )
                                                    }
                                                    isLoadMore = true
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                recyclerView.visibility = View.GONE
                                tvAlert.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.GET_ALL_PRODUCTS_URL, params, true)
        }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.reviews)
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
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}