package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setFormatTime
import wrteam.ecart.shop.model.Cart
import wrteam.ecart.shop.model.ProductList
import wrteam.ecart.shop.model.Variants
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n", "NotifyDataSetChanged")
class ProductLoadMoreAdapter1(
    val activity: Activity,
    val productArrayList: ArrayList<ProductList?>,
    val resource: Int,
    from: String,
    hashMap: HashMap<String, Long>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val VIEW_TYPE_ITEM = 0
    val VIEW_TYPE_LOADING = 1
    val session: Session
    val isLogin: Boolean
    val databaseHelper: DatabaseHelper
    val from: String
    var isFavorite = false
    var hashMap: HashMap<String, Long>

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                view = LayoutInflater.from(activity).inflate(resource, parent, false)
                ItemHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                view =
                    LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false)
                ViewHolderLoading(view)
            }
            else -> throw IllegalArgumentException("unexpected viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val holder = holderParent
            holder.setIsRecyclable(false)
            try {
                val product = productArrayList[position]
                val variants = product!!.variants
                if (variants.size == 1) {
                    holder.spinner.visibility = View.GONE
                    holder.lytSpinner.visibility = View.GONE
                }
                if (product.indicator != "0") {
                    holder.imgIndicator.visibility = View.VISIBLE
                    if (product.indicator == "1") holder.imgIndicator.setImageResource(R.drawable.ic_veg_icon) else if (product.indicator == "2") holder.imgIndicator.setImageResource(
                        R.drawable.ic_non_veg_icon
                    )
                }
                holder.tvProductName.text = Html.fromHtml(product.name, 0)
                if (session.getData(Constant.ratings) == "1") {
                    holder.lytRatings.visibility = View.VISIBLE
                    holder.tvRatingCount.text = "(" + product.number_of_ratings + ")"
                    holder.ratingProduct.rating = product.ratings.toFloat()
                } else {
                    holder.lytRatings.visibility = View.GONE
                }
                val customAdapter: CustomAdapter = CustomAdapter(
                    activity, variants, holder, product
                )
                holder.spinner.adapter = customAdapter
                Picasso.get().load(product.image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProduct)
                holder.lytMain.setOnClickListener { v: View? ->
                    if (Constant.CartValues.size != 0) ApiConfig.addMultipleProductInCart(
                        session,
                        activity, Constant.CartValues
                    )
                    val activity1 = activity as AppCompatActivity
                    val fragment: Fragment = ProductDetailFragment()
                    val bundle = Bundle()
                    bundle.putInt(
                        "variantsPosition",
                        if (variants.size == 1) 0 else holder.spinner.selectedItemPosition
                    )
                    bundle.putString("id", product.id)
                    bundle.putString(Constant.FROM, from)
                    bundle.putInt("position", position)
                    fragment.arguments = bundle
                    activity1.supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment).addToBackStack(null).commit()
                }
                if (isLogin) {
                    holder.tvQuantity.text = variants[0].cart_count
                    if (product.is_favorite) {
                        holder.imgFav.setImageResource(R.drawable.ic_is_favorite)
                    } else {
                        holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                    }
                    val session = Session(
                        activity
                    )
                    holder.imgFav.setOnClickListener { v: View? ->
                        isFavorite = product.is_favorite
                        if (from != "favorite") {
                            if (isFavorite) {
                                isFavorite = false
                                holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                                holder.lottieAnimationView.visibility = View.GONE
                            } else {
                                isFavorite = true
                                holder.lottieAnimationView.visibility = View.VISIBLE
                                holder.lottieAnimationView.playAnimation()
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
                    holder.tvQuantity.setText(
                        databaseHelper.checkCartItemExist(
                            product.variants[0].id,
                            product.id
                        )
                    )
                    if (databaseHelper.getFavoriteById(product.id)) {
                        holder.imgFav.setImageResource(R.drawable.ic_is_favorite)
                    } else {
                        holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                    }
                    holder.imgFav.setOnClickListener { v: View? ->
                        isFavorite = databaseHelper.getFavoriteById(product.id)
                        databaseHelper.addOrRemoveFavorite(
                            product.variants[0].product_id,
                            isFavorite
                        )
                        if (from != "favorite") {
                            if (isFavorite) {
                                isFavorite = false
                                holder.imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                                holder.lottieAnimationView.visibility = View.GONE
                            } else {
                                isFavorite = true
                                holder.lottieAnimationView.visibility = View.VISIBLE
                                holder.lottieAnimationView.playAnimation()
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
                SetSelectedData(holder, variants[0], product)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (holderParent is ViewHolderLoading) {
            val loadingViewHolder: ViewHolderLoading =
                holderParent
            loadingViewHolder.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return productArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (productArrayList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun getItemId(position: Int): Long {
        val product = productArrayList[position]
        return product?.id?.toInt()?.toLong() ?: position.toLong()
    }

    @SuppressLint("SetTextI18n")
    fun SetSelectedData(holder: ItemHolder, variants: Variants, product: ProductList?) {

//        GST_Amount (Original Cost x GST %)/100
//        Net_Price Original Cost + GST Amount
        try {
            holder.tvMeasurement.text = variants.measurement + variants.measurement_unit_name
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                if (Constant.CartValues.containsKey(variants.id)) {
                    holder.tvQuantity.text = "" + Constant.CartValues[variants.id]
                }
            } else {
                if (session.getData(variants.id) != null) {
                    holder.tvQuantity.text = session.getData(variants.id)
                } else {
                    holder.tvQuantity.text = variants.cart_count
                }
            }
            var OriginalPrice: Double
            var DiscountedPrice: Double
            var taxPercentage = "0"
            try {
                taxPercentage =
                    if (product!!.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            DiscountedPrice =
                (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            OriginalPrice =
                (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            if (variants.flash_sales.equals("false")) {
                holder.lytTimer.visibility = View.GONE
                DiscountedPrice =
                    (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                OriginalPrice =
                    (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                if (variants.discounted_price == "0" || variants.discounted_price == "") {
                    holder.tvOriginalPrice.visibility = View.GONE
                    holder.showDiscount.visibility = View.GONE
                    holder.tvSavePrice.visibility = View.GONE
                    holder.tvPrice.text =
                        session.getData(Constant.currency) + ApiConfig.stringFormat("" + OriginalPrice)
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
                    startDate = df.parse(session.getData(Constant.current_date))
                    endDate = df.parse(flashSale.end_date)
                    different = (endDate?.time ?: 0) - (startDate?.time ?: 0)
                    val days = different / (60 * 60 * 24 * 1000)
                    if (setFormatTime(days).equals("00", ignoreCase = true)) {
                        StartTimer(holder, variants, different)
                    } else {
                        holder.tvTimer.setText(ApiConfig.calculateDays(activity, days))
                    }
                    holder.tvTimerTitle.text = activity.getString(R.string.ends_in)
                    if (flashSale.discounted_price == "0" || flashSale.discounted_price == "") {
                        holder.showDiscount.visibility = View.GONE
                        holder.tvSavePrice.visibility = View.GONE
                        holder.tvPrice.text =
                            session.getData(Constant.currency) + ApiConfig.stringFormat("" + OriginalPrice)
                    } else {
                        holder.showDiscount.visibility = View.VISIBLE
                        holder.tvSavePrice.visibility = View.VISIBLE
                        DiscountedPrice =
                            (flashSale.discounted_price.toFloat() + flashSale.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        OriginalPrice =
                            (flashSale.price.toFloat() + flashSale.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        holder.tvOriginalPrice.paintFlags =
                            holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        holder.tvOriginalPrice.text =
                            session.getData(Constant.currency) + ApiConfig.stringFormat("" + OriginalPrice)
                        holder.tvPrice.text =
                            session.getData(Constant.currency) + ApiConfig.stringFormat("" + DiscountedPrice)
                    }
                } else {
                    startDate = df.parse(session.getData(Constant.current_date))
                    endDate = df.parse(flashSale.start_date)
                    different = (endDate?.time ?: 0) - (startDate?.time ?: 0)
                    val days = different / (60 * 60 * 24 * 1000)
                    if (setFormatTime(days).equals("00", ignoreCase = true)) {
                        StartTimer(holder, variants, different)
                    } else {
                        holder.tvTimer.setText(ApiConfig.calculateDays(activity, days))
                    }
                    holder.tvTimerTitle.text = activity.getString(R.string.starts_in)
                    holder.tvTimer.setText(ApiConfig.calculateDays(activity, Math.abs(days)))
                    if (variants.discounted_price == "0" || variants.discounted_price == "") {
                        holder.showDiscount.visibility = View.GONE
                        holder.tvSavePrice.visibility = View.GONE
                        holder.tvPrice.text =
                            session.getData(Constant.currency) + ApiConfig.stringFormat("" + OriginalPrice)
                    } else {
                        holder.showDiscount.visibility = View.VISIBLE
                        holder.tvSavePrice.visibility = View.VISIBLE
                        DiscountedPrice =
                            (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        OriginalPrice =
                            (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        holder.tvOriginalPrice.paintFlags =
                            holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        holder.tvOriginalPrice.text =
                            session.getData(Constant.currency) + ApiConfig.stringFormat("" + OriginalPrice)
                        holder.tvPrice.text =
                            session.getData(Constant.currency) + ApiConfig.stringFormat("" + DiscountedPrice)
                    }
                }
                holder.showDiscount.text =
                    "-" + ApiConfig.getDiscount(OriginalPrice, DiscountedPrice)
            }
            holder.tvOriginalPrice.paintFlags =
                holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvOriginalPrice.text =
                session.getData(Constant.currency) + ApiConfig.stringFormat("" + OriginalPrice)
            holder.tvPrice.text =
                session.getData(Constant.currency) + ApiConfig.stringFormat("" + if (DiscountedPrice == 0.0) OriginalPrice else DiscountedPrice)
            holder.showDiscount.text = "-" + ApiConfig.getDiscount(OriginalPrice, DiscountedPrice)
            holder.tvSavePrice.text =
                activity.getString(R.string.you_save) + session.getData(Constant.currency) + ApiConfig.stringFormat(
                    "" + (OriginalPrice - DiscountedPrice)
                )
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
                if (databaseHelper.checkCartItemExist(variants.id, variants.product_id)
                        .equals("0")
                ) {
                    holder.btnAddToCart.visibility = View.VISIBLE
                } else {
                    holder.btnAddToCart.visibility = View.GONE
                }
                holder.tvQuantity.setText(
                    databaseHelper.checkCartItemExist(
                        variants.id,
                        variants.product_id
                    )
                )
            }
            val maxCartCont: String
            maxCartCont =
                if (product!!.total_allowed_quantity == null || product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                    session.getData(Constant.max_cart_items_count)
                } else {
                    product.total_allowed_quantity
                }
            val isLoose = variants.type.equals("loose", ignoreCase = true)
            if (isLoose) {
                holder.btnMinusQty.setOnClickListener { view: View? ->
                    removeLooseItemFromCartClickEvent(
                        holder,
                        variants
                    )
                }
                holder.btnAddQty.setOnClickListener { view: View? ->
                    addLooseItemToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
                holder.btnAddToCart.setOnClickListener { v: View? ->
                    addLooseItemToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
            } else {
                holder.btnMinusQty.setOnClickListener { view: View? ->
                    removeFromCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
                holder.btnAddQty.setOnClickListener { view: View? ->
                    addToCartClickEvent(
                        holder,
                        variants,
                        maxCartCont
                    )
                }
                holder.btnAddToCart.setOnClickListener { v: View? ->
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

    fun addLooseItemToCartClickEvent(holder: ItemHolder, variants: Variants, maxCartCont: String) {
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
                    val stock = hashMap[variants.product_id]!! - unit
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
                        ApiConfig.addMultipleProductInCart(session, activity, Constant.CartValues)
                    } else {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    val countList_ = ArrayList<Cart>()
                    val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart_ in Constant.countList) {
                        if (cart1.product_id == cart_.product_id && cart1.product_variant_id == cart_.product_variant_id) {
                            cart_.qty = cart1.qty
                        } else {
                            countList_.add(
                                Cart(
                                    cart1.product_id,
                                    cart1.product_variant_id,
                                    cart1.qty
                                )
                            )
                        }
                    }
                    Constant.countList.addAll(countList_)
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

    fun removeLooseItemFromCartClickEvent(holder: ItemHolder, variants: Variants) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = variants.measurement.toDouble().toLong() * unitMeasurement
        if (session.getData(Constant.STATUS) == "1") {
            var count = holder.tvQuantity.text.toString().toInt()
            count--
            val stock = hashMap[variants.product_id]!! + unit
            if (count == 0) {
                holder.btnAddToCart.visibility = View.VISIBLE
            }
            hashMap.replace(variants.product_id, stock)
            if (isLogin) {
                holder.tvQuantity.text = "" + count
                variants.cart_count = "" + count
                ApiConfig.addMultipleProductInCart(session, activity, Constant.CartValues)
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
            val countList_ = ArrayList<Cart>()
            val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
            for (cart_ in Constant.countList) {
                if (cart1.product_id == cart_.product_id && cart1.product_variant_id == cart_.product_variant_id) {
                    cart_.qty = cart1.qty
                } else {
                    countList_.add(Cart(cart1.product_id, cart1.product_variant_id, cart1.qty))
                }
            }
            Constant.countList.addAll(countList_)
            variants.cart_count = "" + count
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun addToCartClickEvent(holder: ItemHolder, variants: Variants, maxCartCont: String) {
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
                        ApiConfig.addMultipleProductInCart(session, activity, Constant.CartValues)
                    } else {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    val countList_ = ArrayList<Cart>()
                    val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart_ in Constant.countList) {
                        if (cart1.product_id == cart_.product_id && cart1.product_variant_id == cart_.product_variant_id) {
                            cart_.qty = cart1.qty
                        } else {
                            countList_.add(
                                Cart(
                                    cart1.product_id,
                                    cart1.product_variant_id,
                                    cart1.qty
                                )
                            )
                        }
                    }
                    Constant.countList.addAll(countList_)
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

    fun removeFromCartClickEvent(holder: ItemHolder, variants: Variants, maxCartCont: String) {
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
                            ApiConfig.addMultipleProductInCart(
                                session,
                                activity,
                                Constant.CartValues
                            )
                        }
                    } else {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                        databaseHelper.getTotalItemOfCart(activity)
                    }
                    val countList_ = ArrayList<Cart>()
                    val cart1 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart_ in Constant.countList) {
                        if (cart1.product_id == cart_.product_id && cart1.product_variant_id == cart_.product_variant_id) {
                            cart_.qty = cart1.qty
                        } else {
                            countList_.add(
                                Cart(
                                    cart1.product_id,
                                    cart1.product_variant_id,
                                    cart1.qty
                                )
                            )
                        }
                    }
                    Constant.countList.addAll(countList_)
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

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar

        init {
            progressBar = view.findViewById(R.id.itemProgressbar)
        }
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnAddQty: ImageButton
        val btnMinusQty: ImageButton
        val tvProductName: TextView
        val tvPrice: TextView
        val tvQuantity: TextView
        val tvMeasurement: TextView
        val showDiscount: TextView
        val tvTimerTitle: TextView
        val tvOriginalPrice: TextView
        val tvStatus: TextView
        val imgProduct: ImageView
        val imgFav: ImageView
        val imgIndicator: ImageView
        val lytSpinner: RelativeLayout
        val lytMain: CardView
        val spinner: AppCompatSpinner
        val lytQuantity: RelativeLayout
        val lytTimer: RelativeLayout
        val lottieAnimationView: LottieAnimationView
        val btnAddToCart: Button
        val ratingProduct: RatingBar
        val tvRatingCount: TextView
        val lytRatings: LinearLayout
        val tvTimer: TextView
        val tvSavePrice: TextView
        val timer: CountDownTimer?

        init {
            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvPrice = itemView.findViewById(R.id.tvPrice)
            showDiscount = itemView.findViewById(R.id.showDiscount)
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice)
            tvTimerTitle = itemView.findViewById(R.id.tvTimerTitle)
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement)
            tvStatus = itemView.findViewById(R.id.tvStatus)
            imgProduct = itemView.findViewById(R.id.imgProduct)
            imgIndicator = itemView.findViewById(R.id.imgIndicator)
            btnAddQty = itemView.findViewById(R.id.btnAddQty)
            btnMinusQty = itemView.findViewById(R.id.btnMinusQty)
            tvQuantity = itemView.findViewById(R.id.tvQuantity)
            lytQuantity = itemView.findViewById(R.id.lytQuantity)
            lytTimer = itemView.findViewById(R.id.lytTimer)
            imgFav = itemView.findViewById(R.id.imgFav)
            lytMain = itemView.findViewById(R.id.lytMain)
            spinner = itemView.findViewById(R.id.spinner)
            lytSpinner = itemView.findViewById(R.id.lytSpinner)
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart)
            ratingProduct = itemView.findViewById(R.id.ratingProduct)
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount)
            lytRatings = itemView.findViewById(R.id.lytRatings)
            lottieAnimationView = itemView.findViewById(R.id.lottieAnimationView)
            lottieAnimationView.setAnimation("add_to_wish_list.json")
            tvTimer = itemView.findViewById(R.id.tvTimer)
            tvSavePrice = itemView.findViewById(R.id.tvSavePrice)
            timer = null
        }
    }

    inner class CustomAdapter(
        val context: Context,
        val extraList: ArrayList<Variants>,
        val holder: ItemHolder,
        val product: ProductList
    ) :
        BaseAdapter() {
        val inflter: LayoutInflater
        override fun getCount(): Int {
            return extraList.size
        }

        override fun getItem(i: Int): ProductList? {
            return productArrayList[i]
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        @SuppressLint("SetTextI18n", "ViewHolder", "InflateParams")
        override fun getView(i: Int, view: View, viewGroup: ViewGroup): View {
            var view = view
            view = inflter.inflate(R.layout.lyt_spinner_item, null)
            val measurement = view.findViewById<TextView>(R.id.tvMeasurement)
            val variants = extraList[i]
            measurement.text = variants.measurement + " " + variants.measurement_unit_name
            if (variants.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                measurement.setTextColor(ContextCompat.getColor(context, R.color.red))
            } else {
                measurement.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            holder.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    val variants = extraList[i]
                    SetSelectedData(holder, variants, product)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
            return view
        }

        init {
            inflter = LayoutInflater.from(context)
        }
    }

    fun StartTimer(holder: ItemHolder, variants: Variants, duration: Long) {
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

    init {
        this.hashMap = HashMap()
        this.from = from
        this.hashMap = hashMap
        session = Session(activity)
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
        Constant.CartValues = HashMap()
        databaseHelper = DatabaseHelper(activity)
    }
}