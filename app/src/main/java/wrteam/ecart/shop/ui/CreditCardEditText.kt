package wrteam.ecart.shop.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.SparseArray
import androidx.appcompat.widget.AppCompatEditText
import wrteam.ecart.shop.R
import java.util.regex.Pattern

class CreditCardEditText : AppCompatEditText {
    private val mDefaultDrawableResId = R.drawable.ic_credit_cards //default credit card image
    private var mCCPatterns: SparseArray<Pattern>? = null
    private var mCurrentDrawableResId = 0
    private var mCurrentDrawable: Drawable? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    fun init() {
        if (mCCPatterns == null) {
            mCCPatterns = SparseArray()
            // Without spaces for credit card masking
            mCCPatterns!!.put(R.drawable.ic_card_visa, Pattern.compile("^4[0-9]{2,12}(:[0-9]{3})$"))
            mCCPatterns!!.put(R.drawable.ic_card_mastercard, Pattern.compile("^5[1-5][0-9]{1,14}$"))
            mCCPatterns!!.put(R.drawable.ic_card_amex, Pattern.compile("^3[47][0-9]{1,13}$"))
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (mCCPatterns == null) {
            init()
        }
        var mDrawableResId = 0
        for (i in 0 until mCCPatterns!!.size()) {
            val key = mCCPatterns!!.keyAt(i)
            // get the object by the key.
            val p = mCCPatterns!![key]
            val m = p.matcher(text)
            if (m.find()) {
                mDrawableResId = key
                break
            }
        }
        if (mDrawableResId > 0 && mDrawableResId != mCurrentDrawableResId) {
            mCurrentDrawableResId = mDrawableResId
        } else if (mDrawableResId == 0) {
            mCurrentDrawableResId = mDefaultDrawableResId
        }
        mCurrentDrawable = resources.getDrawable(mCurrentDrawableResId)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mCurrentDrawable == null) {
            return
        }
        var rightOffset = 0
        if (error != null && error.isNotEmpty()) {
            rightOffset = resources.displayMetrics.density.toInt() * 32
        }
        val right = width - paddingRight - rightOffset
        val top = paddingTop
        val bottom = height - paddingBottom
        val ratio = mCurrentDrawable!!.intrinsicWidth.toFloat() / mCurrentDrawable!!.intrinsicHeight
            .toFloat()
        //int left = right - mCurrentDrawable.getIntrinsicWidth(); //If images are correct size.
        val left =
            (right - (bottom - top) * ratio).toInt() //scale image depending on height available.
        mCurrentDrawable!!.setBounds(left, top, right, bottom)
        mCurrentDrawable!!.draw(canvas)
    }
}