package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.ApiConfig.Companion.toTitleCase
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Transaction

class TransactionAdapter(
    val context: Context,
    val activity: Activity,
    val transactions: ArrayList<Transaction?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_transection_list, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val transaction = transactions[position]!!
            holderParent.tvTxDateAndTime.text = transaction.date_created
            holderParent.tvTxMessage.text =
                activity.getString(R.string.hash) + transaction.order_id + " " + transaction.message
            holderParent.tvTxAmount.text =
                activity.getString(R.string.amount_) + Session(
                    context
                ).getData(Constant.currency) + transaction.amount.toFloat()
            holderParent.tvTxNo.text = activity.getString(R.string.hash) + transaction.txn_id
            holderParent.tvPaymentMethod.text = activity.getString(R.string.via) + transaction.type
            holderParent.tvTxStatus.text = toTitleCase(transaction.status)
            if (transaction.status.equals(
                    Constant.CREDIT,
                    ignoreCase = true
                ) || transaction.status.equals(
                    Constant.SUCCESS, ignoreCase = true
                ) || transaction.status.equals(
                    "capture",
                    ignoreCase = true
                ) || transaction.status.equals(
                    "challenge",
                    ignoreCase = true
                ) || transaction.status.equals("pending", ignoreCase = true)
            ) {
                holderParent.cardViewTxStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.tx_success_bg
                    )
                )
            } else {
                holderParent.cardViewTxStatus.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity, R.color.tx_fail_bg
                    )
                )
            }
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val product = transactions[position]!!
        return product.id.toInt().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTxNo: TextView = itemView.findViewById(R.id.tvTxNo)
        val tvTxDateAndTime: TextView = itemView.findViewById(R.id.tvTxDateAndTime)
        val tvTxMessage: TextView = itemView.findViewById(R.id.tvTxMessage)
        val tvTxAmount: TextView = itemView.findViewById(R.id.tvTxAmount)
        val tvTxStatus: TextView = itemView.findViewById(R.id.tvTxStatus)
        val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        val cardViewTxStatus: CardView = itemView.findViewById(R.id.cardViewTxStatus)

    }
}