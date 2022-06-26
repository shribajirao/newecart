package wrteam.ecart.shop.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.SupportChatAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.createJWT
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Session.Companion.setCount
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.SupportChat
import wrteam.ecart.shop.model.SupportTicket
import java.io.File
import java.util.*

class SupportChatFragment : Fragment() {
    private val openMediaPicker = 1  // Request code
    private val permissionReadExternalStorage = 100       // Request code for read external storage
    lateinit var root: View
    private lateinit var recyclerViewImages: RecyclerView
    lateinit var supportChats: ArrayList<SupportChat>
    var total = 0
    lateinit var activity: Activity
    var offset = 0
    lateinit var session: Session
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var imgSelectImages: ImageView
    private lateinit var imgSendMessage: ImageView
    private lateinit var edtMessage: EditText
    private lateinit var imagesPath: ArrayList<String>
    private lateinit var supportTicket: SupportTicket
    private lateinit var supportChat: SupportChat
    var id: String = "0"
    var status: String = ""
    private lateinit var lytSendMessage: LinearLayout
    lateinit var tvAlert: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_suppport_chat, container, false)
        activity = requireActivity()
        session = Session(activity)
        supportChats = ArrayList()
        if (arguments?.getString("from") != null) {
            supportChat = arguments?.getSerializable("model") as SupportChat
            id = supportChat.ticket_id
            status = ""
        } else {
            supportTicket = arguments?.getSerializable("model") as SupportTicket
            id = supportTicket.id
            status = supportTicket.status
        }
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setHasOptionsMenu(true)
        imgSelectImages = root.findViewById(R.id.imgSelectImages)
        lytSendMessage = root.findViewById(R.id.lytSendMessage)
        tvAlert = root.findViewById(R.id.tvAlert)
        edtMessage = root.findViewById(R.id.edtMessage)
        imgSendMessage = root.findViewById(R.id.imgSendMessage)
        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerViewImages = root.findViewById(R.id.recyclerViewImages)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        recyclerViewImages.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        imagesPath = ArrayList()
        imgSendMessage.setOnClickListener { sendMessage() }
        if (status.equals("closed", ignoreCase = true) || status.equals(
                "resolved",
                ignoreCase = true
            )
        ) {
            lytSendMessage.visibility = View.GONE
            tvAlert.visibility = View.VISIBLE
            tvAlert.text =
                activity.getString(R.string.ticket_is) + status.lowercase(Locale.getDefault())
        } else {
            lytSendMessage.visibility = View.VISIBLE
            tvAlert.visibility = View.GONE
        }
        if (isConnected(activity)) {
            getMessages()
        }
        imgSelectImages.setOnClickListener {
            if (!permissionIfNeeded()) {
                val intent = Intent(activity, Gallery::class.java)
                // Set the title
                intent.putExtra("title", getString(R.string.select_media))
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode", 2)
                intent.putExtra("maxSelection", 10) // Optional
                intent.putExtra("tabBarHidden", true) //Optional - default value is false
                startActivityForResult(intent, openMediaPicker)
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val lastVisiblePosition =
                    (Objects.requireNonNull(recyclerView.layoutManager) as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition != 0) if (offset != 0 && supportChatAdapter.itemCount != 0) getMessages()
            }
        })
        recyclerView.setOnScrollChangeListener { _: View, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY == 0) {
                if (supportChats.size > total && offset != 0) getMessages()
            }
        }
        return root
    }

    private fun permissionIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    permissionReadExternalStorage
                )
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == openMediaPicker) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")!!
                for (path in selectionResult) {
                    imagesPath.add(path)

                }
                recyclerViewImages.visibility = View.VISIBLE
            }
        }
    }


    fun stopShimmer() {
        recyclerView.visibility = View.VISIBLE
        mShimmerViewContainer.visibility = View.GONE
        mShimmerViewContainer.stopShimmer()
    }

    fun startShimmer() {
        recyclerView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getMessages() {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_MESSAGES] = Constant.GetVal
        params[Constant.TICKET_ID] = id
        params[Constant.OFFSET] = "" + offset
        params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 10)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            total = jsonObject.getString(Constant.TOTAL).toInt()
                            val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject1 = jsonArray.getJSONObject(i)
                                if (jsonObject1 != null) {
                                    val supportTicket = Gson().fromJson(
                                        jsonObject1.toString(),
                                        SupportChat::class.java
                                    )
                                    supportChats.add(supportTicket)
                                } else {
                                    break
                                }
                            }
                            if (offset == 0) {
                                supportChatAdapter = SupportChatAdapter(
                                    activity, supportChats
                                )
                                recyclerView.adapter = supportChatAdapter
                            }
                            supportChatAdapter.notifyDataSetChanged()
                            offset += Constant.LOAD_ITEM_LIMIT + 10
                        }
                    } catch (e: JSONException) {
                        stopShimmer()
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.GET_SUPPORT_TICKET_URL, params, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sendMessage() {
        try {
            val message = edtMessage.text.toString()
            if (message.isEmpty() && imagesPath.size == 0) {
                edtMessage.error = activity.getString(R.string.enter_valid_message)
            } else {
                val policy = ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val client = OkHttpClient().newBuilder().build()
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal)
                builder.addFormDataPart(Constant.SEND_MESSAGE, Constant.GetVal)
                builder.addFormDataPart(Constant.TYPE, Constant.USER)
                builder.addFormDataPart(Constant.USER_ID, session.getData(Constant.ID))
                builder.addFormDataPart(Constant.TICKET_ID, id)
                builder.addFormDataPart(Constant.MESSAGE, message)
                for (i in imagesPath.indices) {
                    val file = File(imagesPath[i])
                    builder.addFormDataPart(
                        Constant.ATTACHMENTS,
                        file.name,
                        RequestBody.create(MediaType.parse("application/octet-stream"), file)
                    )
                }
                val body: RequestBody = builder.build()
                val request = Request.Builder()
                    .url(Constant.GET_SUPPORT_TICKET_URL)
                    .method("POST", body)
                    .addHeader(
                        Constant.AUTHORIZATION,
                        "Bearer " + createJWT("eKart", "eKart Authentication")
                    )
                    .build()
                val response = client.newCall(request).execute()
                val jsonObject = JSONObject(response.peekBody(Long.MAX_VALUE).string())
                if (!jsonObject.getBoolean(Constant.ERROR)) {
                    val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject1 = jsonArray.getJSONObject(i)
                        if (jsonObject1 != null) {
                            supportChatAdapter.addMessage(
                                Gson().fromJson(
                                    jsonObject1.toString(),
                                    SupportChat::class.java
                                )
                            )
                        } else {
                            break
                        }
                    }
                    recyclerViewImages.adapter = null
                    imagesPath = ArrayList()
                    recyclerViewImages.visibility = View.GONE
                    edtMessage.text.clear()
                    recyclerView.smoothScrollToPosition(0)
                    activity.invalidateOptionsMenu()
                } else {
                    Toast.makeText(
                        activity,
                        jsonObject.getString(Constant.MESSAGE),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.support)
        activity.invalidateOptionsMenu()
        setCount(Constant.UNREAD_NOTIFICATION_COUNT, 0, activity)
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

    override fun onDetach() {
        super.onDetach()
        supportChatAdapter.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.toolbar_layout).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    companion object {
        lateinit var recyclerView: RecyclerView
        lateinit var supportChatAdapter: SupportChatAdapter
    }
}