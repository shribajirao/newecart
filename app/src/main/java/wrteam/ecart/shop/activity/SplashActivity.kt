package wrteam.ecart.shop.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session

@SuppressLint("CustomSplashScreen")
class SplashActivity : Activity() {
    lateinit var session: Session
    lateinit var activity: Activity
    private val SPLASH_TIME_OUT = 500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this@SplashActivity
        session = Session(activity)
        session.setBoolean("update_skip", false)
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(activity, R.color.colorPrimary)
        setContentView(R.layout.activity_splash)
        val data = this.intent.data
        if (data != null && data.isHierarchical) {
            when (data.path?.split("/".toRegex())?.toTypedArray()?.get(
                data.path!!.split("/".toRegex())
                    .toTypedArray().size - 2
            )) {
                "product" -> {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(
                        Constant.ID,
                        data.path?.split("/".toRegex())?.toTypedArray()?.get(
                            data.path?.split("/".toRegex())!!
                                .toTypedArray().size - 1
                        )
                    )
                    intent.putExtra(Constant.FROM, "share")
                    intent.putExtra(Constant.VARIANT_POSITION, 0)
                    startActivity(intent)
                    finish()
                }
                "refer" -> if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                    Constant.FRIEND_CODE_VALUE = data.path!!.split("/".toRegex())
                        .toTypedArray()[data.path!!.split("/".toRegex()).toTypedArray().size - 1]
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", Constant.FRIEND_CODE_VALUE)
                    assert(clipboard != null)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        this@SplashActivity,
                        R.string.refer_code_copied,
                        Toast.LENGTH_LONG
                    ).show()
                    val referIntent = Intent(this, LoginActivity::class.java)
                    referIntent.putExtra(Constant.FROM, "refer")
                    startActivity(referIntent)
                    finish()
                } else {
                    Handler().postDelayed({
                        startActivity(
                            Intent(this@SplashActivity, MainActivity::class.java).putExtra(
                                Constant.FROM, ""
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }, SPLASH_TIME_OUT.toLong())
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.msg_refer),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Handler().postDelayed({
                    startActivity(
                        Intent(this@SplashActivity, MainActivity::class.java).putExtra(
                            Constant.FROM, ""
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, SPLASH_TIME_OUT.toLong())
            }
        } else {
            if (!session.getBoolean("is_first_time")) {
                Handler().postDelayed({
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            WelcomeActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, SPLASH_TIME_OUT.toLong())
            } else {
                Handler().postDelayed({
                    startActivity(
                        Intent(this@SplashActivity, MainActivity::class.java).putExtra(
                            Constant.FROM, ""
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, SPLASH_TIME_OUT.toLong())
            }
        }
    }
}