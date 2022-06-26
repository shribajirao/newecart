package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.fragment.SupportChatFragment
import wrteam.ecart.shop.fragment.SupportTicketFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.toTitleCase
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.SupportTicket

class SupportTicketListAdapter(
    val activity: Activity,
    private val supportTickets: ArrayList<SupportTicket?>,
    private var animShow: Animation,
    private var fabCreateTicket: FloatingActionButton,
    private var lytMainCreateTicket: RelativeLayout,
    private var lytCreateTicket: LinearLayout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    var session: Session = Session(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_support_ticket_list, parent, false)
        )

    }

    fun setItem(position: Int, supportTicket: SupportTicket) {
        supportTickets[position] = supportTicket
        notifyItemChanged(position)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val supportTicket = supportTickets[position]!!
            holderParent.tvTicketId.text = "#" + supportTicket.id
            holderParent.tvTitle.text = Html.fromHtml(supportTicket.title, 0)
            holderParent.tvMessage.text = Html.fromHtml(supportTicket.message, 0)
            val status = supportTicket.status
            when (status) {
                "pending" -> holderParent.cardViewStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.support_ticket_pending
                    )
                )
                "opened" -> holderParent.cardViewStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.support_ticket_opened
                    )
                )
                "resolved" -> holderParent.cardViewStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.support_ticket_resolved
                    )
                )
                "closed" -> holderParent.cardViewStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.support_ticket_closed
                    )
                )
                "reopen" -> holderParent.cardViewStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.support_ticket_reopen
                    )
                )
            }
            holderParent.tvStatus.text = toTitleCase(status)
            holderParent.lytMain.setOnClickListener {
                val fragment: Fragment = SupportChatFragment()
                val bundle = Bundle()
                bundle.putSerializable("model", supportTicket)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            holderParent.imageEdit.setOnClickListener {
                SupportTicketFragment.from = "edit"
                SupportTicketFragment.selectedSupportTicket = supportTicket
                SupportTicketFragment.showCreateTickerDialog(
                    animShow,
                    fabCreateTicket,
                    lytMainCreateTicket,
                    lytCreateTicket
                )
            }
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return supportTickets.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val product = supportTickets[position]!!
        return product.id.toInt().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)
    }

    internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTicketId: TextView = itemView.findViewById(R.id.tvTicketId)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        var lytMain: RelativeLayout = itemView.findViewById(R.id.lytMain)
        var cardViewStatus: CardView = itemView.findViewById(R.id.cardViewStatus)
        var imageEdit: ImageView = itemView.findViewById(R.id.imageEdit)

    }

}