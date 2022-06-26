package wrteam.ecart.shop.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
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
import wrteam.ecart.shop.adapter.TransactionAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Session.Companion.setCount
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Transaction

class TransactionFragment : Fragment() {
    lateinit var root: View
    lateinit var recyclerView: RecyclerView
    lateinit var transactions: ArrayList<Transaction?>
    lateinit var tvAlert: RelativeLayout
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var scrollView: NestedScrollView
    lateinit var transactionAdapter: TransactionAdapter
    var total = 0
    lateinit var activity: Activity
    lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertSubTitle: TextView
    var offset = 0
    lateinit var session: Session
    var isLoadMore = false
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_transection, container, false)
        offset = 0
        activity = requireActivity()
        session = Session(activity)
        setHasOptionsMenu(true)
        scrollView = root.findViewById(R.id.scrollView)
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvAlert = root.findViewById(R.id.tvAlert)
        tvAlertTitle = root.findViewById(R.id.tvAlertTitle)
        tvAlertSubTitle = root.findViewById(R.id.tvAlertSubTitle)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        tvAlertTitle.text = getString(R.string.no_transaction_history_found)
        tvAlertSubTitle.text = getString(R.string.you_have_not_any_transactional_history_yet)
        if (isConnected(activity)) {
            getTransactionData()
        }
        swipeLayout.setColorSchemeResources(R.color.colorPrimary)
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            offset = 0
            getTransactionData()
        }
        return root
    }//bottom of list!

    // if (diff == 0) {
    fun getTransactionData() {
            recyclerView.visibility = View.GONE
            mShimmerViewContainer.visibility = View.VISIBLE
            mShimmerViewContainer.startShimmer()
            transactions = ArrayList()
            val linearLayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = linearLayoutManager
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_USER_TRANSACTION] = Constant.GetVal
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            params[Constant.TYPE] = Constant.TYPE_TRANSACTION
            params[Constant.OFFSET] = "" + offset
            params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                total = jsonObject.getString(Constant.TOTAL).toInt()

                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    if (jsonObject1 != null) {
                                        val transaction = Gson().fromJson(
                                            jsonObject1.toString(),
                                            Transaction::class.java
                                        )
                                        transactions.add(transaction)
                                    } else {
                                        break
                                    }
                                }
                                if (offset == 0) {
                                    transactionAdapter =
                                        TransactionAdapter(activity, activity, transactions)
                                    recyclerView.adapter = transactionAdapter
                                    mShimmerViewContainer.stopShimmer()
                                    mShimmerViewContainer.visibility = View.GONE
                                    recyclerView.visibility = View.VISIBLE
                                    scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                        // if (diff == 0) {
                                        if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                            val linearLayoutManager1 =
                                                recyclerView.layoutManager as LinearLayoutManager
                                            if (transactions.size < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager1.findLastCompletelyVisibleItemPosition() == transactions.size - 1) {
                                                        //bottom of list!


                                                        offset += Constant.LOAD_ITEM_LIMIT
                                                        val params1: MutableMap<String, String> =
                                                            HashMap()
                                                        params1[Constant.GET_USER_TRANSACTION] =
                                                            Constant.GetVal
                                                        if (session.getBoolean(Constant.IS_USER_LOGIN)) params1[Constant.USER_ID] =
                                                            session.getData(
                                                                Constant.ID
                                                            )
                                                        params1[Constant.TYPE] =
                                                            Constant.TYPE_TRANSACTION
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
                                                                            val jsonObject2 =
                                                                                JSONObject(response)


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
                                                                                        val transaction =
                                                                                            Gson().fromJson(
                                                                                                jsonObject1.toString(),
                                                                                                Transaction::class.java
                                                                                            )
                                                                                        transactions.add(
                                                                                            transaction
                                                                                        )
                                                                                    } else {
                                                                                        break
                                                                                    }
                                                                                }
                                                                                transactionAdapter.notifyDataSetChanged()
                                                                                isLoadMore = false
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
                                                            Constant.TRANSACTION_URL,
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
            }, activity, Constant.TRANSACTION_URL, params, false)
        }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.transaction_history)
        activity.invalidateOptionsMenu()
        setCount(Constant.UNREAD_TRANSACTION_COUNT, 0, activity)
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