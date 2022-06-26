package wrteam.ecart.shop.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.SliderAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMarkers
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.model.Slider

class FullScreenViewFragment : Fragment() {
    lateinit var root: View
    private var pos = 0
    lateinit var imageList: ArrayList<Slider>
    lateinit var mMarkersLayout: LinearLayout
    lateinit var viewPager: ViewPager
    lateinit var activity: Activity
    lateinit var bundle: Bundle
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_full_screen_view, container, false)
        mMarkersLayout = root.findViewById(R.id.layout_markers)
        viewPager = root.findViewById(R.id.pager)
        activity = requireActivity()
        setHasOptionsMenu(true)
        bundle = requireArguments()
        imageList = bundle.getSerializable("images") as ArrayList<Slider>
        pos = bundle.getInt("pos", 0)
        viewPager.adapter = SliderAdapter(
            imageList,
            activity,
            R.layout.lyt_fullscreenimg,
            "fullscreen"
        )
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(position: Int) {
                addMarkers(position, imageList, mMarkersLayout, activity)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
        viewPager.currentItem = pos
        addMarkers(pos, imageList, mMarkersLayout, activity)
        return root
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.app_name)
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
}