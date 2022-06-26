package wrteam.ecart.shop.helper

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity

class Session(context: Context) {
    private val privateMode = 0
    lateinit var pref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var context1: Context
    fun getData(id: String): String {
        return pref.getString(id, "")!!
    }

    fun getCoordinates(id: String): String {
        return pref.getString(id, "0")!!
    }

    fun setData(id: String, `val`: String) {
        editor.putString(id, `val`)
        editor.commit()
    }

    fun setBoolean(id: String, `val`: Boolean) {
        editor.putBoolean(id, `val`)
        editor.commit()
    }

    fun getBoolean(id: String): Boolean {
        return pref.getBoolean(id, false)
    }

    fun createUserLoginSession(
        profile: String,
        fcmId: String,
        id: String,
        name: String,
        email: String,
        mobile: String,
        password: String,
        referCode: String,
        balance: String
    ) {
        editor.putBoolean(Constant.IS_USER_LOGIN, true)
        editor.putString(Constant.FCM_ID, fcmId)
        editor.putString(Constant.ID, id)
        editor.putString(Constant.NAME, name)
        editor.putString(Constant.EMAIL, email)
        editor.putString(Constant.MOBILE, mobile)
        editor.putString(Constant.PASSWORD, password)
        editor.putString(Constant.REFERRAL_CODE, referCode)
        editor.putString(Constant.PROFILE, profile)
        editor.putString(Constant.WALLET_BALANCE, balance)
        editor.commit()
    }

    fun logoutUser(activity: Activity) {
        editor.clear()
        editor.commit()
        Session(context1).setBoolean("is_first_time", true)
        val i = Intent(activity, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra(Constant.FROM, "")
        activity.startActivity(i)
        activity.finish()
    }

    fun logoutUserConfirmation(activity: Activity) {
        val alertDialog = AlertDialog.Builder(
            context1
        )
        alertDialog.setTitle(R.string.logout)
        alertDialog.setMessage(R.string.logout_msg)
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes) { dialog: DialogInterface, which: Int ->
            editor.clear()
            editor.commit()
            Session(context1).setBoolean("is_first_time", true)
            val i = Intent(activity, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.putExtra(Constant.FROM, "")
            activity.startActivity(i)
            activity.finish()
        }
        alertDialog.setNegativeButton(R.string.no) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    companion object {
        const val PREFER_NAME = "eKart"
        @JvmStatic
        fun setCount(id: String, value: Int, context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = sharedPreferences.edit()
            editor.putInt(id, value)
            editor.apply()
        }

        fun getCount(id: String, context: Context): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getInt(id, 0)
        }
    }

    init {
        try {
            context1 = context
            pref = context1.getSharedPreferences(PREFER_NAME, privateMode)
            editor = pref.edit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}