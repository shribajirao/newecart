package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.ProductDetailFragment
import wrteam.ecart.shop.fragment.ProductListFragment
import wrteam.ecart.shop.helper.ApiConfig
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.calculateDays
import wrteam.ecart.shop.helper.ApiConfig.Companion.getDiscount
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setFormatTime
import wrteam.ecart.shop.model.Cart
import wrteam.ecart.shop.model.ProductList
import wrteam.ecart.shop.model.Variants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


@SuppressLint("SetTextI18n", "NotifyDataSetChanged")
class ProductLoadMoreAdapter(
    val activity: Activity,
    private val productArrayList: ArrayList<ProductList?>,
    val resource: Int,
    val from: String,
    val hashMap: HashMap<String, Long>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val session = Session(activity)
    val isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
    var databaseHelper = DatabaseHelper(activity)
    private var isFavorite = false

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(LayoutInflater.from(activity).inflate(resource, parent, false))
    }

    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            try {
                val product = productArrayList[position]!!
                val variants = product.variants

                if (variants.size == 1) {
                    holderParent.spinner.visibility = View.GONE
                    holderParent.lytSpinner.visibility = View.GONE
                }

                if (product.indicator != "0") {
                    holderParent.imgIndicator.visibility = View.VISIBLE
                    if (product.indicator == "1") holderParent.imgIndicator.setImageResource(R.drawable.ic_veg_icon) else if (product.indicator == "2") holderParent.imgIndicator.setImageResource(
                        R.drawable.ic_non_veg_icon
                    )
                }

                holderParent.tvProductName.text = Html.fromHtml(product.name, 0)

                if (session.getData(Constant.ratings) == "1") {
                    holderParent.lytRatings.visibility = View.VISIBLE
                    holderParent.tvRatingCount.text = "(" + product.number_of_ratings + ")"
                    holderParent.ratingProduct.rating = product.ratings.toFloat()
                } else {
                    holderParent.lytRatings.visibility = View.GONE
                }

                val variantsName = arrayOfNulls<String>(variants.size)
                val variantsStockStatus = arrayOfNulls<String>(variants.size)
                for ((i, name) in variants.withIndex()) {
                    variantsName[i] = name.measurement + " " + name.measurement_unit_name
                    variantsStockStatus[i] = name.serve_for
                }

                holderParent.spinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            setSelectedData(holderParent, variants[position], product)
                        }
                    }

                setSelectedData(holderParent, variants[0], product)

                val customAdapter = CustomAdapter(activity, variantsName, variantsStockStatus)
                holderParent.spinner.adapter = customAdapter

                Picasso.get().load(product.image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holderParent.imgProduct)

                holderParent.lytMain.setOnClickListener {
                    if (Constant.CartValues.size != 0) addMultipleProductInCart(
                        session,
                        activity,
                        Constant.CartValues
                    )
                    val activity1 = activity as AppCompatActivity
                    val fragment: Fragment = ProductDetailFragment()
                    val bundle = Bundle()
                    bundle.putInt(
                        "variantsPosition",
                        if (variants.size == 1) 0 else holderParent.spinner.selectedItemPosition
                    )
                    bundle.putString("id", product.id)
                    bundle.putString(Constant.FROM, from)
                    bundle.putInt("position", position)
                    fragment.arguments = bundle
                    activity1.supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment).addToBackStack(null).commit()
                }
                if (isLogin) {
                    holderParent.tvQuantity.text = variants[0].cart_count
                    if (product.is_favorite) {
                        holderParent.imgFav.setImageResource(R.drawable.ic_is_favorite)
                    } else {
                        holderParent.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                    }
                    val session = Session(
                        activity
                    )
                    holderParent.imgFav.setOnClickListener {
                        isFavorite = product.is_favorite
                        if (from != "favorite") {
                            if (isFavorite) {
                                isFavorite = false
                                holderParent.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                                holderParent.lottieAnimationView.visibility = View.GONE
                            } else {
                                isFavorite = true
                                holderParent.lottieAnimationView.visibility = View.VISIBLE
                                holderParent.lottieAnimationView.playAnimation()
                            }
                        } else {
                            isFavorite = false
                            productArrayList.remove(product)
                            notifyDataSetChanged()
                        }
                        product.is_favorite = isFavorite
                        ApiConfig.addOrRemoveFavorite(
                            activity,
                            session,
                            product.variants[0].product_id,
                            isFavorite
                        )
                    }
                } else {
                    holderParent.tvQuantity.text =
                        databaseHelper.checkCartItemExist(product.variants[0].id, product.id)
                    if (databaseHelper.getFavoriteById(product.id)) {
                        holderParent.imgFav.setImageResource(R.drawable.ic_is_favorite)
                    } else {
                        holderParent.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                    }
                    holderParent.imgFav.setOnClickListener {
                        isFavorite = databaseHelper.getFavoriteById(product.id)
                        databaseHelper.addOrRemoveFavorite(
                            product.variants[0].product_id,
                            isFavorite
                        )
                        if (from != "favorite") {
                            if (isFavorite) {
                                isFavorite = false
                                holderParent.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                                holderParent.lottieAnimationView.visibility = View.GONE
                            } else {
                                isFavorite = true
                                holderParent.lottieAnimationView.visibility = View.VISIBLE
                                holderParent.lottieAnimationView.playAnimation()
                            }
                        } else {
                            isFavorite = false
                            productArrayList.remove(product)
                        }
                        databaseHelper.addOrRemoveFavorite(
                            product.variants[0].product_id,
                            isFavorite
                        )
                        notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return productArrayList.size
    }

    override fun getItemId(position: Int): Long {
        return productArrayList[position]!!.id.toInt().toLong()
    }

    class CustomAdapter(
        internal var activity: Activity,
        private var variantNames: Array<String?>,
        private var variantsStockStatus: Array<String?>
    ) :
        BaseAdapter() {
        private var inflater: LayoutInflater = LayoutInflater.from(activity)

        override fun getCount(): Int {
            return variantNames.size
        }

        override fun getItem(i: Int): String? {
            return variantNames[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        @SuppressLint("ViewHolder", "InflateParams")
        override fun getView(position: Int, view1: View?, viewGroup: ViewGroup): View {
            val view = inflater.inflate(R.layout.lyt_spinner_item, null)
            val tvMeasurement = view.findViewById<View>(R.id.tvMeasurement) as TextView
            if (variantsStockStatus[position] == Constant.SOLD_OUT_TEXT) {
                tvMeasurement.setTextColor(ContextCompat.getColor(activity, R.color.red))
            } else {
                tvMeasurement.setTextColor(ContextCompat.getColor(activity, R.color.txt_color))
            }
            tvMeasurement.text = variantNames[position]
            return view
        }
    }

    @SuppressLint("SetTextI18n")
    fun setSelectedData(holder: ItemHolder, variants: Variants, product: ProductList) {

//        GST_Amount (Original Cost x GST %)/100
//        Net_Price Original Cost + GST Amount
        try {
            holder.tvMeasurement.text =
                variants.measurement + variants.measurement_unit_name
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                if (Constant.CartValues.containsKey(variants.id)) {
                    holder.tvQuantity.text = "" + Constant.CartValues[variants.id]
                }
            } else {
                holder.tvQuantity.text = session.getData(variants.id)
            }
            var originalPrice: Double
            var discountedPrice: Double
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (product.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            discountedPrice =
                (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            originalPrice =
                (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            if (variants.is_flash_sales == "false") {
                holder.lytTimer.visibility = View.GONE
                discountedPrice =
                    (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                originalPrice =
                    (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                if (variants.discounted_price == "0" || variants.discounted_price == "") {
                    holder.tvOriginalPrice.visibility = View.GONE
                    holder.showDiscount.visibility = View.GONE
                    holder.tvSavePrice.visibility = View.GONE
                    holder.tvPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + originalPrice)
                } else {
                    holder.tvOriginalPrice.visibility = View.VISIBLE
                    holder.showDiscount.visibility = View.VISIBLE
                    holder.tvSavePrice.visibility = View.VISIBLE
                }
            } else {
                holder.lytTimer.visibility = View.VISIBLE
                //                Variants variants = product.getVariants().get(0);
                val flashSale = variants.flash_sales[0]
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
                        holder.tvSavePrice.visibility = View.GONE
                        holder.tvPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + originalPrice)
                    } else {
                        holder.showDiscount.visibility = View.VISIBLE
                        holder.tvSavePrice.visibility = View.VISIBLE
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
                    holder.tvTimer.text = calculateDays(activity, abs(days))
                    if (variants.discounted_price == "0" || variants.discounted_price == "") {
                        holder.showDiscount.visibility = View.GONE
                        holder.tvSavePrice.visibility = View.GONE
                        holder.tvPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + originalPrice)
                    } else {
                        holder.showDiscount.visibility = View.VISIBLE
                        holder.tvSavePrice.visibility = View.VISIBLE
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
            }
            holder.tvOriginalPrice.paintFlags =
                holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvOriginalPrice.text =
                session.getData(Constant.currency) + stringFormat("" + originalPrice)
            holder.tvPrice.text =
                session.getData(Constant.currency) + stringFormat("" + if (discountedPrice == 0.0) originalPrice else discountedPrice)
            holder.showDiscount.text = "-" + getDiscount(originalPrice, discountedPrice)
            holder.tvSavePrice.text =
                activity.getString(R.string.you_save) + session.getData(
                    Constant.currency
                ) + stringFormat("" + (originalPrice - discountedPrice))
            if (variants.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                holder.tvStatus.visibility = View.VISIBLE
                holder.lytQuantity.visibility = View.GONE
            } else {
                holder.tvStatus.visibility = View.GONE
                holder.lytQuantity.visibility = View.VISIBLE
            }
            if (isLogin) {
                if (Constant.CartValues.containsKey(variants.id)) {
                    holder.tvQuantity.text = "" + Constant.CartValues[variants.id]
                } else {
                    holder.tvQuantity.text = variants.cart_count
                }
                if (variants.cart_count == "0") {
                    holder.btnAddToCart.visibility = View.VISIBLE
                } else {
                    if (session.getData(Constant.STATUS) == "1") {
                        holder.btnAddToCart.visibility = View.GONE
                    } else {
                        holder.btnAddToCart.visibility = View.VISIBLE
                    }
                }
            } else {
                if (databaseHelper.checkCartItemExist(
                        variants.id,
                        variants.product_id
                    ) == "0"
                ) {
                    holder.btnAddToCart.visibility = View.VISIBLE
                } else {
                    holder.btnAddToCart.visibility = View.GONE
                }
                holder.tvQuantity.text =
                    databaseHelper.checkCartItemExist(variants.id, variants.product_id)
            }
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
                if (hashMap[variants.product_id]!! >= unit) {
                    val stock = hashMap[variants.product_id]?.minus(unit)!!
                    hashMap.replace(variants.product_id, stock)
                    if (count != 0) {
                        holder.btnAddToCart.visibility = View.GONE
                    }
                    holder.tvQuantity.text = "" + count
                    variants.cart_count = "" + count
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(variants.id)) {
                            Constant.CartValues.replace(variants.id, "" + count)
                        } else {
                            Constant.CartValues[variants.id] = "" + count
                        }
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                    } else {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(
                            variants.id,
                            variants.product_id,
                            "" + count
                        )
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    val countList1 = ArrayList<Cart>()
                    val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart11 in Constant.countList) {
                        if (cart11.product_id == cart1.product_id && cart11.product_variant_id == cart1.product_variant_id) {
                            cart1.qty = cart1.qty
                        } else {
                            countList1.add(
                                Cart(
                                    cart1.product_id,
                                    cart1.product_variant_id,
                                    cart1.qty
                                )
                            )
                        }
                    }
                    Constant.countList.addAll(countList1)
                    variants.cart_count = "" + count
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
            val stock = hashMap[variants.product_id]?.plus(unit)!!
            if (count == 0) {
                holder.btnAddToCart.visibility = View.VISIBLE
            }
            hashMap.replace(variants.product_id, stock)
            if (isLogin) {
                holder.tvQuantity.text = "" + count
                variants.cart_count = "" + count
                addMultipleProductInCart(session, activity, Constant.CartValues)
                if (count > 0) {
                    if (Constant.CartValues.containsKey(variants.id)) {
                        Constant.CartValues.replace(variants.id, "" + count)
                    } else {
                        Constant.CartValues[variants.id] = "" + count
                    }
                }
            } else {
                holder.tvQuantity.text = "" + count
                databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                databaseHelper.getTotalItemOfCart(activity)
            }
            val countList1 = ArrayList<Cart>()
            val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
            for (cart11 in Constant.countList) {
                if (cart11.product_id == cart1.product_id && cart11.product_variant_id == cart1.product_variant_id) {
                    cart1.qty = cart1.qty
                } else {
                    countList1.add(
                        Cart(
                            cart1.product_id,
                            cart1.product_variant_id,
                            cart1.qty
                        )
                    )
                }
            }
            Constant.countList.addAll(countList1)
            variants.cart_count = "" + count
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addToCartClickEvent(
        holder: ItemHolder,
        variants: Variants,
        maxCartCont: String
    ) {
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
                        databaseHelper.addToCart(
                            variants.id,
                            variants.product_id,
                            "" + count
                        )
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    val countList1 = ArrayList<Cart>()
                    val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart11 in Constant.countList) {
                        if (cart11.product_id == cart1.product_id && cart11.product_variant_id == cart1.product_variant_id) {
                            cart1.qty = cart1.qty
                        } else {
                            countList1.add(
                                Cart(
                                    cart1.product_id,
                                    cart1.product_variant_id,
                                    cart1.qty
                                )
                            )
                        }
                    }
                    Constant.countList.addAll(countList1)
                    variants.cart_count = "" + count
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
                        databaseHelper.addToCart(
                            variants.id,
                            variants.product_id,
                            "" + count
                        )
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    val countList1 = ArrayList<Cart>()
                    val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart11 in Constant.countList) {
                        if (cart11.product_id == cart1.product_id && cart11.product_variant_id == cart1.product_variant_id) {
                            cart1.qty = cart1.qty
                        } else {
                            countList1.add(
                                Cart(
                                    cart1.product_id,
                                    cart1.product_variant_id,
                                    cart1.qty
                                )
                            )
                        }
                    }
                    Constant.countList.addAll(countList1)
                    variants.cart_count = "" + count
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

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnAddQty: ImageButton = itemView.findViewById(R.id.btnAddQty)
        val btnMinusQty: ImageButton = itemView.findViewById(R.id.btnMinusQty)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvMeasurement: TextView = itemView.findViewById(R.id.tvMeasurement)
        val showDiscount: TextView = itemView.findViewById(R.id.showDiscount)
        val tvTimerTitle: TextView = itemView.findViewById(R.id.tvTimerTitle)
        val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val imgFav: ImageView = itemView.findViewById(R.id.imgFav)
        val imgIndicator: ImageView = itemView.findViewById(R.id.imgIndicator)
        val lytSpinner: RelativeLayout = itemView.findViewById(R.id.lytSpinner)
        val lytMain: CardView = itemView.findViewById(R.id.lytMain)
        val spinner: AppCompatSpinner = itemView.findViewById(R.id.spinner)
        val lytQuantity: RelativeLayout = itemView.findViewById(R.id.lytQuantity)
        val lytTimer: RelativeLayout = itemView.findViewById(R.id.lytTimer)
        val lottieAnimationView: LottieAnimationView =
            itemView.findViewById(R.id.lottieAnimationView)
        val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)
        val ratingProduct: RatingBar = itemView.findViewById(R.id.ratingProduct)
        val tvRatingCount: TextView = itemView.findViewById(R.id.tvRatingCount)
        val lytRatings: LinearLayout = itemView.findViewById(R.id.lytRatings)
        val tvTimer: TextView = itemView.findViewById(R.id.tvTimer)
        val tvSavePrice: TextView = itemView.findViewById(R.id.tvSavePrice)

        init {
            lottieAnimationView.setAnimation("add_to_wish_list.json")
        }
    }

    private fun startTimer(holder: ItemHolder, variants: Variants, duration: Long) {
        try {
            object : CountDownTimer(duration, 1000) {
                override fun onTick(different: Long) {
                    val seconds = (different / 1000).toInt() % 60
                    val minutes = (different / (1000 * 60) % 60).toInt()
                    val hours = (different / (1000 * 60 * 60) % 24).toInt()
                    holder.tvTimer.text =
                        setFormatTime(hours.toLong()) + ":" + setFormatTime(minutes.toLong()) + ":" + setFormatTime(
                            seconds.toLong()
                        )
                }

                override fun onFinish() {
                    if (!variants.flash_sales[0].is_start) {
                        variants.flash_sales[0].is_start = true
                        variants.is_flash_sales = "true"
                    } else {
                        variants.is_flash_sales = "false"
                    }
                    ProductListFragment.productLoadMoreAdapter.notifyDataSetChanged()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}