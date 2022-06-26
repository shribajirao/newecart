package wrteam.ecart.shop.helper

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.fragment.SupportChatFragment
import wrteam.ecart.shop.model.SupportChat

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private lateinit var supportTicket: SupportChat
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            try {
                val json = JSONObject(remoteMessage.data.toString())
                sendPushNotification(json)
            } catch (e: Exception) {
                Log.e(tag, "Exception: " + e.message)
            }
        }
    }

    private fun sendPushNotification(json: JSONObject) {
        try {
            val data = json.getJSONObject(Constant.DATA)
            val id = data.getString("id")
            val type = data.getString("type")
            val imageUrl =
                when {
                    data.getString("image") == "null" -> ""
                    else -> data.getString(
                        "image"
                    )
                }
            val title = data.getString("title")
            val message = data.getString("message")
            if (type == "customer_notification") {
                val sb = StringBuilder(data.getString("message_res")).deleteCharAt(0).deleteCharAt(
                    data.getString("message_res").length - 2
                )
                supportTicket = Gson().fromJson(sb.toString(), SupportChat::class.java)
                SupportChatFragment.supportChatAdapter.addMessage(supportTicket)
            } else {
                val intent = Intent(applicationContext, MainActivity::class.java)
                when (type) {
                    "category" -> {
                        intent.putExtra("id", id)
                        intent.putExtra("name", title)
                        intent.putExtra(Constant.FROM, type)
                    }
                    "product" -> {
                        intent.putExtra("id", id)
                        intent.putExtra(Constant.VARIANT_POSITION, 0)
                        intent.putExtra(Constant.FROM, type)
                    }
                    "order" -> {
                        intent.putExtra(Constant.FROM, type)
                        intent.putExtra("model", "")
                        intent.putExtra("id", id)
                    }
                    "customer_notification" -> {
                        if (data.has("message_res") && data.getString("message_res").isNotEmpty()) {
                            val sb = StringBuilder(data.getString("message_res")).deleteCharAt(0)
                                .deleteCharAt(
                                    data.getString("message_res").length - 2
                                )
                            supportTicket = Gson().fromJson(sb.toString(), SupportChat::class.java)
                            intent.putExtra("model", supportTicket)
                        } else {
                            intent.putExtra("model", "")
                        }
                        intent.putExtra(Constant.FROM, type)
                        intent.putExtra("id", id)
                    }
                    else -> intent.putExtra(Constant.FROM, "")
                }
                val mNotificationManager = MyNotificationManager(applicationContext)
                if (imageUrl == "") {
                    mNotificationManager.showSmallNotification(title, message, intent)
                } else {
                    mNotificationManager.showBigNotification(title, message, imageUrl, intent)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Exception: " + e.message)
        }
    }

    companion object {
        const val tag = "MyFirebaseMsgService"
    }

    override fun onNewToken(s: String) {
        Session(applicationContext).setData(Constant.FCM_ID, s)
    }
}