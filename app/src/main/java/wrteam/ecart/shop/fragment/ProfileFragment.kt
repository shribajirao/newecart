package wrteam.ecart.shop.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.coursion.freakycoder.mediapicker.galleries.Gallery
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.ApiConfig.Companion.checkValidation
import wrteam.ecart.shop.helper.ApiConfig.Companion.createJWT
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setHideShowPassword
import wrteam.ecart.shop.helper.VolleyCallback
import java.io.File


class ProfileFragment : Fragment() {
    private val openMediaPicker = 1  // Request code
    private val permissionReadExternalStorage = 100       // Request code for read external storage
    private lateinit var imgProfile: ImageView
    private lateinit var fabProfile: FloatingActionButton
    lateinit var progressBar: ProgressBar
    lateinit var root: View
    private lateinit var tvChangePassword: TextView
    lateinit var session: Session
    private lateinit var btnSubmit: Button
    lateinit var activity: Activity
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtMobile: EditText
    private var imagePath = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_profile, container, false)
        activity = requireActivity()
        edtName = root.findViewById(R.id.edtName)
        edtEmail = root.findViewById(R.id.edtEmail)
        edtMobile = root.findViewById(R.id.edtMobile)
        btnSubmit = root.findViewById(R.id.btnSubmit)
        tvChangePassword = root.findViewById(R.id.tvChangePassword)
        fabProfile = root.findViewById(R.id.fabProfile)
        progressBar = root.findViewById(R.id.progressBar)
        setHasOptionsMenu(true)
        session = Session(activity)
        imgProfile = root.findViewById(R.id.imgProfile)

        imagePath = session.getData(Constant.PROFILE)

        Picasso.get()
            .load(session.getData(Constant.PROFILE))
            .fit()
            .centerInside()
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder) //
            .into(imgProfile)

        fabProfile.setOnClickListener {
            if (!permissionIfNeeded()) {
                val intent = Intent(activity, Gallery::class.java)
                // Set the title
                intent.putExtra("title", activity.getString(R.string.select_images))
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode", 2)
                intent.putExtra("maxSelection", 1) // Optional
                intent.putExtra("tabBarHidden", true) //Optional - default value is false
                startActivityForResult(intent, openMediaPicker)
            }
        }

        tvChangePassword.setOnClickListener {
            openBottomDialog(
                activity
            )
        }

        btnSubmit.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val mobile = edtMobile.text.toString()
            when {
                checkValidation(name, isMailValidation = false, isMobileValidation = false) -> {
                    edtName.requestFocus()
                    edtName.error = getString(R.string.enter_name)
                }
                checkValidation(email, isMailValidation = false, isMobileValidation = false) -> {
                    edtEmail.requestFocus()
                    edtEmail.error = getString(R.string.enter_email)
                }
                checkValidation(email, isMailValidation = true, isMobileValidation = false) -> {
                    edtEmail.requestFocus()
                    edtEmail.error = getString(R.string.enter_valid_email)
                }
                isConnected(activity) -> {
                    val params: MutableMap<String, String> = HashMap()
                    params[Constant.TYPE] = Constant.EDIT_PROFILE
                    params[Constant.ID] = session.getData(Constant.ID)
                    params[Constant.NAME] = name
                    params[Constant.EMAIL] = email
                    params[Constant.MOBILE] = mobile
                    params[Constant.LONGITUDE] = session.getCoordinates(Constant.LONGITUDE)
                    params[Constant.LATITUDE] = session.getCoordinates(Constant.LATITUDE)
                    params[Constant.FCM_ID] = session.getData(Constant.FCM_ID)
                    //System.out.println("====update res " + params.toString());
                    requestToVolley(object : VolleyCallback {
                        override fun onSuccess(result: Boolean, response: String) {
                            //System.out.println ("=================* " + response);
                            if (result) {
                                try {
                                    val jsonObject = JSONObject(response)
                                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                                        session.setData(Constant.NAME, name)
                                        session.setData(Constant.EMAIL, email)
                                        session.setData(Constant.MOBILE, mobile)
                                        DrawerFragment.tvName.text = name
                                    }
                                    Toast.makeText(
                                        activity,
                                        jsonObject.getString(Constant.MESSAGE),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }, activity, Constant.RegisterUrl, params, true)
                }
            }
        }

        edtName.setText(session.getData(Constant.NAME))
        edtEmail.setText(session.getData(Constant.EMAIL))
        edtMobile.setText(session.getData(Constant.MOBILE))
        return root
    }

    private fun permissionIfNeeded(): Boolean {
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
        return false
    }

    private fun openBottomDialog(activity: Activity) {
        try {
            val sheetView = activity.layoutInflater.inflate(
                R.layout.dialog_change_password,
                root as ViewGroup,
                false
            )
            val parentViewGroup = sheetView.parent as ViewGroup
            parentViewGroup.removeAllViews()
            val mBottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetTheme)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.window
                ?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            mBottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val edtOldPassword = sheetView.findViewById<EditText>(R.id.edtOldPassword)
            val edtNewPassword = sheetView.findViewById<EditText>(R.id.edtNewPassword)
            val edtConfirmPassword = sheetView.findViewById<EditText>(R.id.edtConfirmPassword)
            val imgChangePasswordClose =
                sheetView.findViewById<ImageView>(R.id.imgChangePasswordClose)
            val btnChangePassword = sheetView.findViewById<Button>(R.id.btnChangePassword)
            edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pass,
                0,
                R.drawable.ic_show,
                0
            )
            edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pass,
                0,
                R.drawable.ic_show,
                0
            )
            edtConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_pass,
                0,
                R.drawable.ic_show,
                0
            )
            setHideShowPassword(edtOldPassword)
            setHideShowPassword(edtNewPassword)
            setHideShowPassword(edtConfirmPassword)
            mBottomSheetDialog.setCancelable(true)
            imgChangePasswordClose.setOnClickListener { mBottomSheetDialog.dismiss() }
            btnChangePassword.setOnClickListener {
                val oldPassword = edtOldPassword.text.toString()
                val password = edtNewPassword.text.toString()
                val confirmPassword = edtConfirmPassword.text.toString()
                when {
                    password != confirmPassword -> {
                        edtConfirmPassword.requestFocus()
                        edtConfirmPassword.error = activity.getString(R.string.pass_not_match)
                    }
                    checkValidation(
                        oldPassword,
                        isMailValidation = false,
                        isMobileValidation = false
                    ) -> {
                        edtOldPassword.requestFocus()
                        edtOldPassword.error = activity.getString(R.string.enter_old_pass)
                    }
                    checkValidation(
                        password,
                        isMailValidation = false,
                        isMobileValidation = false
                    ) -> {
                        edtNewPassword.requestFocus()
                        edtNewPassword.error = activity.getString(R.string.enter_new_pass)
                    }
                    oldPassword != Session(activity).getData(Constant.PASSWORD) -> {
                        edtOldPassword.requestFocus()
                        edtOldPassword.error = activity.getString(R.string.no_match_old_pass)
                    }
                    isConnected(activity) -> {
                        changePassword(password)
                    }
                }
            }
            mBottomSheetDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changePassword(password: String) {
        val params: MutableMap<String, String> = HashMap()
        params[Constant.TYPE] = Constant.CHANGE_PASSWORD
        params[Constant.PASSWORD] = password
        params[Constant.ID] = session.getData(Constant.ID)
        val alertDialog = AlertDialog.Builder(activity)
        // Setting Dialog Message
        alertDialog.setTitle(getString(R.string.change_pass))
        alertDialog.setMessage(getString(R.string.reset_alert_msg))
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()

        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.yes)) { _: DialogInterface, _: Int ->
            requestToVolley(
                object : VolleyCallback {
                    override fun onSuccess(result: Boolean, response: String) {
                        if (result) {
                            try {
                                val jsonObject = JSONObject(response)
                                if (!jsonObject.getBoolean(Constant.ERROR)) {
                                    session.logoutUser(activity)
                                }
                                Toast.makeText(
                                    activity,
                                    jsonObject.getString(Constant.MESSAGE),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }, activity, Constant.RegisterUrl, params, true
            )
        }
        alertDialog.setNegativeButton(getString(R.string.no)) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.profile)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.toolbar_layout).isVisible = false
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_logout).isVisible = true
        menu.findItem(R.id.toolbar_search).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == openMediaPicker) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")!!
                for (path in selectionResult) {
                    imagePath = path
                }
                updateProfile(activity)
            }
        }
    }


    private fun updateProfile(activity: Activity) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val client = OkHttpClient().newBuilder().build()
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal)
        builder.addFormDataPart(Constant.TYPE, Constant.UPLOAD_PROFILE)
        builder.addFormDataPart(Constant.USER_ID, session.getData(Constant.ID))

        val file = File(imagePath)
        builder.addFormDataPart(
            Constant.PROFILE,
            file.name,
            RequestBody.create(MediaType.parse("application/octet-stream"), file)
        )

        val body: RequestBody = builder.build()

        val request = Request.Builder()
            .url(Constant.RegisterUrl)
            .method("POST", body)
            .addHeader(
                Constant.AUTHORIZATION,
                "Bearer " + createJWT("eKart", "eKart Authentication")
            )
            .build()

        val response = client.newCall(request).execute()
        val jsonObject = JSONObject(response.peekBody(Long.MAX_VALUE).string())

        try {

            if (!jsonObject.getBoolean(Constant.ERROR)) {
                session.setData(
                    Constant.PROFILE,
                    jsonObject.getString(Constant.PROFILE)
                )
                Picasso.get()
                    .load(session.getData(Constant.PROFILE))
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imgProfile)
                Picasso.get()
                    .load(session.getData(Constant.PROFILE))
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(DrawerFragment.imgProfile)
            }
            Toast.makeText(
                activity,
                jsonObject.getString(Constant.MESSAGE),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show()


    }
}