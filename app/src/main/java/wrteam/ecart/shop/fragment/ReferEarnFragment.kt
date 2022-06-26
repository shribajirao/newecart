package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session

class ReferEarnFragment : Fragment() {
    lateinit var root: View
    private lateinit var edtReferCoin: TextView
    private lateinit var edtCode: TextView
    private lateinit var edtCopy: TextView
    private lateinit var edtInvite: TextView
    lateinit var session: Session
    var preText = ""
    lateinit var activity: Activity
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_refer_earn, container, false)
        activity = requireActivity()
        setHasOptionsMenu(true)
        session = Session(activity)
        edtReferCoin = root.findViewById(R.id.edtReferCoin)
        preText = if (session.getData(Constant.refer_earn_method) == "rupees") {
            session.getData(Constant.currency) + session.getData(Constant.refer_earn_bonus)
        } else {
            session.getData(Constant.refer_earn_bonus) + "% "
        }
        edtReferCoin.text = getString(R.string.refer_text_1) + preText + getString(R.string.refer_text_2) + session.getData(
            Constant.currency
        ) + session.getData(Constant.min_refer_earn_order_amount) + getString(R.string.refer_text_3) + session.getData(
            Constant.currency
        ) + session.getData(Constant.max_refer_earn_amount) + "."
        edtCode = root.findViewById(R.id.edtCode)
        edtCopy = root.findViewById(R.id.edtCopy)
        edtInvite = root.findViewById(R.id.edtInvite)
        edtInvite.setCompoundDrawablesWithIntrinsicBounds(
            AppCompatResources.getDrawable(
                activity,
                R.drawable.ic_share
            ), null, null, null
        )
        edtCode.text = session.getData(Constant.REFERRAL_CODE)
        edtCopy.setOnClickListener {
            val clipboard =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", edtCode.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, R.string.refer_code_copied, Toast.LENGTH_SHORT).show()
        }
        edtInvite.setOnClickListener {
            if (edtCode.text.toString() != "code") {
                try {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "")
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        """${getString(R.string.refer_share_msg_1)}${resources.getString(R.string.app_name)}${
                            getString(R.string.refer_share_msg_2)
                        }
 ${Constant.WebsiteUrl}refer/${edtCode.text}"""
                    )
                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            getString(R.string.invite_friend_title)
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.refer_code_alert_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.refer_and_earn)
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
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}