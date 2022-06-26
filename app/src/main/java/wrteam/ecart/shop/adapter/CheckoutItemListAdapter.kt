package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.CheckoutFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.ApiConfig.Companion.toTitleCase
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Cart

/**
 * Created by shree1 on 3/16/2017.
 */
class CheckoutItemListAdapter(activity: Activity, carts: ArrayList<Cart>) :
    RecyclerView.Adapter<CheckoutItemListAdapter.ItemHolder>() {
    private lateinit var carts: ArrayList<Cart>
    lateinit var activity: Activity
    lateinit var session: Session
    override fun getItemCount(): Int {
        return carts.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        try {
            val cart = carts[position]
            val price = if (cart.discounted_price == "0") {
                cart.price.toFloat()
            } else {
                cart.discounted_price.toFloat()
            }
            val taxPercentage = cart.tax_percentage
            if (cart.is_cod_allowed == "0") {
                Constant.isCODAllow = false
            }
            if (cart.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                CheckoutFragment.isSoldOut = true
                holder.tvStatus.visibility = View.VISIBLE
            }
            holder.tvItemName.text =
                cart.name + " (" + cart.measurement + " " + toTitleCase(cart.unit) + ")"
            holder.tvQty.text = activity.getString(R.string.qty_1) + cart.qty
            holder.tvPrice.text = session.getData(Constant.currency) + stringFormat("" + price)
            if (cart.discounted_price == "0" || cart.discounted_price == "") {
                holder.tvTaxTitle.text = cart.tax_title
                holder.tvTaxAmount.text =
                    session.getData(Constant.currency) + stringFormat("" + cart.qty.toInt() * (cart.price.toFloat() * taxPercentage.toFloat() / 100))
            } else {
                holder.tvTaxTitle.text = cart.tax_title
                holder.tvTaxAmount.text =
                    session.getData(Constant.currency) + stringFormat("" + cart.qty.toInt() * (cart.discounted_price.toFloat() * taxPercentage.toFloat() / 100))
            }
            if (cart.tax_percentage == "0") {
                holder.tvTaxTitle.text = "TAX"
            }
            holder.tvTaxPercent.text = "(" + cart.tax_percentage + "%)"
            if (cart.discounted_price == "0" || cart.discounted_price == "") {
                holder.tvSubTotal.text =
                    session.getData(Constant.currency) + stringFormat("" + cart.qty.toInt() * (cart.price.toFloat() + cart.price.toFloat() * taxPercentage.toFloat() / 100))
            } else {
                holder.tvSubTotal.text =
                    session.getData(Constant.currency) + stringFormat("" + cart.qty.toInt() * (cart.discounted_price.toFloat() + cart.discounted_price.toFloat() * taxPercentage.toFloat() / 100))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lyt_checkout_item_list, parent, false)
        return ItemHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvQty: TextView = itemView.findViewById(R.id.tvQty)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvSubTotal: TextView = itemView.findViewById(R.id.tvSubTotal)
        val tvTaxPercent: TextView = itemView.findViewById(R.id.tvTaxPercent)
        val tvTaxTitle: TextView = itemView.findViewById(R.id.tvTaxTitle)
        val tvTaxAmount: TextView = itemView.findViewById(R.id.tvTaxAmount)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

    }

    init {
        try {
            this.activity = activity
            this.carts = carts
            session = Session(activity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}