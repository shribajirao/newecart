package wrteam.ecart.shop.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import wrteam.ecart.shop.R

class ProgressDisplay @SuppressLint("UseCompatLoadingForDrawables") constructor(context: Activity) {
    fun showProgress() {
        if (mProgressBar!!.visibility == View.GONE) mProgressBar!!.visibility = View.VISIBLE
    }

    fun hideProgress() {
        mProgressBar!!.visibility = View.GONE
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var mProgressBar: ProgressBar? = null
    }

    init {
        try {
            val layout = context.findViewById<View>(android.R.id.content).rootView as ViewGroup
            mProgressBar = ProgressBar(context, null, android.R.attr.progressBarStyle)
            mProgressBar!!.indeterminateDrawable =
                context.getDrawable(R.drawable.custom_progress_dialog)
            mProgressBar!!.isIndeterminate = true
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            val rl = RelativeLayout(context)
            rl.gravity = Gravity.CENTER
            rl.addView(mProgressBar)
            layout.addView(rl, params)
            hideProgress()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}