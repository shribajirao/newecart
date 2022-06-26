package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.CartFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Cart

@SuppressLint("NotifyDataSetChanged")
class OfflineSaveForLaterAdapter(
    val activity: Activity,
    private var saveForLaterItems: ArrayList<Cart>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    val session: Session

    private fun removeItem(position: Int) {
        val cart = saveForLaterItems[position]
        databaseHelper.removeFromSaveForLater(cart.product_id, cart.product_variant_id)
        saveForLaterItems.remove(cart)
        if (itemCount == 0) CartFragment.lytSaveForLater.visibility = View.GONE
        CartFragment.saveForLaterAdapter.notifyDataSetChanged()
        CartFragment.cartAdapter.notifyDataSetChanged()
    }

    private fun moveItem(position: Int) {
        try {
            val cart = saveForLaterItems[position]
            databaseHelper.moveToCartOrSaveForLater(
                cart.product_variant_id,
                cart.product_id,
                "save_for_later",
                activity
            )
            saveForLaterItems.remove(cart)
            CartFragment.cartAdapter.add(cart)
            if (itemCount == 0) CartFragment.lytSaveForLater.visibility = View.GONE
            if (CartFragment.cartAdapter.itemCount != 0) CartFragment.lytTotal.visibility =
                View.VISIBLE
            notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_save_for_later, parent, false)
        )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val cart = saveForLaterItems[position]
            Picasso.get()
                .load(cart.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holderParent.imgProduct)
            holderParent.tvProductName.text = cart.name
            holderParent.tvMeasurement.text = cart.measurement + "\u0020" + cart.unit
            val price: Double
            val oPrice: Double
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (cart.tax_percentage.toDouble() > 0) cart.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (cart.discounted_price == "0" || cart.discounted_price == "") {
                price =
                    (cart.price.toFloat() + cart.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            } else {
                price =
                    (cart.discounted_price.toFloat() + cart.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                oPrice =
                    (cart.price.toFloat() + cart.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                holderParent.tvOriginalPrice.paintFlags =
                    holderParent.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holderParent.tvOriginalPrice.text =
                    session.getData(Constant.currency) + stringFormat("" + oPrice)
            }
            if (!cart.serve_for.equals("available", ignoreCase = true)) {
                holderParent.tvStatus.visibility = View.VISIBLE
            }
            holderParent.tvPrice.text =
                Session(activity).getData(Constant.currency) + stringFormat("" + price)
            holderParent.tvDelete.setOnClickListener { removeItem(position) }
            holderParent.tvAction.setOnClickListener { moveItem(position) }
            holderParent.tvProductName.text = cart.name
            holderParent.tvMeasurement.text = cart.measurement + "\u0020" + cart.unit
            if (saveForLaterItems.size > 0) {
                CartFragment.lytSaveForLater.visibility = View.VISIBLE
            } else {
                CartFragment.lytSaveForLater.visibility = View.GONE
            }
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return saveForLaterItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val cart = saveForLaterItems[position]
        return cart.product_variant_id.toInt().toLong()
    }

    fun add(cart: Cart) {
        saveForLaterItems.add(cart)
        notifyDataSetChanged()
        if (saveForLaterItems.size != 0) {
            CartFragment.lytSaveForLater.visibility = View.VISIBLE
        }
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvMeasurement: TextView = itemView.findViewById(R.id.tvMeasurement)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val lytMain: RelativeLayout = itemView.findViewById(R.id.lytMain)

    }

    companion object {
        lateinit var databaseHelper: DatabaseHelper
    }

    init {
        databaseHelper = DatabaseHelper(activity)
        session = Session(activity)
    }
}