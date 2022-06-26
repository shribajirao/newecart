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
import androidx.core.content.ContextCompat
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
import wrteam.ecart.shop.adapter.NotificationAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Session.Companion.setCount
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Notification

class NotificationFragment : Fragment() {
    lateinit var root: View
    lateinit var recyclerView: RecyclerView
    lateinit var notifications: ArrayList<Notification?>
    lateinit var notificationAdapter: NotificationAdapter
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var scrollView: NestedScrollView
    lateinit var tvAlert: RelativeLayout
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
        root = inflater.inflate(R.layout.fragment_notification, container, false)
        activity = requireActivity()
        session = Session(activity)
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvAlert = root.findViewById(R.id.tvAlert)
        scrollView = root.findViewById(R.id.scrollView)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        setHasOptionsMenu(true)
        if (isConnected(activity)) {
            notificationData
        }
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            notifications.clear()
            offset = 0
            notificationData
            swipeLayout.isRefreshing = false
        }
        return root
    }

    fun stopShimmer() {
        scrollView.visibility = View.VISIBLE
        mShimmerViewContainer.visibility = View.GONE
        mShimmerViewContainer.stopShimmer()
    }

    fun startShimmer() {
        scrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
    }//bottom of list!

    // if (diff == 0) {
    @get:SuppressLint("NotifyDataSetChanged")
    val notificationData: Unit
        get() {
            startShimmer()
            notifications = ArrayList()
            val linearLayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = linearLayoutManager
            val params: MutableMap<String, String> = HashMap()
            params[Constant.GET_NOTIFICATIONS] = Constant.GetVal
            params[Constant.OFFSET] = "" + offset
            params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 10)
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
                                        val notification = Gson().fromJson(
                                            jsonObject1.toString(),
                                            Notification::class.java
                                        )
                                        notifications.add(notification)
                                    } else {
                                        break
                                    }
                                }
                                if (offset == 0) {
                                    notificationAdapter =
                                        NotificationAdapter(activity, notifications)
                                    recyclerView.adapter = notificationAdapter
                                    stopShimmer()
                                    scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                        // if (diff == 0) {
                                        if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                            if (notifications.size < total) {
                                                if (!isLoadMore) {
                                                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == notifications.size - 1) {
                                                        //bottom of list!


                                                        offset += Constant.LOAD_ITEM_LIMIT + 10
                                                        val params1: MutableMap<String, String> =
                                                            HashMap()
                                                        params1[Constant.GET_NOTIFICATIONS] =
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
                                                                            val jsonObject12 =
                                                                                JSONObject(response)
                                                                            if (!jsonObject12.getBoolean(
                                                                                    Constant.ERROR
                                                                                )
                                                                            ) {
                                                                                session.setData(
                                                                                    Constant.TOTAL,
                                                                                    jsonObject12.getString(
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
                                                                                        val notification =
                                                                                            Gson().fromJson(
                                                                                                jsonObject1.toString(),
                                                                                                Notification::class.java
                                                                                            )
                                                                                        notifications.add(
                                                                                            notification
                                                                                        )
                                                                                    } else {
                                                                                        break
                                                                                    }
                                                                                }
                                                                                notificationAdapter.notifyDataSetChanged()
                                                                                isLoadMore = false
                                                                            }
                                                                        } catch (e: JSONException) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            activity,
                                                            Constant.GET_SECTION_URL,
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
                                stopShimmer()
                                tvAlert.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            stopShimmer()
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.GET_SECTION_URL, params, false)
        }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.notifications)
        activity.invalidateOptionsMenu()
        setCount(Constant.UNREAD_NOTIFICATION_COUNT, 0, activity)
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