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
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Cart

@SuppressLint("NotifyDataSetChanged")
class SaveForLaterAdapter(
    val activity: Activity,
    carts: ArrayList<Cart>,
    private var saveForLater: ArrayList<Cart>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    val session: Session = Session(activity)
    var taxPercentage: String
    private var carts: ArrayList<Cart> = carts
    fun add(position: Int, item: Cart) {
        saveForLater.add(position, item)
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    fun removeItem(cart: Cart) {
        CartFragment.isSoldOut = false
        if (CartFragment.values.containsKey(cart.product_variant_id)) {
            CartFragment.values.replace(cart.product_variant_id, "0")
        } else {
            CartFragment.values[cart.product_variant_id] = "0"
        }
        saveForLater.remove(cart)
        CartFragment.tvSaveForLaterTitle.text =
            activity.resources.getString(R.string.save_for_later) + " (" + itemCount + ")"
        CartFragment.tvSaveForLaterTitle.text =
            activity.resources.getString(R.string.save_for_later) + " (" + itemCount + ")"
        CartFragment.values[cart.product_variant_id] = cart.qty
        addMultipleProductInCart(session, activity, Constant.CartValues)
        CartFragment.cartAdapter.notifyDataSetChanged()
        Constant.TOTAL_CART_ITEM = CartFragment.cartAdapter.itemCount
        CartFragment.setData(activity)
        if (saveForLater.size != 0) {
            CartFragment.lytSaveForLater.visibility = View.VISIBLE
        } else {
            CartFragment.lytSaveForLater.visibility = View.GONE
        }
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    fun moveItem(cart: Cart) {
        try {
            Constant.FLOAT_TOTAL_AMOUNT = 0.0
            CartFragment.cartAdapter.add(cart)
            CartFragment.hashMap.clear()
            for (cart1 in carts) {
                val unitMeasurement =
                    if (cart1.unit.equals("kg", ignoreCase = true) || cart1.unit.equals(
                            "ltr",
                            ignoreCase = true
                        )
                    ) 1000 else 1.toLong()
                val unit = cart1.measurement.toDouble().toLong() * unitMeasurement
                if (!CartFragment.hashMap.containsKey(cart1.product_id)) {
                    CartFragment.hashMap[cart1.product_id] =
                        (cart1.stock.toDouble() * (if (cart1.stock_unit_name.equals(
                                "kg",
                                ignoreCase = true
                            ) || cart1.stock_unit_name.equals("ltr", ignoreCase = true)
                        ) 1000 else 1) - unit * cart1.qty.toDouble()).toLong()
                } else {
                    val qty = (unit * cart1.qty.toDouble()).toLong()
                    val availableStock = CartFragment.hashMap[cart1.product_id]?.minus(qty)!!
                    CartFragment.hashMap.replace(cart1.product_id, availableStock)
                }
            }
            removeItem(cart)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(LayoutInflater.from(activity).inflate(R.layout.lyt_save_for_later, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            viewTypeItem -> {
                val holder = holderParent as ItemHolder
                val cart = saveForLater[position]
                val price: Double
                val oPrice: Double
                try {
                    taxPercentage =
                        if (cart.tax_percentage.toDouble() > 0) cart.tax_percentage else "0"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Picasso.get()
                    .load(cart.image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProduct)
                holder.tvDelete.setOnClickListener { removeItem(cart) }
                holder.tvAction.setOnClickListener { moveItem(cart) }
                holder.tvProductName.text = cart.name
                holder.tvMeasurement.text = cart.measurement + "\u0020" + cart.unit
                if (cart.serve_for == Constant.SOLD_OUT_TEXT) {
                    holder.tvStatus.visibility = View.VISIBLE
                }
                if (cart.discounted_price == "0" || cart.discounted_price == "") {
                    price =
                        (cart.price.toFloat() + cart.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                } else {
                    price =
                        (cart.discounted_price.toFloat() + cart.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                    oPrice =
                        (cart.price.toFloat() + cart.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                    holder.tvOriginalPrice.paintFlags =
                        holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.tvOriginalPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + oPrice)
                }
                holder.tvPrice.text = session.getData(Constant.currency) + stringFormat("" + price)
                CartFragment.lytEmpty.visibility =
                    if (itemCount == 0 && carts.size == 0) View.VISIBLE else View.GONE
                if (carts.size != 0) {
                    CartFragment.lytTotal.visibility = View.VISIBLE
                } else {
                    CartFragment.lytTotal.visibility = View.GONE
                }
            }
            viewTypeLoading -> {
                val loadingViewHolder = holderParent as ViewHolderLoading
                loadingViewHolder.progressBar.isIndeterminate = true
            }
        }
    }

    override fun getItemCount(): Int {
        return saveForLater.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        return carts[position].id.toLong()
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

    init {
        taxPercentage = "0"
    }
}