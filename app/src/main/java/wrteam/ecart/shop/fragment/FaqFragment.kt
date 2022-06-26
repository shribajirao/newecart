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
import android.widget.RelativeLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.FaqAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Faq

class FaqFragment : Fragment() {
    lateinit var root: View
    lateinit var recyclerView: RecyclerView
    lateinit var faqs: ArrayList<Faq?>
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var scrollView: NestedScrollView
    lateinit var tvAlert: RelativeLayout
    lateinit var faqAdapter: FaqAdapter
    var total = 0
    lateinit var activity: Activity
    var offset = 0
    lateinit var session: Session
    var isLoadMore = false
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_faq, container, false)
        activity = requireActivity()
        session = Session(activity)
        scrollView = root.findViewById(R.id.scrollView)
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvAlert = root.findViewById(R.id.tvAlert)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        setHasOptionsMenu(true)
        getFaqData()
        swipeLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            offset = 0
            getFaqData()
        }
        return root
    }// System.out.println("====product  " + response);//bottom of list!// if (diff == 0) {

    //                        System.out.println("====transaction " + response);
    @SuppressLint("NotifyDataSetChanged")
    private fun getFaqData() {
            recyclerView.visibility = View.GONE
            mShimmerViewContainer.visibility = View.VISIBLE
            mShimmerViewContainer.startShimmer()
            faqs = ArrayList()
            val linearLayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = linearLayoutManager
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_FAQS] = Constant.GetVal
            params[Constant.OFFSET] = "" + offset
            params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 10)
            requestToVolley(
                object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {
                        if (result) {
                            try {
//                        System.out.println("====transaction " + response);
                                val jsonObject = JSONObject(response)
                                if (!jsonObject.getBoolean(Constant.ERROR)) {
                                    total = jsonObject.getString(Constant.TOTAL).toInt()
                                    
                                    val jsonArray = jsonObject.getJSONArray(Constant.DATA)

                                    for (i in 0 until jsonArray.length()) {
                                        val jsonObject1 = jsonArray.getJSONObject(i)
                                        if (jsonObject1 != null) {
                                            val faq =
                                                Gson().fromJson(
                                                    jsonObject1.toString(),
                                                    Faq::class.java
                                                )
                                            faqs.add(faq)
                                        } else {
                                            break
                                        }
                                    }
                                    if (offset == 0) {
                                        faqAdapter = FaqAdapter(activity, faqs)
                                        recyclerView.adapter = faqAdapter
                                        mShimmerViewContainer.stopShimmer()
                                        mShimmerViewContainer.visibility = View.GONE
                                        recyclerView.visibility = View.VISIBLE
                                        scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                            // if (diff == 0) {
                                            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                                val linearLayoutManager1 =
                                                    recyclerView.layoutManager as LinearLayoutManager
                                                if (faqs.size < total) {
                                                    if (!isLoadMore) {
                                                        if (linearLayoutManager1.findLastCompletelyVisibleItemPosition() == faqs.size - 1) {
                                                            //bottom of list!

                                                            offset += Constant.LOAD_ITEM_LIMIT
                                                            val params1: MutableMap<String, String> =
                                                                HashMap()
                                                            params1[Constant.GET_FAQS] =
                                                                Constant.GetVal
                                                            params1[Constant.OFFSET] = "" + offset
                                                            params1[Constant.LIMIT] =
                                                                "" + (Constant.LOAD_ITEM_LIMIT + 10)
                                                            requestToVolley(
                                                                object : VolleyCallback {
                                                                    override fun onSuccess(
                                                                        result: Boolean,
                                                                        response: String
                                                                    ) {
                                                                        if (result) {
                                                                            try {
                                                                                // System.out.println("====product  " + response);
                                                                                val jsonObject2 =
                                                                                    JSONObject(
                                                                                        response
                                                                                    )
                                                                                if (!jsonObject2.getBoolean(
                                                                                        Constant.ERROR
                                                                                    )
                                                                                ) {
                                                                                    session.setData(
                                                                                        Constant.TOTAL,
                                                                                        jsonObject2.getString(
                                                                                            Constant.TOTAL
                                                                                        )
                                                                                    )

                                                                                    val object1 =
                                                                                        JSONObject(
                                                                                            response
                                                                                        )
                                                                                    val jsonArray1 =
                                                                                        object1.getJSONArray(
                                                                                            Constant.DATA
                                                                                        )
                                                                                    for (i in 0 until jsonArray1.length()) {
                                                                                        val jsonObject1 =
                                                                                            jsonArray1.getJSONObject(
                                                                                                i
                                                                                            )
                                                                                        if (jsonObject1 != null) {
                                                                                            val faq =
                                                                                                Gson().fromJson(
                                                                                                    jsonObject1.toString(),
                                                                                                    Faq::class.java
                                                                                                )
                                                                                            faqs.add(
                                                                                                faq
                                                                                            )
                                                                                        } else {
                                                                                            break
                                                                                        }
                                                                                    }
                                                                                    faqAdapter.notifyDataSetChanged()
                                                                                    isLoadMore =
                                                                                        false
                                                                                }
                                                                            } catch (e: JSONException) {
                                                                                mShimmerViewContainer.stopShimmer()
                                                                                mShimmerViewContainer.visibility =
                                                                                    View.GONE
                                                                                recyclerView.visibility =
                                                                                    View.VISIBLE
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                activity,
                                                                Constant.FAQ_URL,
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
                                    mShimmerViewContainer.stopShimmer()
                                    mShimmerViewContainer.visibility = View.GONE
                                    recyclerView.visibility = View.VISIBLE
                                }
                            } catch (e: JSONException) {
                                mShimmerViewContainer.stopShimmer()
                                mShimmerViewContainer.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                            }
                        }
                    }
                },
                activity,
                Constant.FAQ_URL,
                params,
                false
            )
        }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.faq)
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