package wrteam.ecart.shop.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import wrteam.ecart.shop.fragment.SliderItemFragment

class SliderPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {
    override fun getItem(position: Int): Fragment {
        return SliderItemFragment.newInstance(position)
    }

    // size is hardcoded
    override fun getCount(): Int {
        return 3
    }
}