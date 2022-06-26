package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.ApiConfig.Companion.createJWT
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.VolleyCallback

class WebViewFragment : Fragment() {
    lateinit var prgLoading: ProgressBar
    lateinit var mWebView: WebView
    lateinit var type: String
    lateinit var root: View
    lateinit var activity: Activity
    private lateinit var btnPrint: Button
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_web_view, container, false)
        setHasOptionsMenu(true)
        activity = requireActivity()
        assert(arguments != null)
        type = arguments?.getString("type").toString()
        prgLoading = root.findViewById(R.id.prgLoading)
        mWebView = root.findViewById(R.id.webView1)
        btnPrint = root.findViewById(R.id.btnPrint)
        mWebView.settings.javaScriptEnabled = true
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return run {
                    view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
            }
        }
        try {
            if (isConnected(activity)) {
                when (type) {
                    "Privacy Policy" -> getContent(Constant.GET_PRIVACY, "privacy")
                    "Terms & Conditions" -> getContent(Constant.GET_TERMS, "terms")
                    "Contact Us" -> getContent(Constant.GET_CONTACT, "contact")
                    "About Us" -> getContent(Constant.GET_ABOUT_US, "about")
                    else -> getInvoice(type)
                }
                activity.invalidateOptionsMenu()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return root
    }

    fun getContent(type: String, key: String) {
        prgLoading.visibility = View.VISIBLE
        val params: MutableMap<String, String> = HashMap()
        params[Constant.SETTINGS] = Constant.GetVal
        params[type] = Constant.GetVal
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val obj = JSONObject(response)
                        if (!obj.getBoolean(Constant.ERROR)) {
                            val privacyStr = obj.getString(key)
                            mWebView.isVerticalScrollBarEnabled = true
                            mWebView.loadDataWithBaseURL("", privacyStr, "text/html", "UTF-8", "")
                            prgLoading.visibility = View.GONE
                        } else {
                            prgLoading.visibility = View.GONE
                            Toast.makeText(
                                activity,
                                obj.getString(Constant.MESSAGE),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                        prgLoading.visibility = View.GONE
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        prgLoading.visibility = View.GONE
                    }
                }
            }
        }, activity, Constant.SETTING_URL, params, false)
    }

    private fun getInvoice(type: String) {
        try {
            mWebView.loadUrl(
                Constant.INVOICE_URL + "id=" + type.split("#".toRegex())
                    .toTypedArray()[1] + "&token=" + createJWT("eKart", "eKart Authentication")
            )
            btnPrint.visibility = View.VISIBLE
            btnPrint.setOnClickListener {  createWebPagePrint(mWebView) }
        } catch (e: Exception) {
            e.printStackTrace()
            prgLoading.visibility = View.GONE
        }
    }

    private fun createWebPagePrint(webView: WebView) {
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView. createPrintDocumentAdapter()
        val jobName =
            getString(R.string.order) + "_" + type.split("#".toRegex()).toTypedArray()[1]
        val builder = PrintAttributes.Builder()
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        val printJob = printManager.print(jobName, printAdapter, builder.build())
        if (printJob.isCompleted) {
            Toast.makeText(activity, R.string.print_complete, Toast.LENGTH_SHORT).show()
        } else if (printJob.isFailed) {
            Toast.makeText(activity, R.string.print_failed, Toast.LENGTH_SHORT).show()
        }
        // Save the job object for later status checking
    }

    override fun onResume() {
        super.onResume()
        assert(arguments != null)
        Constant.TOOLBAR_TITLE = arguments?.getString("type").toString()
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
        menu.findItem(R.id.toolbar_search).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}