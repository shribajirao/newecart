package wrteam.ecart.shop.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.SliderPagerAdapter
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session

class WelcomeActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var tvNext: TextView
    private lateinit var adapter: SliderPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // bind views
        viewPager = findViewById(R.id.pagerIntroSlider)
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)
        tvNext = findViewById(R.id.tvNext)

        // init slider pager adapter
        adapter = SliderPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )

        // set adapter
        viewPager.adapter = adapter

        // set dot indicators
        tabLayout.setupWithViewPager(viewPager)
        tvNext.setOnClickListener {
            if (viewPager.currentItem + 1 < adapter.count) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                Session(this@WelcomeActivity).setBoolean("is_first_time", true)
                Session(this@WelcomeActivity).setBoolean("isCartFirstTime", true)
                startActivity(
                    Intent(this@WelcomeActivity, MainActivity::class.java).putExtra(
                        Constant.FROM,
                        ""
                    )
                )
                finish()
            }
        }
        tvSkip.setOnClickListener { 
            Session(this@WelcomeActivity).setBoolean("is_first_time", true)
            Session(this@WelcomeActivity).setBoolean("isCartFirstTime", true)
            startActivity(
                Intent(
                    this@WelcomeActivity,
                    MainActivity::class.java
                ).putExtra(Constant.FROM, "")
            )
            finish()
        }
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == adapter.count - 1) {
                    tvNext.setText(R.string.get_started)
                } else {
                    tvNext.setText(R.string.next)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
}