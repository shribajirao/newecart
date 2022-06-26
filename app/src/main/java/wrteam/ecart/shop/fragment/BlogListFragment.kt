package wrteam.ecart.shop.fragment

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.BlogAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Blog

class BlogListFragment : Fragment() {
    lateinit var tvAlert: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var root: View
    lateinit var activity: Activity
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    lateinit var nestedScrollView: NestedScrollView
    var total = 0
    var offset = 0
    var isLoadMore = false
    lateinit var bundle: Bundle
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_blog_list, container, false)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        activity = requireActivity()
        bundle = requireArguments()
        setHasOptionsMenu(true)
        tvAlert = root.findViewById(R.id.tvAlert)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        recyclerView = root.findViewById(R.id.recyclerView)
        nestedScrollView = root.findViewById(R.id.nestedScrollView)
        recyclerView.layoutManager = GridLayoutManager(activity, Constant.GRID_COLUMN)
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            if (isConnected(activity)) {
                recyclerView.visibility = View.GONE
                mShimmerViewContainer.visibility = View.VISIBLE
                mShimmerViewContainer.startShimmer()
                getData()
            }
        }
        if (isConnected(activity)) {
            recyclerView.visibility = View.GONE
            mShimmerViewContainer.visibility = View.VISIBLE
            mShimmerViewContainer.startShimmer()
            getData()
        }
        return root
    }

    fun startShimmer() {
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
    }

    fun stopShimmer() {
        mShimmerViewContainer.stopShimmer()
        mShimmerViewContainer.visibility = View.GONE
    }//bottom of list!

    // if (diff == 0) {
    fun getData(){
            blogArrayList = ArrayList()
            startShimmer()
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_BLOGS] = Constant.GetVal
            params[Constant.CATEGORY_ID] = bundle.getString("id").toString()
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
                                        val blog = Gson().fromJson(
                                            jsonArray.getJSONObject(i).toString(),
                                            Blog::class.java
                                        )
                                        blogArrayList.add(blog)
                                    }
                                } catch (e: Exception) {
                                    stopShimmer()
                                    recyclerView.visibility = View.GONE
                                    tvAlert.visibility = View.GONE
                                }
                                if (offset == 0) {
                                    blogAdapter = BlogAdapter(activity, blogArrayList)
                                    recyclerView.adapter = blogAdapter
                                    stopShimmer()
                                    recyclerView.visibility = View.VISIBLE
                                    tvAlert.visibility = View.GONE
                                    nestedScrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
                                        // if (diff == 0) {
                                        if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                            val linearLayoutManager =
                                                recyclerView.layoutManager as LinearLayoutManager
                                            if (blogArrayList.size < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == blogArrayList.size - 1) {
                                                        //bottom of list!

                                                        offset += ("" + Constant.LOAD_ITEM_LIMIT).toInt()
                                                        val params1: MutableMap<String, String> =
                                                            HashMap()
                                                        params1[Constant.GET_BLOGS] =
                                                            Constant.GetVal
                                                        params1[Constant.CATEGORY_ID] =
                                                            bundle.getString("id").toString()
                                                        params1[Constant.LIMIT] =
                                                            "" + Constant.LOAD_ITEM_LIMIT
                                                        params1[Constant.OFFSET] = "" + offset
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

                                                                                val jsonArray1 =
                                                                                    jsonObject11.getJSONArray(
                                                                                        Constant.DATA
                                                                                    )
                                                                                for (i in 0 until jsonArray1.length()) {
                                                                                    val blog =
                                                                                        Gson().fromJson(
                                                                                            jsonArray1.getJSONObject(
                                                                                                i
                                                                                            )
                                                                                                .toString(),
                                                                                            Blog::class.java
                                                                                        )
                                                                                    blogArrayList.add(
                                                                                        blog
                                                                                    )
                                                                                }
                                                                                blogAdapter.notifyDataSetChanged()
                                                                                isLoadMore = false
                                                                            }
                                                                        } catch (e: JSONException) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            activity,
                                                            Constant.GET_BLOGS_URL,
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
                                stopShimmer()
                                recyclerView.visibility = View.GONE
                                tvAlert.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            stopShimmer()
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.GONE
                        }
                    }
                }
            }, activity, Constant.GET_BLOGS_URL, params, false)
        }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = bundle.getString("title").toString()
        requireActivity().invalidateOptionsMenu()
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
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_layout).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = true
    }

    companion object {
        lateinit var blogArrayList: ArrayList<Blog?>
        lateinit var blogAdapter: BlogAdapter
    }
}