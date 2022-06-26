package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.model.SupportChat

class SupportChatAdapter(val activity: Activity, private val supportChats: ArrayList<SupportChat>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    fun addMessage(supportChat: SupportChat) {
        supportChats.add(0, supportChat)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(LayoutInflater.from(activity).inflate(R.layout.lyt_support_chat_list, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val supportChat = supportChats[position]
            if (supportChat.type == "user") {
                holderParent.lytSend.visibility = View.VISIBLE
                holderParent.tvSendMessage.text = supportChat.message
                holderParent.tvSendTime.text = supportChat.date_created
                if (supportChat.attachments.size != 0) {
                    holderParent.recyclerViewSendImages.layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    holderParent.recyclerViewSendImages.adapter =
                        SupportChatImageAdapter(activity, supportChat.attachments)
                }
            } else {
                holderParent.lytReceive.visibility = View.VISIBLE
                holderParent.tvReceiveMessage.text = supportChat.message
                holderParent.tvReceiveTime.text = supportChat.date_created
                if (supportChat.attachments.size != 0) {
                    holderParent.recyclerViewReceiveImages.layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    holderParent.recyclerViewReceiveImages.adapter =
                        SupportChatImageAdapter(activity, supportChat.attachments)
                }
            }
            holderParent.setIsRecyclable(false)
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return supportChats.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        return supportChats[position].toString().toInt().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReceiveMessage: TextView = itemView.findViewById(R.id.tvReceiveMessage)
        val tvSendMessage: TextView = itemView.findViewById(R.id.tvSendMessage)
        val tvReceiveTime: TextView = itemView.findViewById(R.id.tvReceiveTime)
        val tvSendTime: TextView = itemView.findViewById(R.id.tvSendTime)
        var lytReceive: LinearLayout = itemView.findViewById(R.id.lytReceive)
        var lytSend: LinearLayout = itemView.findViewById(R.id.lytSend)
        var recyclerViewReceiveImages: RecyclerView = itemView.findViewById(R.id.recyclerViewReceiveImages)
        var recyclerViewSendImages: RecyclerView = itemView.findViewById(R.id.recyclerViewSendImages)

    }
}