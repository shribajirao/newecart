package wrteam.ecart.shop.helper

import android.text.Selection
import android.text.Spannable
import android.text.method.MovementMethod
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView

class DefaultMovementMethod internal constructor() : MovementMethod {
    override fun initialize(widget: TextView, text: Spannable) {
        // It will mark the IMM as openable
        Selection.setSelection(text, 0)
    }

    override fun onKeyDown(
        widget: TextView,
        text: Spannable,
        keyCode: Int,
        event: KeyEvent
    ): Boolean {
        return false
    }

    override fun onKeyUp(
        widget: TextView,
        text: Spannable,
        keyCode: Int,
        event: KeyEvent
    ): Boolean {
        return false
    }

    override fun onKeyOther(view: TextView, text: Spannable, event: KeyEvent): Boolean {
        return false
    }

    override fun onTakeFocus(widget: TextView, text: Spannable, direction: Int) {}
    override fun onTrackballEvent(widget: TextView, text: Spannable, event: MotionEvent): Boolean {
        return false
    }

    override fun onTouchEvent(widget: TextView, text: Spannable, event: MotionEvent): Boolean {
        return false
    }

    override fun onGenericMotionEvent(
        widget: TextView,
        text: Spannable,
        event: MotionEvent
    ): Boolean {
        return false
    }

    override fun canSelectArbitrarily(): Boolean {
        return false
    }

    companion object {
        private lateinit var sInstance: DefaultMovementMethod
        @JvmStatic
        val instance: MovementMethod
            get() {
                return sInstance
            }
    }
}