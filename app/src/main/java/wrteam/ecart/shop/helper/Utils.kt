package wrteam.ecart.shop.helper

import android.annotation.SuppressLint
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import wrteam.ecart.shop.R

object Utils {
    @JvmStatic
    @SuppressLint("ClickableViewAccessibility")
    fun setHideShowPassword(edtPassword: EditText) {
        edtPassword.tag = "show"
        edtPassword.setOnTouchListener { _: View, event: MotionEvent ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= edtPassword.right - edtPassword.compoundDrawables[drawableRight].bounds.width()) {
                    if (edtPassword.tag == "show") {
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_pass,
                            0,
                            R.drawable.ic_hide,
                            0
                        )
                        edtPassword.transformationMethod = null
                        edtPassword.tag = "hide"
                    } else {
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_pass,
                            0,
                            R.drawable.ic_show,
                            0
                        )
                        edtPassword.transformationMethod = PasswordTransformationMethod()
                        edtPassword.tag = "show"
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun setFormatTime(time: Long): String {
        return String.format("%02d", time)
    }
}