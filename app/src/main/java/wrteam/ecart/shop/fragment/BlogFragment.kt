package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.model.Blog

class BlogFragment : Fragment() {
    private lateinit var webViewBlog: WebView
    lateinit var imageView: ImageView
    lateinit var root: View
    lateinit var activity: Activity
    private lateinit var blog: Blog
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_blog, container, false)
        activity = requireActivity()
        setHasOptionsMenu(true)
        webViewBlog = root.findViewById(R.id.webViewBlog)
        imageView = root.findViewById(R.id.imageView)
        blog = arguments?.getSerializable("model") as Blog
        Picasso.get()
            .load(blog.image)
            .fit()
            .centerInside()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(imageView)
        webViewBlog.settings.javaScriptEnabled = true
        webViewBlog.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                webResourceRequest: WebResourceRequest
            ): Boolean {
                return if (webResourceRequest.url != null) {
                    view.context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(webResourceRequest.url.toString())
                        )
                    )
                    true
                } else {
                    false
                }
            }
        }
        webViewBlog.isVerticalScrollBarEnabled = true
        webViewBlog.loadDataWithBaseURL("", blog.description, "text/html", "UTF-8", "")
        return root
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = blog.title
        requireActivity().invalidateOptionsMenu()
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
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_layout).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = true
    }
}