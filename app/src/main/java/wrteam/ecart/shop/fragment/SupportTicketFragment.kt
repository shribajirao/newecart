package wrteam.ecart.shop.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.SelectedImagesAdapter
import wrteam.ecart.shop.adapter.SupportChatImageAdapter
import wrteam.ecart.shop.adapter.SupportTicketListAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.createJWT
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Session.Companion.setCount
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.SupportTicket
import wrteam.ecart.shop.model.SupportTicketType
import java.io.File

class SupportTicketFragment : Fragment() {
    private val openMediaPicker = 1  // Request code
    private val permissionReadExternalStorage = 100       // Request code for read external storage
    lateinit var root: View
    lateinit var recyclerView: RecyclerView
    lateinit var recyclerViewImages: RecyclerView
    lateinit var supportTickets: ArrayList<SupportTicket?>
    lateinit var supportTicketAdapter: SupportTicketListAdapter
    lateinit var supportTicketTypesAdapter: SupportTicketTypesAdapter
    lateinit var swipeLayout: SwipeRefreshLayout
    lateinit var scrollView: NestedScrollView
    lateinit var tvAlert: TextView
    var total = 0
    lateinit var activity: Activity
    var offset = 0
    var offsetTicketType = 0
    lateinit var session: Session
    var isLoadMore = false
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    lateinit var lytMainCreateTicket: RelativeLayout
    lateinit var lytCreateTicket: LinearLayout
    lateinit var lytImages: LinearLayout
    lateinit var animShow: Animation
    private lateinit var animHide: Animation
    lateinit var fabCreateTicket: FloatingActionButton
    lateinit var btnSelectImages: TextView
    lateinit var edtCreateTicketSubject: EditText
    lateinit var edtCreateTicketDescription: EditText
    private lateinit var imgTicketClose: ImageView
    private lateinit var imagesPath: ArrayList<String>
    lateinit var btnCreateTicket: Button
    private lateinit var supportTypeArrayList: ArrayList<SupportTicketType?>
    lateinit var progressBar: ProgressBar
    lateinit var spinnerStatus: Spinner
    lateinit var lytStatus: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_suppport, container, false)
        activity = requireActivity()
        session = Session(activity)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setHasOptionsMenu(true)
        animShow = AnimationUtils.loadAnimation(activity, R.anim.view_show)
        animHide = AnimationUtils.loadAnimation(activity, R.anim.view_hide)
        edtCreateTicketSubject = root.findViewById(R.id.edtCreateTicketSubject)
        edtCreateTicketDescription = root.findViewById(R.id.edtCreateTicketDescription)
        btnSelectImages = root.findViewById(R.id.btnSelectImages)
        imgTicketClose = root.findViewById(R.id.imgTicketClose)
        spinnerStatus = root.findViewById(R.id.spinnerStatus)
        lytStatus = root.findViewById(R.id.lytStatus)
        recyclerView = root.findViewById(R.id.recyclerView)
        swipeLayout = root.findViewById(R.id.swipeLayout)
        tvAlert = root.findViewById(R.id.tvAlert)
        scrollView = root.findViewById(R.id.scrollView)
        lytMainCreateTicket = root.findViewById(R.id.lytMainCreateTicket)
        lytCreateTicket = root.findViewById(R.id.lytCreateTicket)
        fabCreateTicket = root.findViewById(R.id.fabCreateTicket)
        recyclerViewImages = root.findViewById(R.id.recyclerViewImages)
        lytImages = root.findViewById(R.id.lytImages)
        btnCreateTicket = root.findViewById(R.id.btnCreateTicket)
        progressBar = root.findViewById(R.id.progressBar)
        tvTicketType = root.findViewById(R.id.tvTicketType)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerViewImages.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        imagesPath = ArrayList()
        if (isConnected(activity)) {
            getSupportTickets()
        }
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary))
        swipeLayout.setOnRefreshListener {
            supportTickets.clear()
            offset = 0
            getSupportTickets()
            swipeLayout.isRefreshing = false
        }
        btnCreateTicket.setOnClickListener {
            selectedSupportTicket = SupportTicket()
            createTicket() }
        tvTicketType.setOnClickListener { openDialog(activity) }
        fabCreateTicket.setOnClickListener {
            from = "add"
            selectedSupportTicket = SupportTicket()
            edtCreateTicketSubject.setText("")
            edtCreateTicketDescription.setText("")
            tvTicketType.text = ""
            ticketTypeId = "0"
            showCreateTickerDialog(animShow, fabCreateTicket, lytMainCreateTicket, lytCreateTicket)
        }

        imgTicketClose.setOnClickListener { hideCreateTickerDialog() }

        btnSelectImages.setOnClickListener {
            lytImages.visibility = View.VISIBLE
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

        animShow.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (from == "edit") {
                    lytStatus.visibility = View.VISIBLE
                    btnSelectImages.visibility = View.GONE
                    edtCreateTicketSubject.setText(selectedSupportTicket.title)
                    edtCreateTicketDescription.setText(selectedSupportTicket.message)
                    ticketTypeId = selectedSupportTicket.type_id
                    tvTicketType.text = selectedSupportTicket.type
                    btnCreateTicket.text = activity.getString(R.string.update)

                    spinnerStatus.setSelection(
                        if (selectedSupportTicket.status.equals(
                                "resolved",
                                ignoreCase = true
                            )
                        ) 0 else 1
                    )

                    if (selectedSupportTicket.image.size != 0) {
                        lytImages.visibility = View.VISIBLE
                        recyclerViewImages.visibility = View.VISIBLE
                        recyclerViewImages.adapter = SupportChatImageAdapter(
                            activity,
                            selectedSupportTicket.image
                        )
                    }

                } else {
                    recyclerViewImages.adapter = null
                    btnSelectImages.visibility = View.VISIBLE
                    lytStatus.visibility = View.GONE
                }
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        return root
    }

    private fun createTicket() {
        try {
            val ticketSubject = edtCreateTicketSubject.text.toString()
            val ticketDescription = edtCreateTicketDescription.text.toString()
            if (ticketSubject.isEmpty()) {
                edtCreateTicketSubject.error = activity.getString(R.string.enter_valid_subject)
            } else if (ticketDescription.isEmpty()) {
                edtCreateTicketDescription.error =
                    activity.getString(R.string.enter_valid_description)
            } else if (ticketTypeId == "0") {
                tvTicketType.error = activity.getString(R.string.select_ticket_type)
            } else {
                val policy = ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val client = OkHttpClient().newBuilder().build()
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal)
                if (from == "add") {
                    builder.addFormDataPart(Constant.ADD_TICKET, Constant.GetVal)
                    for (i in imagesPath.indices) {
                        val file = File(imagesPath[i])
                        builder.addFormDataPart(
                            Constant.IMAGES,
                            file.name,
                            RequestBody.create(MediaType.parse("application/octet-stream"), file)
                        )
                    }
                } else {
                    builder.addFormDataPart(Constant.EDIT_TICKET, Constant.GetVal)
                    builder.addFormDataPart(Constant.TICKET_ID, selectedSupportTicket.id)
                    builder.addFormDataPart(
                        Constant.STATUS,
                        if (spinnerStatus.selectedItemPosition == 0) "3" else "5"
                    )
                }
                builder.addFormDataPart(Constant.TICKET_TYPE_ID, ticketTypeId)
                builder.addFormDataPart(Constant.USER_ID, session.getData(Constant.ID))
                builder.addFormDataPart(Constant.EMAIL, session.getData(Constant.EMAIL))
                builder.addFormDataPart(Constant.TITLE, ticketSubject)
                builder.addFormDataPart(Constant.MESSAGE, ticketDescription)
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
                    val supportTicket = Gson().fromJson(
                        jsonObject.getJSONArray(Constant.DATA).getJSONObject(0).toString(),
                        SupportTicket::class.java
                    )
                    if (selectedSupportTicket == SupportTicket()) {
                        val fragment: Fragment = SupportChatFragment()
                        val bundle = Bundle()
                        bundle.putSerializable("model", supportTicket)
                        fragment.arguments = bundle
                        MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                            .addToBackStack(null).commit()
                    } else {
                        supportTicketAdapter.setItem(selectedSupportTicketPosition, supportTicket)
                    }
                    hideCreateTickerDialog()
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

    @SuppressLint("ClickableViewAccessibility")
    fun openDialog(activity: Activity) {
        offsetTicketType = 0
        val alertDialog = AlertDialog.Builder(requireContext())
        val inflater1 =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater1.inflate(R.layout.dialog_city_area_selection, null)
        alertDialog.setView(dialogView)
        alertDialog.setCancelable(true)
        val dialog = alertDialog.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val scrollView: NestedScrollView = dialogView.findViewById(R.id.scrollView)
        val tvSearch: TextView = dialogView.findViewById(R.id.tvSearch)
        val tvAlert: TextView = dialogView.findViewById(R.id.tvAlert)
        val searchView: EditText = dialogView.findViewById(R.id.searchView)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
        val shimmerFrameLayout: ShimmerFrameLayout =
            dialogView.findViewById(R.id.shimmerFrameLayout)
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()
        searchView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_search,
            0,
            R.drawable.ic_close_,
            0
        )
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (searchView.text.toString().isNotEmpty()) {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_search,
                        0,
                        R.drawable.ic_close,
                        0
                    )
                } else {
                    searchView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_search,
                        0,
                        R.drawable.ic_close_,
                        0
                    )
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        tvAlert.text = getString(R.string.no_types_found)
        getTicketTypes(
            "",
            recyclerView,
            tvAlert,
            linearLayoutManager,
            scrollView,
            dialog,
            shimmerFrameLayout
        )
        tvSearch.setOnClickListener {
            getTicketTypes(
                searchView.text.toString(),
                recyclerView,
                tvAlert,
                linearLayoutManager,
                scrollView,
                dialog,
                shimmerFrameLayout
            )
        }
        searchView.setOnTouchListener { _: View, event: MotionEvent ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (searchView.text.toString().isNotEmpty()) {
                    if (event.rawX >= searchView.right - searchView.compoundDrawables[drawableRight].bounds.width()) {
                        searchView.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_search,
                            0,
                            R.drawable.ic_close_,
                            0
                        )
                        searchView.setText("")
                        getTicketTypes(
                            "",
                            recyclerView,
                            tvAlert,
                            linearLayoutManager,
                            scrollView,
                            dialog,
                            shimmerFrameLayout
                        )
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getTicketTypes(
        search: String,
        recyclerView: RecyclerView,
        tvAlert: TextView,
        linearLayoutManager: LinearLayoutManager,
        scrollView: NestedScrollView,
        dialog: AlertDialog,
        shimmerFrameLayout: ShimmerFrameLayout
    ) {
        supportTypeArrayList = ArrayList()
        progressBar.visibility = View.VISIBLE
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_TICKET_TYPE] = Constant.GetVal
        params[Constant.SEARCH] = search
        params[Constant.OFFSET] = "" + offsetTicketType
        params[Constant.LIMIT] = "" + (Constant.LOAD_ITEM_LIMIT + 20)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            try {
                                total = jsonObject.getString(Constant.TOTAL).toInt()
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val supportTicketType = Gson().fromJson(
                                        jsonObject1.toString(),
                                        SupportTicketType::class.java
                                    )
                                    supportTypeArrayList.add(supportTicketType)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (offset == 0) {
                                progressBar.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                tvAlert.visibility = View.GONE
                                supportTicketTypesAdapter =
                                    SupportTicketTypesAdapter(
                                        activity,
                                        supportTypeArrayList,
                                        dialog,
                                    )
                                supportTicketTypesAdapter.setHasStableIds(true)
                                recyclerView.adapter = supportTicketTypesAdapter
                                shimmerFrameLayout.visibility = View.GONE
                                shimmerFrameLayout.stopShimmer()
                                scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        if (supportTypeArrayList.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == supportTypeArrayList.size - 1) {
                                                    //bottom of list!


                                                    offsetTicketType += Constant.LOAD_ITEM_LIMIT + 20
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.GET_TICKET_TYPE] =
                                                        Constant.GetVal
                                                    params1[Constant.SEARCH] = search
                                                    params1[Constant.OFFSET] = "" + offsetTicketType
                                                    params1[Constant.LIMIT] =
                                                        "" + (Constant.LOAD_ITEM_LIMIT + 20)
                                                    requestToVolley(
                                                        object : VolleyCallback {
                                                            override fun onSuccess(
                                                                result: Boolean,
                                                                response: String
                                                            ) {
                                                                if (result) {
                                                                    try {
                                                                        val jsonObject1 =
                                                                            JSONObject(response)
                                                                        if (!jsonObject1.getBoolean(
                                                                                Constant.ERROR
                                                                            )
                                                                        ) {

                                                                            val jsonArray =
                                                                                jsonObject.getJSONArray(
                                                                                    Constant.DATA
                                                                                )
                                                                            for (i in 0 until jsonArray.length()) {
                                                                                val jsonObject2 =
                                                                                    jsonArray.getJSONObject(
                                                                                        i
                                                                                    )
                                                                                val supportTicketType =
                                                                                    Gson().fromJson(
                                                                                        jsonObject2.toString(),
                                                                                        SupportTicketType::class.java
                                                                                    )
                                                                                supportTypeArrayList.add(
                                                                                    supportTicketType
                                                                                )
                                                                            }
                                                                            supportTicketTypesAdapter.notifyDataSetChanged()
                                                                            supportTicketTypesAdapter.setLoaded()
                                                                            isLoadMore = false
                                                                        }
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                        e.printStackTrace()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        activity,
                                                        Constant.GET_SUPPORT_TICKET_URL,
                                                        params1,
                                                        false
                                                    )
                                                }
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            shimmerFrameLayout.visibility = View.GONE
                            shimmerFrameLayout.stopShimmer()
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        shimmerFrameLayout.visibility = View.GONE
                        shimmerFrameLayout.stopShimmer()
                        progressBar.visibility = View.GONE
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.GET_SUPPORT_TICKET_URL, params, false)
    }

    class SupportTicketTypesAdapter(
        val activity: Activity,
        private val supportTicketTypes: ArrayList<SupportTicketType?>,
        val dialog: AlertDialog,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // for load more
        val viewTypeItem = 0
        val viewTypeLoading = 1
        private var isLoading = false
        val session: Session = Session(activity)
        fun add(position: Int, supportTicketType: SupportTicketType) {
            supportTicketTypes.add(position, supportTicketType)
            notifyItemInserted(position)
        }

        fun setLoaded() {
            isLoading = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View
            return when (viewType) {
                viewTypeItem -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.lyt_city_area_list, parent, false)
                    ItemHolder(
                        view
                    )
                }
                viewTypeLoading -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_progressbar, parent, false)
                    ViewHolderLoading(
                        view
                    )
                }
                else -> throw IllegalArgumentException("unexpected viewType: $viewType")
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
            if (holderParent is ItemHolder) {
                try {
                    val supportTicketType = supportTicketTypes[position]!!
                    holderParent.tvPinCode.text = supportTicketType.type
                    holderParent.tvPinCode.setOnClickListener {
                        tvTicketType.text = supportTicketType.type
                        ticketTypeId = supportTicketType.id
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (holderParent is ViewHolderLoading) {
                holderParent.progressBar.isIndeterminate = true
            }
        }

        override fun getItemCount(): Int {
            return supportTicketTypes.size
        }

        override fun getItemViewType(position: Int): Int {
            return viewTypeItem
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
            val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

        }

        internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvPinCode: TextView = itemView.findViewById(R.id.tvPinCode)

        }

    }

    private fun permissionIfNeeded(): Boolean {
        if (ContextCompat.checkSelfPermission(
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
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectionResult = data.getStringArrayListExtra("result")
            for (path in selectionResult!!) {
                imagesPath.add(path)
            }
            recyclerViewImages.adapter = SelectedImagesAdapter(activity, imagesPath)
        }
    }

    private fun hideCreateTickerDialog() {
        lytCreateTicket.visibility = View.GONE
        lytCreateTicket.startAnimation(animHide)
        fabCreateTicket.visibility = View.VISIBLE
        animHide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                lytMainCreateTicket.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    fun stopShimmer() {
        scrollView.visibility = View.VISIBLE
        mShimmerViewContainer.visibility = View.GONE
        mShimmerViewContainer.stopShimmer()
    }

    fun startShimmer() {
        scrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getSupportTickets() {
        startShimmer()
        supportTickets = ArrayList()
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_TICKETS] = Constant.GetVal
        params[Constant.USER_ID] = session.getData(Constant.ID)
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
                                        SupportTicket::class.java
                                    )
                                    supportTickets.add(supportTicket)
                                } else {
                                    break
                                }
                            }
                            if (offset == 0) {
                                supportTicketAdapter = SupportTicketListAdapter(
                                    activity,
                                    supportTickets,
                                    animShow,
                                    fabCreateTicket,
                                    lytMainCreateTicket,
                                    lytCreateTicket
                                )
                                recyclerView.adapter = supportTicketAdapter
                                stopShimmer()
                                if (arguments?.getString("from") == "customer_notification") {
                                    val fragment: Fragment = SupportChatFragment()
                                    val bundle = Bundle()
                                    bundle.putSerializable(
                                        "model",
                                        arguments?.getSerializable("model")
                                    )
                                    bundle.putSerializable("from", arguments?.getString("from"))
                                    fragment.arguments = bundle
                                    MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                                        .addToBackStack(null).commit()
                                }
                                scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        if (supportTickets.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == supportTickets.size - 1) {
                                                    //bottom of list!
                                                    supportTickets.add(SupportTicket())
                                                    supportTicketAdapter.notifyItemInserted(
                                                        supportTickets.size - 1
                                                    )
                                                    offset += Constant.LOAD_ITEM_LIMIT + 10
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.GET_TICKETS] = Constant.GetVal
                                                    params1[Constant.USER_ID] =
                                                        session.getData(Constant.ID)
                                                    params1[Constant.OFFSET] = "" + offset
                                                    params1[Constant.LIMIT] =
                                                        "" + (Constant.LOAD_ITEM_LIMIT + 10)
                                                    requestToVolley(
                                                        object : VolleyCallback {
                                                            override fun onSuccess(
                                                                result: Boolean,
                                                                response: String
                                                            ) {
                                                                if (result) {
                                                                    try {
                                                                        val jsonObject12 =
                                                                            JSONObject(response)
                                                                        if (!jsonObject12.getBoolean(
                                                                                Constant.ERROR
                                                                            )
                                                                        ) {
                                                                            session.setData(
                                                                                Constant.TOTAL,
                                                                                jsonObject12.getString(
                                                                                    Constant.TOTAL
                                                                                )
                                                                            )
                                                                            supportTickets.removeAt(
                                                                                supportTickets.size - 1
                                                                            )
                                                                            supportTicketAdapter.notifyItemRemoved(
                                                                                supportTickets.size
                                                                            )
                                                                            val object1 =
                                                                                JSONObject(response)
                                                                            val jsonArray1 =
                                                                                object1.getJSONArray(
                                                                                    Constant.DATA
                                                                                )
                                                                            for (i in 0 until jsonArray1.length()) {
                                                                                val jsonObject1 =
                                                                                    jsonArray1.getJSONObject(
                                                                                        i
                                                                                    )
                                                                                if (jsonObject1 != null) {
                                                                                    val supportTicket =
                                                                                        Gson().fromJson(
                                                                                            jsonObject1.toString(),
                                                                                            SupportTicket::class.java
                                                                                        )
                                                                                    supportTickets.add(
                                                                                        supportTicket
                                                                                    )
                                                                                } else {
                                                                                    break
                                                                                }
                                                                            }
                                                                            supportTicketAdapter.notifyDataSetChanged()
                                                                            isLoadMore = false
                                                                        }
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        activity,
                                                        Constant.GET_SUPPORT_TICKET_URL,
                                                        params1,
                                                        false
                                                    )
                                                }
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            stopShimmer()
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        stopShimmer()
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.GET_SUPPORT_TICKET_URL, params, false)
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.toolbar_layout).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    companion object {
        lateinit var selectedSupportTicket: SupportTicket
        var selectedSupportTicketPosition = 0
        var ticketTypeId = "0"
        var from = ""
        @SuppressLint("StaticFieldLeak")
        lateinit var tvTicketType: TextView
        fun showCreateTickerDialog(
            animShow: Animation,
            fabCreateTicket: FloatingActionButton,
            lytMainCreateTicket: RelativeLayout,
            lytCreateTicket: LinearLayout
        ) {
            fabCreateTicket.visibility = View.GONE
            lytMainCreateTicket.visibility = View.VISIBLE
            lytCreateTicket.visibility = View.VISIBLE
            lytCreateTicket.startAnimation(animShow)
        }
    }
}