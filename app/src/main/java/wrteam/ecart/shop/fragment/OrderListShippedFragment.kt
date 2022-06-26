package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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
import com.facebook.shimmer.ShimmerFrameLayout
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.OrderListAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.getOrders
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.OrderList

class OrderListShippedFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var tvAlert: TextView
    lateinit var session: Session
    lateinit var activity: Activity
    lateinit var root: View
    lateinit var orderListArrayList: ArrayList<OrderList?>
    lateinit var orderListAdapter: OrderListAdapter
    private var offset = 0
    private var total = 0
    private lateinit var scrollView: NestedScrollView
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_order_list, container, false)
        activity = requireActivity()
        session = Session(activity)
        recyclerView = root.findViewById(R.id.recyclerView)
        scrollView = root.findViewById(R.id.scrollView)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        tvAlert = root.findViewById(R.id.tvAlert)
        setHasOptionsMenu(true)
        val swipeLayout: SwipeRefreshLayout = root.findViewById(R.id.swipeLayout)
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            offset = 0
            swipeLayout.isRefreshing = false
            getAllOrders()
        }
        getAllOrders()
        return root
    }// System.out.println("====product  " + response);//bottom of list!

    // if (diff == 0) {
    private fun getAllOrders() {
            orderListArrayList = ArrayList()
            val linearLayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = linearLayoutManager
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_ORDERS] = Constant.GetVal
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            params[Constant.STATUS] = Constant.SHIPPED
            params[Constant.OFFSET] = "" + offset
            params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                total = jsonObject.getString(Constant.TOTAL).toInt()
                                orderListArrayList.addAll(
                                    getOrders(
                                        jsonObject.getJSONArray(
                                            Constant.DATA
                                        )
                                    )
                                )
                                if (offset == 0) {
                                    orderListAdapter =
                                        OrderListAdapter(activity, activity, orderListArrayList)
                                    recyclerView.adapter = orderListAdapter
                                    mShimmerViewContainer.stopShimmer()
                                    mShimmerViewContainer.visibility = View.GONE
                                    recyclerView.visibility = View.VISIBLE
                                    scrollView.setOnScrollChangeListener(object :
                                        NestedScrollView.OnScrollChangeListener {
                                        private var isLoadMore = false

                                        @SuppressLint("NotifyDataSetChanged")
                                        override fun onScrollChange(
                                            v: NestedScrollView,
                                            scrollX: Int,
                                            scrollY: Int,
                                            oldScrollX: Int,
                                            oldScrollY: Int
                                        ) {

                                            // if (diff == 0) {
                                            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                                val linearLayoutManager1 =
                                                    recyclerView.layoutManager as LinearLayoutManager
                                                if (orderListArrayList.size < total) {
                                                    if (!isLoadMore) {
                                                        if (linearLayoutManager1.findLastCompletelyVisibleItemPosition() == orderListArrayList.size - 1) {
                                                            //bottom of list!
                                                            
                                                            offset += Constant.LOAD_ITEM_LIMIT
                                                            val params1: MutableMap<String, String> =
                                                                HashMap()
                                                            params1[Constant.GET_ORDERS] =
                                                                Constant.GetVal
                                                            if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                                session.getData(
                                                                    Constant.ID
                                                                )
                                                            params1[Constant.STATUS] =
                                                                Constant.SHIPPED
                                                            params1[Constant.OFFSET] = "" + offset
                                                            params1[Constant.LIMIT] =
                                                                "" + Constant.LOAD_ITEM_LIMIT
                                                            requestToVolley(
                                                                object : VolleyCallback {
                                                                    override fun onSuccess(
                                                                        result: Boolean,
                                                                        response: String
                                                                    ) {
                                                                        if (result) {
                                                                            try {
                                                                                // System.out.println("====product  " + response);
                                                                                val jsonObject1 =
                                                                                    JSONObject(
                                                                                        response
                                                                                    )
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
                                                                                    
                                                                                    orderListAdapter.notifyItemRemoved(
                                                                                        orderListArrayList.size
                                                                                    )
                                                                                    val object1 =
                                                                                        JSONObject(
                                                                                            response
                                                                                        )
                                                                                    orderListArrayList.addAll(
                                                                                        getOrders(
                                                                                            object1.getJSONArray(
                                                                                                Constant.DATA
                                                                                            )
                                                                                        )
                                                                                    )
                                                                                    orderListAdapter.notifyDataSetChanged()
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
                                                                Constant.ORDER_PROCESS_URL,
                                                                params1,
                                                                false
                                                            )
                                                        }
                                                        isLoadMore = true
                                                    }
                                                }
                                            }
                                        }
                                    })
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
            }, activity, Constant.ORDER_PROCESS_URL, params, false)
        }

    override fun onResume() {
        super.onResume()
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
}