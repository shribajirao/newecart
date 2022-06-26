package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.fragment.OrderDetailFragment
import wrteam.ecart.shop.helper.ApiConfig
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.OrderList


class OrderListAdapter(
    val context: Context,
    val activity: Activity,
    val orderListArrayList: ArrayList<OrderList?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_trackorder, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {

            val order = orderListArrayList[position]!!
            try {
                holderParent.tvOrderID.text =
                    activity.getString(R.string.order_number) + order.id
                val date = order.date_added.split("\\s+".toRegex()).toTypedArray()
                holderParent.tvOrderDate.text =
                    activity.getString(R.string.ordered_on) + date[0]
                holderParent.txtOrderAmount.text =
                    Session(context).getData(Constant.currency) + ApiConfig.stringFormat(
                        order.final_total
                    )
                holderParent.lytMain.setOnClickListener {
                    val fragment: Fragment = OrderDetailFragment()
                    val bundle = Bundle()
                    bundle.putString("id", "")
                    bundle.putSerializable("model", order)
                    fragment.arguments = bundle
                    MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                        .addToBackStack(null).commit()
                }
                holderParent.tvStatus.text = ApiConfig.toTitleCase(order.active_status)
                if (order.local_pickup == "1") {
                    holderParent.tvOrderType.text =
                        activity.getString(R.string.pickup_from_store)
                } else {
                    holderParent.tvOrderType.text =
                        activity.getString(R.string.door_step_delivery)
                }
                when {
                    order.active_status == Constant.RECEIVED -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.received_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.received_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.received)
                    }
                    order.active_status == Constant.PROCESSED -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.processed_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.processed_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.processed)
                    }
                    order.active_status == Constant.SHIPPED -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.shipped_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.shipped_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.shipped1)
                    }
                    order.active_status == Constant.DELIVERED -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.delivered_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.delivered_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.delivered1)
                    }
                    order.active_status == Constant.CANCELLED -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.returned_and_cancel_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.returned_and_cancel_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.cancelled1)
                    }
                    order.active_status == Constant.RETURNED -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.returned_and_cancel_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.returned_and_cancel_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.returned)
                    }
                    order.active_status.equals(
                        Constant.AWAITING_PAYMENT,
                        ignoreCase = true
                    ) -> {
                        holderParent.cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                activity, R.color.awaiting_status_bg
                            )
                        )
                        holderParent.tvStatus.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.awaiting_status_txt
                            )
                        )
                        holderParent.tvStatus.setText(R.string.awaiting_payment_)
                    }
                }
                val items = ArrayList<String>()
                for (i in order.items.indices) {
                    items.add(order.items[i].product_name)
                }
                holderParent.tvItems.text =
                    items.toTypedArray().contentToString().replace("]", "").replace("[", "")
                holderParent.tvTotalItems.text =
                    if (items.size > 1) items.size.toString() + activity.getString(R.string.items) else items.size.toString() + activity.getString(
                        R.string.item
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return orderListArrayList.size
    }

    override fun getItemId(position: Int): Long {
        return orderListArrayList[position].toString().toLong()
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderID: TextView = itemView.findViewById(R.id.tvOrderID)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val lytMain: RelativeLayout = itemView.findViewById(R.id.lytMain)
        val txtOrderAmount: TextView = itemView.findViewById(R.id.txtOrderAmount)
        val tvTotalItems: TextView = itemView.findViewById(R.id.tvTotalItems)
        val tvItems: TextView = itemView.findViewById(R.id.tvItems)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvOrderType: TextView = itemView.findViewById(R.id.tvOrderType)
        val cardView: CardView = itemView.findViewById(R.id.cardView)

    }
}