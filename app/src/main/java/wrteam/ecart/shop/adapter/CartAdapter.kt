package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.CartFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInSaveForLater
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Cart

@SuppressLint("NotifyDataSetChanged")
class CartAdapter(
    val activity: Activity,
    private var carts: ArrayList<Cart>,
    private var saveForLater: ArrayList<Cart>,
    hashMap: HashMap<String, Long>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    val session = Session(activity)
    var isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
    var taxPercentage: String
    private var availableStock = 0.toLong()
    val databaseHelper: DatabaseHelper
    var hashMap: HashMap<String, Long>
    private fun createHashMap() {
        CartFragment.hashMap.clear()
        for (cart in carts) {
            val unitMeasurement =
                if (cart.unit.equals("kg", ignoreCase = true) || cart.unit.equals(
                        "ltr",
                        ignoreCase = true
                    )
                ) 1000 else 1.toLong()
            val unit = cart.measurement.toDouble().toLong() * unitMeasurement
            if (isLogin) {
                if (!CartFragment.hashMap.containsKey(cart.product_id)) {
                    CartFragment.hashMap[cart.product_id] =
                        (cart.stock.toDouble() * (if (cart.stock_unit_name.equals(
                                "kg",
                                ignoreCase = true
                            ) || cart.stock_unit_name.equals("ltr", ignoreCase = true)
                        ) 1000 else 1) - unit * cart.qty.toDouble()).toLong()
                } else {
                    val qty = (unit * cart.qty.toDouble()).toLong()
                    val availableStock = CartFragment.hashMap[cart.product_id]?.minus(qty)!!
                    CartFragment.hashMap.replace(cart.product_id, availableStock)
                }
            } else {
                if (!hashMap.containsKey(cart.product_id)) {
                    hashMap[cart.product_id] =
                        (cart.stock.toDouble() * (if (cart.stock_unit_name.equals(
                                "kg",
                                ignoreCase = true
                            ) || cart.stock_unit_name.equals("ltr", ignoreCase = true)
                        ) 1000 else 1) - unit * databaseHelper.checkCartItemExist(
                            cart.product_variant_id, cart.product_id
                        ).toDouble()).toLong()
                } else {
                    val qty = (unit * databaseHelper.checkCartItemExist(
                        cart.product_variant_id,
                        cart.product_id
                    ).toDouble()).toLong()
                    hashMap.replace(cart.product_id, (hashMap[cart.product_id]?.minus(qty)!!))
                }
            }
        }
    }

    fun add(cart: Cart) {
        if (isLogin) {
            Constant.FLOAT_TOTAL_AMOUNT = 0.0
            carts.add(cart)
            for (cart1 in Constant.countList) {
                if (cart1 == cart) cart.qty = cart1.qty
            }
            createHashMap()
            CartFragment.variantIdList.add(cart.product_variant_id)
            CartFragment.qtyList.add(cart.qty)
            CartFragment.setData(activity)
            CartFragment.values[cart.product_variant_id] = cart.qty
            if (carts.size != 0) {
                CartFragment.lytTotal.visibility = View.VISIBLE
            } else {
                CartFragment.lytTotal.visibility = View.GONE
            }
            notifyDataSetChanged()
        } else {
            run {
                Constant.FLOAT_TOTAL_AMOUNT = 0.0
                createHashMap()
                val unitMeasurement =
                    if (cart.type.equals("kg", ignoreCase = true) || cart.type.equals(
                            "ltr",
                            ignoreCase = true
                        )
                    ) 1000 else 1.toLong()
                val unit = cart.measurement.toDouble().toLong() * unitMeasurement
                if (!hashMap.containsKey(cart.product_id)) {
                    hashMap[cart.product_id] =
                        (cart.stock.toDouble() * (if (cart.stock_unit_name.equals(
                                "kg",
                                ignoreCase = true
                            ) || cart.stock_unit_name.equals("ltr", ignoreCase = true)
                        ) 1000 else 1) - unit * databaseHelper.checkCartItemExist(
                            cart.product_variant_id, cart.product_id
                        ).toDouble()).toLong()
                } else {
                    if (hashMap[cart.product_id]!! >= unit) {
                        cart.serve_for = Constant.AVAILABLE
                        hashMap.replace(
                            cart.product_id,
                            (cart.stock.toDouble() * (if (cart.stock_unit_name.equals(
                                    "kg",
                                    ignoreCase = true
                                ) || cart.stock_unit_name.equals("ltr", ignoreCase = true)
                            ) 1000 else 1) - unit * databaseHelper.checkCartItemExist(
                                cart.product_variant_id, cart.product_id
                            ).toDouble()).toLong()
                        )
                    } else {
                        cart.serve_for = Constant.SOLD_OUT_TEXT
                    }
                }
                carts.add(cart)
                notifyDataSetChanged()
            }
        }
    }

    private fun removeItem(position: Int) {
        if (isLogin) {
            Constant.FLOAT_TOTAL_AMOUNT = 0.0
            val cart = carts[position]
            CartFragment.qtyList.removeAt(CartFragment.variantIdList.indexOf(cart.product_variant_id))
            CartFragment.variantIdList.remove(cart.product_variant_id)
            for ((index, cart1) in Constant.countList.withIndex()) {
                if (cart1.product_id == cart.product_id && cart1.product_variant_id == cart.product_variant_id) Constant.countList[index].qty =
                    "0"
            }
            CartFragment.setData(activity)
            if (CartFragment.values.containsKey(cart.product_variant_id)) {
                CartFragment.values.replace(cart.product_variant_id, "0")
            } else {
                CartFragment.values[cart.product_variant_id] = "0"
            }
            carts.remove(cart)
            createHashMap()
            CartFragment.isSoldOut = false
            Constant.TOTAL_CART_ITEM = itemCount
            CartFragment.setData(activity)
            activity.invalidateOptionsMenu()
            if (itemCount == 0 && saveForLater.size == 0) {
                CartFragment.lytEmpty.visibility = View.VISIBLE
                CartFragment.lytTotal.visibility = View.GONE
            } else {
                CartFragment.lytEmpty.visibility = View.GONE
                CartFragment.lytTotal.visibility = View.VISIBLE
            }
            notifyDataSetChanged()
            showUndoSnackBar(cart)
        } else {
            val cart = carts[position]
            hashMap.remove(cart.product_id)
            showUndoSnackBar(cart)
            carts.remove(cart)
            createHashMap()
            databaseHelper.removeFromCart(cart.product_variant_id, cart.product_id)
            Constant.FLOAT_TOTAL_AMOUNT = 0.0
            databaseHelper.getTotalItemOfCart(activity)
            notifyDataSetChanged()
            CartFragment.setData(activity)
            if (itemCount == 0 && saveForLater.size == 0) {
                CartFragment.lytEmpty.visibility = View.VISIBLE
                CartFragment.lytTotal.visibility = View.GONE
            } else {
                CartFragment.lytEmpty.visibility = View.GONE
                CartFragment.lytTotal.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun moveItem(position: Int) {
        val cart = carts[position]
        if (isLogin) {
            try {
                Constant.FLOAT_TOTAL_AMOUNT = 0.0
                CartFragment.qtyList.removeAt(CartFragment.variantIdList.indexOf(cart.product_variant_id))
                CartFragment.variantIdList.remove(cart.product_variant_id)
                CartFragment.setData(activity)
                carts.remove(cart)
                createHashMap()
                saveForLater.add(cart)
                CartFragment.saveForLaterAdapter.notifyDataSetChanged()
                if (CartFragment.lytSaveForLater.visibility == View.GONE) CartFragment.lytSaveForLater.visibility =
                    View.VISIBLE
                CartFragment.tvSaveForLaterTitle.text =
                    activity.resources.getString(R.string.save_for_later) + " (" + saveForLater.size + ")"
                CartFragment.saveForLaterValues[cart.product_variant_id] = cart.qty
                Constant.TOTAL_CART_ITEM = itemCount
                CartFragment.setData(activity)
                if (itemCount == 0) CartFragment.lytTotal.visibility = View.GONE
                addMultipleProductInSaveForLater(session, activity, CartFragment.saveForLaterValues)
                notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                Constant.FLOAT_TOTAL_AMOUNT = 0.0
                hashMap.remove(cart.product_id)
                databaseHelper.moveToCartOrSaveForLater(
                    cart.product_variant_id,
                    cart.product_id,
                    "cart",
                    activity
                )
                carts.remove(cart)
                createHashMap()
                CartFragment.saveForLaterAdapter.add(0, cart)
                if (saveForLater.size > 0) CartFragment.lytSaveForLater.visibility = View.VISIBLE
                if (itemCount == 0) CartFragment.lytTotal.visibility = View.GONE
                notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_cartlist, parent, false)
        )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            viewTypeItem -> {
                val holder = holderParent as ItemHolder
                val cart = carts[position]
                if (!isLogin) {
                    cart.qty =
                        databaseHelper.checkCartItemExist(cart.product_variant_id, cart.product_id)
                }
                val maxCartCont: String =
                    if (cart.total_allowed_quantity == "" || cart.total_allowed_quantity == "0") {
                        session.getData(Constant.max_cart_items_count)
                    } else {
                        cart.total_allowed_quantity
                    }
                val price: Double
                val oPrice: Double
                val qty = cart.qty.toInt()
                try {
                    taxPercentage =
                        if (cart.tax_percentage.toDouble() > 0) cart.tax_percentage else "0"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Picasso.get()
                    .load(if (cart.image == "") "-" else cart.image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProduct)
                holder.tvDelete.setOnClickListener { removeItem(position) }
                holder.tvAction.setOnClickListener { moveItem(position) }
                holder.tvProductName.text = cart.name
                holder.tvMeasurement.text = cart.measurement + "\u0020" + cart.unit
                if (cart.serve_for == Constant.SOLD_OUT_TEXT) {
                    holder.tvStatus.visibility = View.VISIBLE
                    holder.lytQuantity.visibility = View.GONE
                    CartFragment.isSoldOut = true
                } else if (cart.qty.toFloat() > maxCartCont.toFloat()) {
                    holder.tvStatus.visibility = View.VISIBLE
                    holder.tvStatus.text =
                        activity.getString(R.string.low_stock_warning1) + cart.stock + activity.getString(
                            R.string.low_stock_warning2
                        )
                    CartFragment.isSoldOut = true
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
                holder.tvQuantity.text = cart.qty
                holder.tvTotalPrice.text =
                    session.getData(Constant.currency) + stringFormat("" + price * qty)
                val isLoose = cart.type.equals("loose", ignoreCase = true)
                if (isLoose) {
                    holder.btnMinusQty.setOnClickListener {
                        removeLooseItemFromCartClickEvent(
                            holder,
                            cart
                        )
                    }
                    holder.btnAddQty.setOnClickListener {
                        addLooseItemToCartClickEvent(
                            holder,
                            maxCartCont,
                            cart
                        )
                    }
                } else {
                    holder.btnMinusQty.setOnClickListener {
                        removeFromCartClickEvent(
                            holder,
                            cart,
                            maxCartCont
                        )
                    }
                    holder.btnAddQty.setOnClickListener {
                        addToCartClickEvent(
                            holder,
                            cart,
                            maxCartCont
                        )
                    }
                }
                Constant.FLOAT_TOTAL_AMOUNT += price * qty
                CartFragment.setData(activity)
            }
            viewTypeLoading -> {
                val loadingViewHolder = holderParent as ViewHolderLoading
                loadingViewHolder.progressBar.isIndeterminate = true
            }
        }
    }

    private fun addLooseItemToCartClickEvent(holder: ItemHolder, maxCartCont: String, cart: Cart) {
        if (isLogin) {
            availableStock = CartFragment.hashMap[cart.product_id].toString().toLong()
            val unitMeasurement = if (cart.unit.equals("kg", ignoreCase = true) || cart.unit.equals(
                    "ltr",
                    ignoreCase = true
                )
            ) 1000 else 1.toLong()
            val unit = cart.measurement.toDouble().toLong() * unitMeasurement
            if (session.getData(Constant.STATUS) == "1") {
                var count = holder.tvQuantity.text.toString().toInt()
                count++
                if (count <= maxCartCont.toInt()) {
                    if (availableStock >= unit) {
                        availableStock -= unit
                        holder.tvQuantity.text = "" + count
                        cart.qty = "" + count
                        CartFragment.hashMap.replace(cart.product_id, availableStock)
                        if (Constant.CartValues.containsKey(cart.product_variant_id)) {
                            Constant.CartValues.replace(cart.product_variant_id, "" + count)
                        } else {
                            Constant.CartValues[cart.product_variant_id] = "" + count
                        }
                        val countList1 = ArrayList<Cart>()
                        val cart1 = Cart(cart.product_id, cart.product_variant_id, cart.qty)
                        for (cart1 in Constant.countList) {
                            if (cart1.product_id == cart1.product_id && cart1.product_variant_id == cart1.product_variant_id) {
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
                        if (CartFragment.variantIdList.contains(cart.product_variant_id)) {
                            CartFragment.qtyList[CartFragment.variantIdList.indexOf(cart.product_variant_id)] =
                                "" + count
                        } else {
                            CartFragment.variantIdList.add(cart.product_variant_id)
                            CartFragment.qtyList.add(
                                CartFragment.variantIdList.indexOf(cart.product_variant_id),
                                "" + count
                            )
                        }
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                        Constant.FLOAT_TOTAL_AMOUNT = 0.0
                        notifyDataSetChanged()
                        CartFragment.setData(activity)
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
        } else {
            availableStock = hashMap[cart.product_id].toString().toLong()
            val unitMeasurement = if (cart.unit.equals("kg", ignoreCase = true) || cart.unit.equals(
                    "ltr",
                    ignoreCase = true
                )
            ) 1000 else 1.toLong()
            val unit = cart.measurement.toDouble().toLong() * unitMeasurement
            if (session.getData(Constant.STATUS) == "1") {
                var count = holder.tvQuantity.text.toString().toInt()
                count++
                if (count <= maxCartCont.toInt()) {
                    if (availableStock >= unit) {
                        availableStock -= unit
                        hashMap.replace(cart.product_id, availableStock)
                        databaseHelper.addToCart(
                            cart.product_variant_id,
                            cart.product_id,
                            "" + count
                        )
                        holder.tvQuantity.text = "" + count
                        Constant.FLOAT_TOTAL_AMOUNT = 0.0
                        notifyDataSetChanged()
                        CartFragment.setData(activity)
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
    }

    private fun removeLooseItemFromCartClickEvent(holder: ItemHolder, cart: Cart) {
        if (isLogin) {
            availableStock = CartFragment.hashMap[cart.product_id].toString().toLong()
            val unitMeasurement = if (cart.unit.equals("kg", ignoreCase = true) || cart.unit.equals(
                    "ltr",
                    ignoreCase = true
                )
            ) 1000 else 1.toLong()
            val unit = cart.measurement.toDouble().toLong() * unitMeasurement
            var count = holder.tvQuantity.text.toString().toInt()
            if (count > 1) {
                if (session.getData(Constant.STATUS) == "1") {
                    count--
                    availableStock += unit
                    CartFragment.hashMap.replace(cart.product_id, availableStock)
                    holder.tvQuantity.text = "" + count
                    cart.qty = "" + count
                    if (Constant.CartValues.containsKey(cart.product_variant_id)) {
                        Constant.CartValues.replace(cart.product_variant_id, "" + count)
                    } else {
                        Constant.CartValues[cart.product_variant_id] = "" + count
                    }
                    val countList1 = ArrayList<Cart>()
                    val cart1 = Cart(cart.product_id, cart.product_variant_id, cart.qty)
                    for (cart1 in Constant.countList) {
                        if (cart1.product_id == cart1.product_id && cart1.product_variant_id == cart1.product_variant_id) {
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
                    CartFragment.qtyList[CartFragment.variantIdList.indexOf(cart.product_variant_id)] =
                        "" + count
                    addMultipleProductInCart(session, activity, Constant.CartValues)
                    Constant.FLOAT_TOTAL_AMOUNT = 0.0
                    notifyDataSetChanged()
                    CartFragment.setData(activity)
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.user_deactivate_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            availableStock = hashMap[cart.product_id].toString().toLong()
            val unitMeasurement = if (cart.unit.equals("kg", ignoreCase = true) || cart.unit.equals(
                    "ltr",
                    ignoreCase = true
                )
            ) 1000 else 1.toLong()
            val unit = cart.measurement.toDouble().toLong() * unitMeasurement
            var count = holder.tvQuantity.text.toString().toInt()
            if (count > 1) {
                if (session.getData(Constant.STATUS) == "1") {
                    count--
                    if (count > 0) {
                        availableStock += unit
                        hashMap.replace(cart.product_id, availableStock)
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(
                            cart.product_variant_id,
                            cart.product_id,
                            "" + count
                        )
                    }
                    Constant.FLOAT_TOTAL_AMOUNT = 0.0
                    notifyDataSetChanged()
                    CartFragment.setData(activity)
                } else {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.user_deactivate_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addToCartClickEvent(holder: ItemHolder, cart: Cart, maxCartCont: String) {
        if (isLogin) {
            if (session.getData(Constant.STATUS) == "1") {
                var count = holder.tvQuantity.text.toString().toInt()
                count++
                if (count < cart.stock.toFloat()) {
                    if (count < maxCartCont.toInt()) {
                        holder.tvQuantity.text = "" + count
                        cart.qty = "" + count
                        if (Constant.CartValues.containsKey(cart.product_variant_id)) {
                            Constant.CartValues.replace(cart.product_variant_id, "" + count)
                        } else {
                            Constant.CartValues[cart.product_variant_id] = "" + count
                        }
                        val countList1 = ArrayList<Cart>()
                        val cart1 = Cart(cart.product_id, cart.product_variant_id, cart.qty)
                        for (cart1 in Constant.countList) {
                            if (cart1.product_id == cart1.product_id && cart1.product_variant_id == cart1.product_variant_id) {
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
                        if (CartFragment.variantIdList.contains(cart.product_variant_id)) {
                            CartFragment.qtyList[CartFragment.variantIdList.indexOf(cart.product_variant_id)] =
                                "" + count
                        } else {
                            CartFragment.variantIdList.add(cart.product_variant_id)
                            CartFragment.qtyList.add(
                                CartFragment.variantIdList.indexOf(cart.product_variant_id),
                                "" + count
                            )
                        }
                        addMultipleProductInCart(session, activity, Constant.CartValues)
                        Constant.FLOAT_TOTAL_AMOUNT = 0.0
                        notifyDataSetChanged()
                        CartFragment.setData(activity)
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
        } else {
            if (session.getData(Constant.STATUS) == "1") {
                var count = holder.tvQuantity.text.toString().toInt()
                count++
                if (count < cart.stock.toFloat()) {
                    if (count < maxCartCont.toInt()) {
                        holder.tvQuantity.text = "" + count
                        databaseHelper.addToCart(
                            cart.product_variant_id,
                            cart.product_id,
                            "" + count
                        )
                        Constant.FLOAT_TOTAL_AMOUNT = 0.0
                        notifyDataSetChanged()
                        CartFragment.setData(activity)
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
    }

    private fun removeFromCartClickEvent(holder: ItemHolder, cart: Cart, maxCartCont: String) {
        if (isLogin) {
            var count = holder.tvQuantity.text.toString().toInt()
            if (count > 1) {
                if (session.getData(Constant.STATUS) == "1") {
                    count--
                    if (count <= cart.stock.toFloat()) {
                        if (count <= maxCartCont.toInt()) {
                            holder.tvQuantity.text = "" + count
                            cart.qty = "" + count
                            if (Constant.CartValues.containsKey(cart.product_variant_id)) {
                                Constant.CartValues.replace(cart.product_variant_id, "" + count)
                            } else {
                                Constant.CartValues[cart.product_variant_id] = "" + count
                            }
                            val countList1 = ArrayList<Cart>()
                            val cart1 = Cart(cart.product_id, cart.product_variant_id, cart.qty)
                            for (cart1 in Constant.countList) {
                                if (cart1.product_id == cart1.product_id && cart1.product_variant_id == cart1.product_variant_id) {
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
                            CartFragment.qtyList[CartFragment.variantIdList.indexOf(cart.product_variant_id)] =
                                "" + count
                            addMultipleProductInCart(session, activity, Constant.CartValues)
                            Constant.FLOAT_TOTAL_AMOUNT = 0.0
                            notifyDataSetChanged()
                            CartFragment.setData(activity)
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
        } else {
            var count = holder.tvQuantity.text.toString().toInt()
            if (count > 1) {
                if (session.getData(Constant.STATUS) == "1") {
                    count--
                    if (count <= cart.stock.toFloat()) {
                        if (count <= maxCartCont.toInt()) {
                            holder.tvQuantity.text = "" + count
                            databaseHelper.addToCart(
                                cart.product_variant_id,
                                cart.product_id,
                                "" + count
                            )
                            Constant.FLOAT_TOTAL_AMOUNT = 0.0
                            notifyDataSetChanged()
                            CartFragment.setData(activity)
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
        }
    }

    override fun getItemCount(): Int {
        return carts.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val cart = carts[position]
        return cart.product_variant_id.toInt().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val btnMinusQty: ImageView = itemView.findViewById(R.id.btnMinusQty)
        val btnAddQty: ImageView = itemView.findViewById(R.id.btnAddQty)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvMeasurement: TextView = itemView.findViewById(R.id.tvMeasurement)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
        val lytQuantity: LinearLayout = itemView.findViewById(R.id.lytQuantity)
        val lytMain: RelativeLayout = itemView.findViewById(R.id.lytMain)

    }

    private fun showUndoSnackBar(cart: Cart) {
        val snackBar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            activity.getString(R.string.undo_message),
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction(activity.getString(R.string.undo)) {
            snackBar.dismiss()
            add(cart)
            notifyDataSetChanged()
            CartFragment.setData(activity)
            CartFragment.isSoldOut = false
            Constant.TOTAL_CART_ITEM = itemCount
            CartFragment.setData(activity)
            if (itemCount != 0) {
                CartFragment.lytTotal.visibility = View.VISIBLE
                CartFragment.lytEmpty.visibility = View.GONE
            }
            addMultipleProductInCart(session, activity, CartFragment.values)
            activity.invalidateOptionsMenu()
        }
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 5
        snackBar.show()
    }

    init {
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
        taxPercentage = "0"
        databaseHelper = DatabaseHelper(activity)
        this.hashMap = hashMap
    }
}