package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.PaymentActivity
import wrteam.ecart.shop.helper.ApiConfig.Companion.getMonth
import wrteam.ecart.shop.model.Slot
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SlotAdapter(val activity: Activity, private val slotList: ArrayList<Slot>) :
    RecyclerView.Adapter<SlotAdapter.ViewHolder>() {
    private var selectedPosition = 0
    private var isToday = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.lyt_time_slot, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = slotList[position]
        holder.rdBtn.text = model.title
        holder.rdBtn.tag = position
        holder.rdBtn.isChecked = position == selectedPosition
        val pattern = "HH:mm:ss"
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(pattern)
        val now = sdf.format(Date())
        lateinit var currentTime: Date
        lateinit var slotTime: Date
        try {
            currentTime = sdf.parse(now)
            slotTime = sdf.parse(model.lastOrderTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val calendar = Calendar.getInstance()
        isToday =
            PaymentActivity.deliveryDay == calendar[Calendar.DATE].toString() + "-" + getMonth(
                activity, calendar[Calendar.MONTH] + 1
            ) + "-" + calendar[Calendar.YEAR]
        if (isToday) {
            if (currentTime > slotTime) {
                holder.rdBtn.isChecked = false
                holder.rdBtn.isClickable = false
                holder.rdBtn.setTextColor(ContextCompat.getColor(activity, R.color.gray))
                holder.rdBtn.buttonDrawable = ContextCompat.getDrawable(
                    activity, R.drawable.ic_uncheck_circle
                )
            } else {
                holder.rdBtn.isClickable = true
                holder.rdBtn.setTextColor(ContextCompat.getColor(activity, R.color.black))
                holder.rdBtn.buttonDrawable = ContextCompat.getDrawable(
                    activity, R.drawable.ic_active_circle
                )
            }
        } else {
            holder.rdBtn.isClickable = true
            holder.rdBtn.setTextColor(ContextCompat.getColor(activity, R.color.black))
            holder.rdBtn.buttonDrawable = ContextCompat.getDrawable(
                activity, R.drawable.ic_active_circle
            )
        }
        val finalCurrentTime = currentTime
        val finalSlotTime = slotTime
        holder.rdBtn.setOnClickListener { v ->
            if (isToday) {
                if (finalCurrentTime < finalSlotTime) {
                    PaymentActivity.deliveryTime = model.title
                    selectedPosition = v.tag as Int
                    notifyDataSetChanged()
                }
            } else {
                PaymentActivity.deliveryTime = model.title
                selectedPosition = v.tag as Int
                notifyDataSetChanged()
            }
        }
        if (holder.rdBtn.isChecked) {
            holder.rdBtn.buttonDrawable =
                ContextCompat.getDrawable(activity, R.drawable.ic_radio_button_checked)
            holder.rdBtn.setTextColor(ContextCompat.getColor(activity, R.color.black))
            PaymentActivity.deliveryTime = model.title
        }
    }

    override fun getItemCount(): Int {
        return slotList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rdBtn: RadioButton = itemView.findViewById(R.id.rdBtn)

    }

    init {
        PaymentActivity.deliveryTime = ""
    }
}