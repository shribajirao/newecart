package wrteam.ecart.shop.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.LoginActivity
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session

class TrackOrderFragment : Fragment() {
    lateinit var root: View
    private lateinit var lytEmpty: LinearLayout
    private lateinit var lytDate: LinearLayout
    lateinit var session: Session
    lateinit var tabs: Array<String>
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    lateinit var adapter: ViewPagerAdapter
    lateinit var activity: Activity
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_track_order, container, false)
        activity = requireActivity()
        session = Session(activity)
        tabs = if (session.getData(Constant.local_pickup) == "1") {
            arrayOf(
                getString(R.string.all),
                getString(R.string.pickup),
                getString(R.string.received),
                getString(R.string.processed),
                getString(R.string.shipped1),
                getString(R.string.delivered1),
                getString(R.string.cancelled1),
                getString(R.string.returned1)
            )
        } else {
            arrayOf(
                getString(R.string.all),
                getString(R.string.received),
                getString(R.string.processed),
                getString(R.string.shipped1),
                getString(R.string.delivered1),
                getString(R.string.cancelled1),
                getString(R.string.returned1)
            )
        }
        lytEmpty = root.findViewById(R.id.lytEmpty)
        lytDate = root.findViewById(R.id.lytDate)
        viewPager = root.findViewById(R.id.viewpager)
        viewPager.offscreenPageLimit = 5
        tabLayout = root.findViewById(R.id.lytTab)
        tabLayout.setupWithViewPager(viewPager)
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (isConnected(activity)) {
                getWalletBalance(activity, Session(activity))
                setupViewPager(viewPager)
            }
        } else {
            startActivity(
                Intent(activity, LoginActivity::class.java).putExtra(
                    Constant.FROM,
                    "tracker"
                )
            )
        }
        root.findViewById<View>(R.id.btnBorder).setOnClickListener { 
            MainActivity.fm.beginTransaction().show(
                MainActivity.homeFragment
            ).hide(
                MainActivity.active
            ).commit()
            MainActivity.bottomNavigationView.selectedItemId = 0
            MainActivity.homeClicked = true
        }
        return root
    }

    private fun setupViewPager(viewPager: ViewPager) {
        adapter = ViewPagerAdapter(MainActivity.fm)
        adapter.addFrag(OrderListAllFragment(), tabs[0])
        if (session.getData(Constant.local_pickup) == "1") {
            adapter.addFrag(OrderListPickupFragment(), tabs[1])
            adapter.addFrag(OrderListReceivedFragment(), tabs[2])
            adapter.addFrag(OrderListProcessedFragment(), tabs[3])
            adapter.addFrag(OrderListShippedFragment(), tabs[4])
            adapter.addFrag(OrderListDeliveredFragment(), tabs[5])
            adapter.addFrag(OrderListCancelledFragment(), tabs[6])
            adapter.addFrag(OrderListReturnedFragment(), tabs[7])
        } else {
            adapter.addFrag(OrderListReceivedFragment(), tabs[1])
            adapter.addFrag(OrderListProcessedFragment(), tabs[2])
            adapter.addFrag(OrderListShippedFragment(), tabs[3])
            adapter.addFrag(OrderListDeliveredFragment(), tabs[4])
            adapter.addFrag(OrderListCancelledFragment(), tabs[5])
            adapter.addFrag(OrderListReturnedFragment(), tabs[6])
        }
        viewPager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string._title_order_track)
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
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = true
    }

    inner class ViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(
        manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            val data = Bundle()
            data.putInt("pos", position)
            lateinit var fragment: Fragment
            if (position == 0) {
                fragment = OrderListAllFragment()
            } else {
                if (session.getData(Constant.local_pickup) == "1") {
                    when (position) {
                        1 -> {
                            fragment = OrderListPickupFragment()
                        }
                        2 -> {
                            fragment = OrderListReceivedFragment()
                        }
                        3 -> {
                            fragment = OrderListProcessedFragment()
                        }
                        4 -> {
                            fragment = OrderListShippedFragment()
                        }
                        5 -> {
                            fragment = OrderListDeliveredFragment()
                        }
                        6 -> {
                            fragment = OrderListCancelledFragment()
                        }
                        7 -> {
                            fragment = OrderListReturnedFragment()
                        }
                    }
                } else {
                    when (position) {
                        1 -> {
                            fragment = OrderListReceivedFragment()
                        }
                        2 -> {
                            fragment = OrderListProcessedFragment()
                        }
                        3 -> {
                            fragment = OrderListShippedFragment()
                        }
                        4 -> {
                            fragment = OrderListDeliveredFragment()
                        }
                        5 -> {
                            fragment = OrderListCancelledFragment()
                        }
                        6 -> {
                            fragment = OrderListReturnedFragment()
                        }
                    }
                }
            }
            fragment.arguments = data
            return fragment
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFrag(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }

        override fun getItemPosition(jsonObject: Any): Int {
            return POSITION_NONE
        }
    }
}