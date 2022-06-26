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
import androidx.cardview.widget.CardView
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
class AdapterStyle2(
    val activity: Activity,
    private val productList: ArrayList<ProductList?>,
    val hashMap: HashMap<String, Long>
) : RecyclerView.Adapter<AdapterStyle2.ItemHolder>() {
    private var session = Session(activity)
    var isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
    private var availableStock = 0.toLong()
    var databaseHelper = DatabaseHelper(activity)

    override fun getItemCount(): Int {
        return 1
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (productList.size > 0) {
            val product = productList[0]!!
            val variant = product.variants[0]
            val maxCartCont =
                if (product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                    session.getData(Constant.max_cart_items_count)
                } else {
                    product.total_allowed_quantity
                }
            if (variant.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                holder.tvStatus21.visibility = View.VISIBLE
                holder.lytQuantity21.visibility = View.INVISIBLE
            } else {
                holder.tvStatus21.visibility = View.GONE
                holder.lytQuantity21.visibility = View.VISIBLE
            }
            val price: Double
            val oPrice: Double
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (productList[0]!!.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (variant.discounted_price == "0" || variant.discounted_price == "") {
                holder.tvDiscountedPrice21.visibility = View.GONE
                price =
                    (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            } else {
                price =
                    (variant.discounted_price.toFloat() + variant.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                oPrice =
                    (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                holder.tvDiscountedPrice21.paintFlags =
                    holder.tvDiscountedPrice21.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvDiscountedPrice21.text =
                    Session(activity).getData(Constant.CURRENCY) + stringFormat("" + oPrice)
                holder.tvDiscountedPrice21.visibility = View.VISIBLE
            }
            holder.tvOriginalPrice21.text =
                Session(activity).getData(Constant.CURRENCY) + stringFormat("" + price)
            holder.tvProductName21.text = product.name
            Picasso.get()
                .load(if (product.image == "") "-" else product.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProduct21)
            holder.layoutStyle21.setOnClickListener {
                val activity1 = activity as AppCompatActivity
                val fragment: Fragment = ProductDetailFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "section")
                bundle.putInt(Constant.VARIANT_POSITION, 0)
                bundle.putString(Constant.ID, product.id)
                fragment.arguments = bundle
                activity1.supportFragmentManager.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            if (isLogin) {
                holder.tvQuantity21.text = variant.cart_count
            } else {
                holder.tvQuantity21.text =
                    databaseHelper.checkCartItemExist(variant.id, variant.product_id)
            }
            holder.btnAddToCart21.visibility =
                if (holder.tvQuantity21.text == "0") View.VISIBLE else View.GONE
            val isLoose = variant.type.equals("loose", ignoreCase = true)
            if (isLoose) {
                holder.btnMinusQty21.setOnClickListener {
                    removeLooseItemFromCartClickEvent(
                        variant,
                        holder.tvQuantity21,
                        holder.btnAddToCart21
                    )
                }
                holder.btnAddQty21.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity21,
                        holder.btnAddToCart21
                    )
                }
                holder.btnAddToCart21.setOnClickListener { 
                    addLooseItemToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity21,
                        holder.btnAddToCart21
                    )
                }
            } else {
                holder.btnMinusQty21.setOnClickListener {
                    removeFromCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity21,
                        holder.btnAddToCart21
                    )
                }
                holder.btnAddQty21.setOnClickListener {
                    addToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity21,
                        holder.btnAddToCart21
                    )
                }
                holder.btnAddToCart21.setOnClickListener { 
                    addToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity21,
                        holder.btnAddToCart21
                    )
                }
            }
        }
        if (productList.size > 1) {
            val product = productList[1]!!
            val variant = product.variants[0]
            val maxCartCont = if (product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                    session.getData(Constant.max_cart_items_count)
                } else {
                    product.total_allowed_quantity
                }
            val price: Double
            val oPrice: Double
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (product.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (variant.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                holder.tvStatus22.visibility = View.VISIBLE
                holder.lytQuantity22.visibility = View.INVISIBLE
            } else {
                holder.tvStatus22.visibility = View.GONE
                holder.lytQuantity22.visibility = View.VISIBLE
            }
            if (variant.discounted_price == "0" || variant.discounted_price == "") {
                holder.tvDiscountedPrice22.visibility = View.GONE
                price =
                    (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            } else {
                price =
                    (variant.discounted_price.toFloat() + variant.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                oPrice =
                    (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                holder.tvDiscountedPrice22.paintFlags =
                    holder.tvDiscountedPrice22.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvDiscountedPrice22.text =
                    Session(activity).getData(Constant.CURRENCY) + stringFormat("" + oPrice)
                holder.tvDiscountedPrice22.visibility = View.VISIBLE
            }
            holder.tvProductName22.text = product.name
            holder.tvOriginalPrice22.text =
                Session(activity).getData(Constant.CURRENCY) + stringFormat("" + price)
            Picasso.get()
                .load(if (product.image == "") "-" else product.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProduct22)
            holder.layoutStyle22.setOnClickListener {
                val activity1 = activity as AppCompatActivity
                val fragment: Fragment = ProductDetailFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "section")
                bundle.putInt(Constant.VARIANT_POSITION, 0)
                bundle.putString(Constant.ID, product.id)
                fragment.arguments = bundle
                activity1.supportFragmentManager.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            if (isLogin) {
                holder.tvQuantity22.text = variant.cart_count
            } else {
                holder.tvQuantity22.text =
                    databaseHelper.checkCartItemExist(variant.id, variant.product_id)
            }
            holder.btnAddToCart22.visibility =
                if (holder.tvQuantity22.text == "0") View.VISIBLE else View.GONE
            val isLoose = variant.type.equals("loose", ignoreCase = true)
            if (isLoose) {
                holder.btnMinusQty22.setOnClickListener {
                    removeLooseItemFromCartClickEvent(
                        variant,
                        holder.tvQuantity22,
                        holder.btnAddToCart22
                    )
                }
                holder.btnAddQty22.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity22,
                        holder.btnAddToCart22
                    )
                }
                holder.btnAddToCart22.setOnClickListener { 
                    addLooseItemToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity22,
                        holder.btnAddToCart22
                    )
                }
            } else {
                holder.btnMinusQty22.setOnClickListener {
                    removeFromCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity22,
                        holder.btnAddToCart22
                    )
                }
                holder.btnAddQty22.setOnClickListener {
                    addToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity22,
                        holder.btnAddToCart22
                    )
                }
                holder.btnAddToCart22.setOnClickListener { 
                    addToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity22,
                        holder.btnAddToCart22
                    )
                }
            }
        }
        if (productList.size > 2) {
            val product = productList[2]!!
            val variant = product.variants[0]
            val maxCartCont = if (product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                    session.getData(Constant.max_cart_items_count)
                } else {
                    product.total_allowed_quantity
                }
            holder.tvProductName23.text = product.name
            val price: Double
            val oPrice: Double
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (product.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (variant.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                holder.tvStatus23.visibility = View.VISIBLE
                holder.lytQuantity23.visibility = View.INVISIBLE
            } else {
                holder.tvStatus23.visibility = View.GONE
                holder.lytQuantity23.visibility = View.VISIBLE
            }
            if (variant.discounted_price == "0" || variant.discounted_price == "") {
                holder.tvDiscountedPrice23.visibility = View.GONE
                price =
                    (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            } else {
                price =
                    (variant.discounted_price.toFloat() + variant.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                oPrice =
                    (variant.price.toFloat() + variant.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                holder.tvDiscountedPrice23.paintFlags =
                    holder.tvDiscountedPrice23.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvDiscountedPrice23.text =
                    Session(activity).getData(Constant.CURRENCY) + stringFormat("" + oPrice)
                holder.tvDiscountedPrice23.visibility = View.VISIBLE
            }
            holder.tvOriginalPrice23.text =
                Session(activity).getData(Constant.CURRENCY) + stringFormat("" + price)
            Picasso.get()
                .load(if (product.image == "") "-" else product.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProduct23)
            holder.layoutStyle23.setOnClickListener {
                val activity1 = activity as AppCompatActivity
                val fragment: Fragment = ProductDetailFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "section")
                bundle.putInt(Constant.VARIANT_POSITION, 0)
                bundle.putString(Constant.ID, product.id)
                fragment.arguments = bundle
                activity1.supportFragmentManager.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            if (isLogin) {
                holder.tvQuantity23.text = variant.cart_count
            } else {
                holder.tvQuantity23.text =
                    databaseHelper.checkCartItemExist(variant.id, variant.product_id)
            }
            holder.btnAddToCart23.visibility =
                if (holder.tvQuantity23.text == "0") View.VISIBLE else View.GONE
            val isLoose = variant.type.equals("loose", ignoreCase = true)
            if (isLoose) {
                holder.btnMinusQty23.setOnClickListener {
                    removeLooseItemFromCartClickEvent(
                        variant,
                        holder.tvQuantity23,
                        holder.btnAddToCart23
                    )
                }
                holder.btnAddQty23.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity23,
                        holder.btnAddToCart23
                    )
                }
                holder.btnAddToCart23.setOnClickListener { 
                    addLooseItemToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity23,
                        holder.btnAddToCart23
                    )
                }
            } else {
                holder.btnMinusQty23.setOnClickListener {
                    removeFromCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity23,
                        holder.btnAddToCart23
                    )
                }
                holder.btnAddQty23.setOnClickListener {
                    addToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity23,
                        holder.btnAddToCart23
                    )
                }
                holder.btnAddToCart23.setOnClickListener { 
                    addToCartClickEvent(
                        variant,
                        maxCartCont,
                        holder.tvQuantity23,
                        holder.btnAddToCart23
                    )
                }
            }
        }
    }

    private fun addLooseItemToCartClickEvent(
        variants: Variants,
        maxCartCont: String,
        tvQuantity: TextView,
        btnAddToCart: TextView
    ) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = (variants.measurement.toDouble() * unitMeasurement).toLong()
        availableStock = hashMap[variants.product_id].toString().toLong()
        if (session.getData(Constant.STATUS) == "1") {
            var count = tvQuantity.text.toString().toInt()
            if (count <= maxCartCont.toInt()) {
                count++
                if (availableStock >= unit) {
                    if (count != 0) {
                        btnAddToCart.visibility = View.GONE
                    }
                    if (hashMap.containsKey(variants.product_id)) {
                        hashMap.replace(variants.product_id,
                            hashMap[variants.product_id]?.minus(unit)!!
                        )
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
                        btnAddToCart.visibility = View.GONE
                    } else {
                        btnAddToCart.visibility = View.VISIBLE
                    }
                    tvQuantity.text = "" + count
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

    private fun removeLooseItemFromCartClickEvent(
        variants: Variants,
        tvQuantity: TextView,
        btnAddToCart: TextView
    ) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = variants.measurement.toDouble().toLong() * unitMeasurement
        if (session.getData(Constant.STATUS) == "1") {
            var count = tvQuantity.text.toString().toInt()
            count--
            availableStock += unit
            if (count == 0) {
                btnAddToCart.visibility = View.VISIBLE
            }
            if (isLogin) {
                if (count <= 0) {
                    tvQuantity.text = "" + count
                    if (Constant.CartValues.containsKey(variants.id)) {
                        Constant.CartValues.replace(variants.id, "" + count)
                    } else {
                        Constant.CartValues[variants.id] = "" + count
                    }
                    addMultipleProductInCart(session, activity, Constant.CartValues)
                }
            } else {
                tvQuantity.text = "" + count
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

    private fun addToCartClickEvent(
        variants: Variants,
        maxCartCont: String,
        tvQuantity: TextView,
        btnAddToCart: TextView
    ) {
        if (session.getData(Constant.STATUS) == "1") {
            var count = tvQuantity.text.toString().toInt()
            if (count < variants.stock.toFloat()) {
                if (count < maxCartCont.toInt()) {
                    count++
                    if (count != 0) {
                        btnAddToCart.visibility = View.GONE
                    }
                    tvQuantity.text = "" + count
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(variants.id)) {
                            Constant.CartValues.replace(variants.id, "" + count)
                        } else {
                            Constant.CartValues[variants.id] = "" + count
                        }
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                    } else {
                        tvQuantity.text = "" + count
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

    private fun removeFromCartClickEvent(
        variants: Variants,
        maxCartCont: String,
        tvQuantity: TextView,
        btnAddToCart: TextView
    ) {
        if (session.getData(Constant.STATUS) == "1") {
            var count = tvQuantity.text.toString().toInt()
            if (count <= variants.stock.toFloat()) {
                if (count <= maxCartCont.toInt()) {
                    count--
                    if (count == 0) {
                        btnAddToCart.visibility = View.VISIBLE
                    }
                    tvQuantity.text = "" + count
                    if (isLogin) {
                        if (count <= 0) {
                            tvQuantity.text = "" + count
                            if (Constant.CartValues.containsKey(variants.id)) {
                                Constant.CartValues.replace(variants.id, "" + count)
                            } else {
                                Constant.CartValues[variants.id] = "" + count
                            }
                            addMultipleProductInCart(session, activity, Constant.CartValues)
                        }
                    } else {
                        tvQuantity.text = "" + count
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lyt_style_2, parent, false)
        return ItemHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct21: ImageView = itemView.findViewById(R.id.imgProduct21)
        val imgProduct22: ImageView = itemView.findViewById(R.id.imgProduct22)
        val imgProduct23: ImageView = itemView.findViewById(R.id.imgProduct23)
        val tvProductName21: TextView = itemView.findViewById(R.id.tvProductName21)
        val tvProductName22: TextView = itemView.findViewById(R.id.tvProductName22)
        val tvProductName23: TextView = itemView.findViewById(R.id.tvProductName23)
        val tvOriginalPrice21: TextView = itemView.findViewById(R.id.tvOriginalPrice21)
        val tvDiscountedPrice21: TextView = itemView.findViewById(R.id.tvDiscountedPrice21)
        val tvOriginalPrice22: TextView = itemView.findViewById(R.id.tvOriginalPrice22)
        val tvDiscountedPrice22: TextView = itemView.findViewById(R.id.tvDiscountedPrice22)
        val tvOriginalPrice23: TextView = itemView.findViewById(R.id.tvOriginalPrice23)
        val tvDiscountedPrice23: TextView = itemView.findViewById(R.id.tvDiscountedPrice23)
        val layoutStyle21: CardView = itemView.findViewById(R.id.layoutStyle21)
        val layoutStyle22: CardView = itemView.findViewById(R.id.layoutStyle22)
        val layoutStyle23: CardView = itemView.findViewById(R.id.layoutStyle23)
        val tvStatus21: TextView = itemView.findViewById(R.id.tvStatus21)
        val tvStatus22: TextView = itemView.findViewById(R.id.tvStatus22)
        val tvStatus23: TextView = itemView.findViewById(R.id.tvStatus23)
        val lytQuantity21: RelativeLayout = itemView.findViewById(R.id.lytQuantity21)
        val lytQuantity22: RelativeLayout = itemView.findViewById(R.id.lytQuantity22)
        val lytQuantity23: RelativeLayout = itemView.findViewById(R.id.lytQuantity23)
        val btnMinusQty21: ImageView = itemView.findViewById(R.id.btnMinusQty21)
        val btnMinusQty22: ImageView = itemView.findViewById(R.id.btnMinusQty22)
        val btnMinusQty23: ImageView = itemView.findViewById(R.id.btnMinusQty23)
        val tvQuantity21: TextView = itemView.findViewById(R.id.tvQuantity21)
        val tvQuantity22: TextView = itemView.findViewById(R.id.tvQuantity22)
        val tvQuantity23: TextView = itemView.findViewById(R.id.tvQuantity23)
        val btnAddQty21: ImageView = itemView.findViewById(R.id.btnAddQty21)
        val btnAddQty22: ImageView = itemView.findViewById(R.id.btnAddQty22)
        val btnAddQty23: ImageView = itemView.findViewById(R.id.btnAddQty23)
        val btnAddToCart21: TextView = itemView.findViewById(R.id.btnAddToCart21)
        val btnAddToCart22: TextView = itemView.findViewById(R.id.btnAddToCart22)
        val btnAddToCart23: TextView = itemView.findViewById(R.id.btnAddToCart23)

    }
}