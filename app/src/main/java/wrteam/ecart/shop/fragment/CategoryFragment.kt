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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.CategoryAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Category

class CategoryFragment : Fragment() {
    lateinit var tvAlert: TextView
    lateinit var categoryRecyclerView: RecyclerView
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var root: View
    lateinit var activity: Activity
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_category, container, false)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        activity = requireActivity()
        setHasOptionsMenu(true)
        tvAlert = root.findViewById(R.id.tvAlert)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        categoryRecyclerView = root.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = GridLayoutManager(activity, Constant.GRID_COLUMN)
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            if (isConnected(activity)) {
                categoryRecyclerView.visibility = View.GONE
                mShimmerViewContainer.visibility = View.VISIBLE
                mShimmerViewContainer.startShimmer()
                getWalletBalance(activity, Session(activity))
                getData()
            }
        }
        if (isConnected(activity)) {
            categoryRecyclerView.visibility = View.GONE
            mShimmerViewContainer.visibility = View.VISIBLE
            mShimmerViewContainer.startShimmer()
            getWalletBalance(activity, Session(activity))
            getData()
        }
        return root
    }

    private fun getData() {
        val params: MutableMap<String, String> = HashMap()
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        categoryArrayList = ArrayList()
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                            val gson = Gson()
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val category =
                                    gson.fromJson(jsonObject.toString(), Category::class.java)
                                categoryArrayList.add(category)
                            }
                            categoryRecyclerView.adapter = CategoryAdapter(
                                activity,
                                categoryArrayList,
                                R.layout.lyt_subcategory,
                                "category",
                                0
                            )
                            mShimmerViewContainer.stopShimmer()
                            mShimmerViewContainer.visibility = View.GONE
                            categoryRecyclerView.visibility = View.VISIBLE
                        } else {
                            tvAlert.visibility = View.VISIBLE
                            mShimmerViewContainer.stopShimmer()
                            mShimmerViewContainer.visibility = View.GONE
                            categoryRecyclerView.visibility = View.GONE
                        }
                    } catch (e: JSONException) {
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        categoryRecyclerView.visibility = View.GONE
                    }
                }
            }
        }, activity, Constant.CategoryUrl, params, false)
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.title_category)
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
        lateinit var categoryArrayList: ArrayList<Category>
    }
}