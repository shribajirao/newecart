package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
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
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.fragment.ProductDetailFragment
import wrteam.ecart.shop.fragment.ProductListFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.calculateDays
import wrteam.ecart.shop.helper.ApiConfig.Companion.getDiscount
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setFormatTime
import wrteam.ecart.shop.model.ProductList
import wrteam.ecart.shop.model.Variants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.properties.Delegates

/**
 * Created by shree1 on 3/16/2017.
 */
class FlashSaleAdapter(val activity: Activity, private val productList: ArrayList<ProductList?>) :
    RecyclerView.Adapter<FlashSaleAdapter.ItemHolder>() {
    val session: Session = Session(activity)
    var isLogin by Delegates.notNull<Boolean>()
    var databaseHelper: DatabaseHelper
    private var availableStock = 0.toLong()

    override fun getItemCount(): Int {
        return productList.size.coerceAtMost(6)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        try {
            val product = productList[position]!!
            holder.setIsRecyclable(false)
            if (position == 5) {
                holder.tvViewAll.visibility = View.VISIBLE
                holder.lytMain.visibility = View.INVISIBLE
            } else {
                holder.tvViewAll.visibility = View.INVISIBLE
                holder.lytMain.visibility = View.VISIBLE
            }
            val variants = product.variants[0]
            val flashSale = variants.flash_sales[0]
            Picasso.get()
                .load(if (product.image == "") "-" else product.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProduct)
            holder.productName.text = product.name
            if (variants.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                holder.tvStatus.visibility = View.VISIBLE
                holder.lytQuantity.visibility = View.GONE
            } else {
                holder.tvStatus.visibility = View.GONE
                holder.lytQuantity.visibility = View.VISIBLE
            }
            var originalPrice = 0.0
            var discountedPrice = 0.0
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (product.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val startDate: Date
            val endDate: Date
            val different: Long
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            if (flashSale.is_start) {
                startDate = df.parse(session.getData(Constant.current_date))!!
                endDate = df.parse(flashSale.end_date)!!
                different = endDate.time - startDate.time
                val days = different / (60 * 60 * 24 * 1000)
                if (setFormatTime(days).equals("00", ignoreCase = true)) {
                    startTimer(holder, variants, different)
                } else {
                    holder.tvTimer.text = calculateDays(activity, days)
                }
                holder.tvTimerTitle.text = activity.getString(R.string.ends_in)
                if (flashSale.discounted_price == "0" || flashSale.discounted_price == "") {
                    holder.showDiscount.visibility = View.GONE
                    holder.tvPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + originalPrice)
                } else {
                    holder.showDiscount.visibility = View.VISIBLE
                    discountedPrice =
                        (flashSale.discounted_price.toFloat() + flashSale.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                    originalPrice =
                        (flashSale.price.toFloat() + flashSale.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                    holder.tvOriginalPrice.paintFlags =
                        holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.tvOriginalPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + originalPrice)
                    holder.tvPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + discountedPrice)
                }
            } else {
                startDate = df.parse(session.getData(Constant.current_date))!!
                endDate = df.parse(flashSale.start_date)!!
                different = endDate.time - startDate.time
                val days = different / (60 * 60 * 24 * 1000)
                if (setFormatTime(days).equals("00", ignoreCase = true)) {
                    startTimer(holder, variants, different)
                } else {
                    holder.tvTimer.text = calculateDays(activity, days)
                }
                holder.tvTimerTitle.text = activity.getString(R.string.starts_in)
                calculateDays(activity, abs(days)).also { holder.tvTimer.text = it }
                if (variants.discounted_price == "0" || variants.discounted_price == "") {
                    holder.showDiscount.visibility = View.GONE
                    holder.tvPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + originalPrice)
                } else {
                    holder.showDiscount.visibility = View.VISIBLE
                    discountedPrice =
                        (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                    originalPrice =
                        (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                    holder.tvOriginalPrice.paintFlags =
                        holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.tvOriginalPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + originalPrice)
                    holder.tvPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + discountedPrice)
                }
            }
            holder.showDiscount.text = "-" + getDiscount(originalPrice, discountedPrice)
            holder.lytMain.setOnClickListener {
                val activity1 = activity as AppCompatActivity
                val fragment: Fragment = ProductDetailFragment()
                val bundle = Bundle()
                bundle.putString(Constant.ID, variants.product_id)
                bundle.putString(Constant.FROM, "section")
                bundle.putInt("variantsPosition", 0)
                fragment.arguments = bundle
                activity1.supportFragmentManager.beginTransaction()
                    .add(R.id.container, fragment).addToBackStack(null).commit()
            }
            holder.tvViewAll.setOnClickListener {
                val fragment: Fragment = ProductListFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "flash_sale")
                bundle.putString(Constant.NAME, activity.getString(R.string.flash_sale))
//                bundle.putString(Constant.ID, flashSale.flash_sales_id)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            if (isLogin) {
                holder.tvQuantity.text = variants.cart_count
            } else {
                holder.tvQuantity.text = databaseHelper.checkCartItemExist(
                    product.variants[0].id,
                    product.variants[0].product_id
                )
            }
            holder.btnAddToCart.visibility =
                if (holder.tvQuantity.text == "0") View.VISIBLE else View.GONE
            val maxCartCont =
                if (product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                    session.getData(Constant.max_cart_items_count)
                } else {
                    product.total_allowed_quantity
                }
            val isLoose = variants.type.equals("loose", ignoreCase = true)
            if (isLoose) {
                holder.btnMinusQty.setOnClickListener {
                    removeLooseItemFromCartClickEvent(
                        holder,
                        variants
                    )
                }
                holder.btnAddQty.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
                holder.btnAddToCart.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
            } else {
                holder.btnMinusQty.setOnClickListener {
                    removeFromCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
                holder.btnAddQty.setOnClickListener {
                    addToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
                holder.btnAddToCart.setOnClickListener {
                    addToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addLooseItemToCartClickEvent(
        holder: ItemHolder,
        variants: Variants,
        maxCartCont: String
    ) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = variants.measurement.toDouble().toLong() * unitMeasurement
        if (session.getData(Constant.STATUS) == "1") {
            var count = holder.tvQuantity.text.toString().toInt()
            if (count <= maxCartCont.toInt()) {
                count++
                if (availableStock >= unit) {
                    availableStock -= unit
                    if (count != 0) {
                        holder.btnAddToCart.visibility = View.GONE
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
            if (count == 0) {
                holder.btnAddToCart.visibility = View.VISIBLE
            }
            availableStock += unit
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

    private fun removeFromCartClickEvent(
        holder: ItemHolder,
        variants: Variants,
        maxCartCont: String
    ) {
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
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.lyt_flash_item_grid, parent, false)
        return ItemHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lytMain: RelativeLayout = itemView.findViewById(R.id.lytMain)
        val showDiscount: TextView = itemView.findViewById(R.id.showDiscount)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvTimer: TextView = itemView.findViewById(R.id.tvTimer)
        val tvTimerTitle: TextView = itemView.findViewById(R.id.tvTimerTitle)
        val tvViewAll: TextView = itemView.findViewById(R.id.tvViewAll)
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val btnAddQty: ImageView = itemView.findViewById(R.id.btnAddQty)
        val btnMinusQty: ImageView = itemView.findViewById(R.id.btnMinusQty)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val lytQuantity: RelativeLayout = itemView.findViewById(R.id.lytQuantity)
        val btnAddToCart: TextView = itemView.findViewById(R.id.btnAddToCart)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

    }

    private fun startTimer(holder: ItemHolder, variants: Variants, duration: Long) {
        try {
            object : CountDownTimer(duration, 1000) {
                override fun onTick(different: Long) {
                    val seconds = (different / 1000).toInt() % 60
                    val minutes = (different / (1000 * 60) % 60).toInt()
                    val hours = (different / (1000 * 60 * 60) % 24).toInt()
                    holder.tvTimer.text = String.format(
                        "%s:%s:%s",
                        setFormatTime(hours.toLong()),
                        setFormatTime(minutes.toLong()),
                        setFormatTime(seconds.toLong())
                    )
                }

                override fun onFinish() {
                    if (!variants.flash_sales[0].is_start) {
                        variants.flash_sales[0].is_start = true
                        variants.is_flash_sales = "true"
                    } else {
                        variants.is_flash_sales = "false"
                    }
                    notifyDataSetChanged()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        availableStock = 0
        databaseHelper = DatabaseHelper(activity)
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
    }
}