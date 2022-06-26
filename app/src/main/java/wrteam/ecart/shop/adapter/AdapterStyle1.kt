package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.ProductDetailFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.ProductList
import wrteam.ecart.shop.model.Variants

/**
 * Created by shree1 on 3/16/2017.
 */
class AdapterStyle1(
    val activity: Activity,
    private val productList: ArrayList<ProductList?>,
    private val itemResource: Int,
    var hashMap: HashMap<String, Long>,
    var session: Session = Session(activity)
) : RecyclerView.Adapter<AdapterStyle1.ItemHolder>() {
    val isLogin: Boolean = session.getBoolean(Constant.IS_USER_LOGIN)
    var databaseHelper: DatabaseHelper = DatabaseHelper(activity)
    private var availableStock = 0.toLong()
    override fun getItemCount(): Int {
        return productList.size.coerceAtMost(4)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val product = productList[position]!!
        val variant = product.variants[0]
        for (product_ in productList) {
            for (variant_ in product_!!.variants) {
                val unitMeasurement = if (variant_.measurement_unit_name.equals(
                        "kg",
                        ignoreCase = true
                    ) || variant_.measurement_unit_name.equals("ltr", ignoreCase = true)
                ) 1000 else 1.toLong()
                val unit = variant_.measurement.toDouble().toLong() * unitMeasurement
                if (!hashMap.containsKey(variant_.product_id)) {
                    hashMap[variant_.product_id] =
                        (variant_.stock.toDouble() * (if (variant_.stock_unit_name.equals(
                                "kg",
                                ignoreCase = true
                            ) || variant_.stock_unit_name.equals("ltr", ignoreCase = true)
                        ) 1000 else 1) - unit * variant_.cart_count.toLong()).toLong()
                } else {
                    hashMap.replace(variant_.product_id,hashMap[variant_.product_id]?.minus(unit * variant_.cart_count.toLong())!!)
                }
            }
        }
        val maxCartCont: String = if (product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                session.getData(Constant.max_cart_items_count)
            } else {
                product.total_allowed_quantity
            }
        if (variant.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
            holder.tvStatus.visibility = View.VISIBLE
            holder.lytQuantity.visibility = View.GONE
        } else {
            holder.tvStatus.visibility = View.GONE
            holder.lytQuantity.visibility = View.VISIBLE
        }
        Picasso.get().load(if (product.image == "") "-" else product.image)
            .fit()
            .centerInside()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgProduct)
        holder.tvTitle.text = product.name
        val price: Double
        val oPrice: Double
        var taxPercentage = "0"
        try {
            taxPercentage =
                if (product.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (variant.discounted_price == "0" || variant.discounted_price == "") {
            holder.tvDPrice.visibility = View.GONE
            price =
                (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
        } else {
            price =
                (variant.discounted_price.toFloat() + variant.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            oPrice =
                (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            holder.tvDPrice.paintFlags = holder.tvDPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvDPrice.text =
                Session(activity).getData(Constant.CURRENCY) + stringFormat("" + oPrice)
            holder.tvDPrice.visibility = View.VISIBLE
        }
        holder.tvPrice.text =
            Session(activity).getData(Constant.CURRENCY) + stringFormat("" + price)
        holder.tvTitle.text = product.name
        holder.relativeLayout.setOnClickListener {
            val activity1 = activity as AppCompatActivity
            val fragment: Fragment = ProductDetailFragment()
            val bundle = Bundle()
            bundle.putString(Constant.ID, product.variants[0].product_id)
            bundle.putString(Constant.FROM, "section")
            bundle.putInt(Constant.VARIANT_POSITION, 0)
            fragment.arguments = bundle
            activity1.supportFragmentManager.beginTransaction().add(R.id.container, fragment)
                .addToBackStack(null).commit()
        }
        if (isLogin) {
            holder.tvQuantity.text = variant.cart_count
        } else {
            holder.tvQuantity.text = databaseHelper.checkCartItemExist(
                product.variants[0].id,
                product.variants[0].product_id
            )
        }
        holder.btnAddToCart.visibility =
            if (holder.tvQuantity.text == "0") View.VISIBLE else View.GONE
        val isLoose = variant.type.equals("loose", ignoreCase = true)
        if (isLoose) {
            holder.btnMinusQty.setOnClickListener {
                removeLooseItemFromCartClickEvent(
                    holder,
                    variant
                )
            }
            holder.btnAddQty.setOnClickListener {
                addLooseItemToCartClickEvent(
                    holder,
                    variant,
                    maxCartCont
                )
            }
            holder.btnAddToCart.setOnClickListener {
                addLooseItemToCartClickEvent(
                    holder,
                    variant,
                    maxCartCont
                )
            }
        } else {
            holder.btnMinusQty.setOnClickListener {
                removeFromCartClickEvent(
                    holder,
                    variant,
                    maxCartCont
                )
            }
            holder.btnAddQty.setOnClickListener {
                addToCartClickEvent(
                    holder,
                    variant,
                    maxCartCont
                )
            }
            holder.btnAddToCart.setOnClickListener {
                addToCartClickEvent(
                    holder,
                    variant,
                    maxCartCont
                )
            }
        }
    }

    private fun addLooseItemToCartClickEvent(holder: ItemHolder, variants: Variants, maxCartCont: String) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = (variants.measurement.toDouble() * unitMeasurement).toLong()
        availableStock = hashMap["" + variants.product_id].toString().toLong()
        if (session.getData(Constant.STATUS) == "1") {
            var count = holder.tvQuantity.text.toString().toInt()
            if (count <= maxCartCont.toInt()) {
                count++
                if (count != 0) {
                    holder.btnAddToCart.visibility = View.GONE
                }
                if (availableStock >= unit) {
                    if (hashMap.containsKey(variants.product_id)) {
                        hashMap.replace(variants.product_id,hashMap[variants.product_id].toString().toLong().minus(unit))
                    } else {
                        hashMap[variants.product_id] = unit
                    }
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(variants.id)) {
                            Constant.CartValues.replace(variants.id, "" + count)
                        } else {
                            Constant.CartValues[variants.id] = "" + count
                        }
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                    } else {
                        databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    if (count > 0) {
                        holder.btnAddToCart.visibility = View.GONE
                    } else {
                        holder.btnAddToCart.visibility = View.VISIBLE
                    }
                    holder.tvQuantity.text = "" + count
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.stock_limit),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.limit_alert),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeLooseItemFromCartClickEvent(holder: ItemHolder, variants: Variants) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = variants.measurement.toDouble().toLong() * unitMeasurement
        if (session.getData(Constant.STATUS) == "1") {
            var count = holder.tvQuantity.text.toString().toInt()
            count--
            availableStock += unit
            if (count == 0) {
                holder.btnAddToCart.visibility = View.VISIBLE
            }
            if (isLogin) {
                if (count <= 0) {
                    holder.tvQuantity.text = "" + count
                    if (Constant.CartValues.containsKey(variants.id)) {
                        Constant.CartValues.replace(variants.id, "" + count)
                    } else {
                        Constant.CartValues[variants.id] = "" + count
                    }
                    addMultipleProductInCart(session, activity, Constant.CartValues)
                }
            } else {
                holder.tvQuantity.text = "" + count
                databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                databaseHelper.getTotalItemOfCart(activity)
            }
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addToCartClickEvent(holder: ItemHolder, variants: Variants, maxCartCont: String) {
        if (session.getData(Constant.STATUS) == "1") {
            var count = holder.tvQuantity.text.toString().toInt()
            if (count < variants.stock.toFloat()) {
                if (count < maxCartCont.toInt()) {
                    count++
                    if (count != 0) {
                        holder.btnAddToCart.visibility = View.GONE
                    }
                    holder.tvQuantity.text = "" + count
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(variants.id)) {
                            Constant.CartValues.replace(variants.id, "" + count)
                        } else {
                            Constant.CartValues[variants.id] = "" + count
                        }
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                    } else {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.limit_alert),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.stock_limit),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun removeFromCartClickEvent(holder: ItemHolder, variants: Variants, maxCartCont: String) {
        if (session.getData(Constant.STATUS) == "1") {
            var count = holder.tvQuantity.text.toString().toInt()
            if (count <= variants.stock.toFloat()) {
                if (count <= maxCartCont.toInt()) {
                    count--
                    if (count == 0) {
                        holder.btnAddToCart.visibility = View.VISIBLE
                    }
                    holder.tvQuantity.text = "" + count
                    if (isLogin) {
                        if (count <= 0) {
                            holder.tvQuantity.text = "" + count
                            if (Constant.CartValues.containsKey(variants.id)) {
                                Constant.CartValues.replace(variants.id, "" + count)
                            } else {
                                Constant.CartValues[variants.id] = "" + count
                            }
                            addMultipleProductInCart(session, activity, Constant.CartValues)
                        }
                    } else {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.limit_alert),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.stock_limit),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(itemResource, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val btnAddQty: ImageView = itemView.findViewById(R.id.btnAddQty)
        val btnMinusQty: ImageView = itemView.findViewById(R.id.btnMinusQty)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvDPrice: TextView = itemView.findViewById(R.id.tvDPrice)
        val relativeLayout: RelativeLayout = itemView.findViewById(R.id.play_layout)
        val lytQuantity: RelativeLayout = itemView.findViewById(R.id.lytQuantity)
        val btnAddToCart: TextView = itemView.findViewById(R.id.btnAddToCart)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

    }
}