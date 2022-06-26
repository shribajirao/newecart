package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.fragment.OrderDetailFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.ApiConfig.Companion.toTitleCase
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.OrderItem
import wrteam.ecart.shop.model.OrderList
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class OrderItemsAdapter(val activity: Activity, val orderList: OrderList, val from: String) :
    RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {
    val orderTrackerArrayList = orderList.items
    val session = Session(activity)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lyt_items, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val orderItem = orderTrackerArrayList[position]
            val payType = if (orderList.payment_method.equals(
                    "cod",
                    ignoreCase = true
                )
            ) activity.resources.getString(R.string.cod) else orderList.payment_method
            val activeStatus = orderList.active_status.substring(0, 1)
                .uppercase(Locale.getDefault()) + orderList.active_status.substring(1).lowercase(Locale.getDefault())
            holder.tvQuantity.text = orderItem.quantity
            val taxPercentage = orderList.tax_percentage
            val price = if (orderItem.discounted_price == "0" || orderItem.discounted_price == "") {
                (orderItem.price.toFloat() + orderItem.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            } else {
                (orderItem.discounted_price.toFloat() + orderItem.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
            }
            holder.tvPrice.text = session.getData(Constant.currency) + stringFormat("" + price)
            holder.tvPayType.text = activity.resources.getString(R.string.via) + payType
            holder.tvStatus.text = activeStatus
            if (activeStatus.equals(Constant.AWAITING_PAYMENT, ignoreCase = true)) {
                holder.tvStatus.text = activity.getString(R.string.awaiting_payment)
            }
            holder.tvStatusDate.text = orderList.date_added

            holder.tvName.text = orderItem.product_name + "(" + orderItem.measurement + orderItem.unit + ")"
            Picasso.get().load(orderItem.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgOrder)
            holder.tvCardDetail.setOnClickListener {
                val fragment: Fragment = OrderDetailFragment()
                val bundle = Bundle()
                bundle.putString("id", "")
                bundle.putSerializable("model", orderList)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            holder.btnCancelOrReturn.setOnClickListener {
                if (!activeStatus.equals("delivered", ignoreCase = true)) {
                    updateOrderStatus(
                        activity,
                        orderList,
                        orderItem,
                        Constant.CANCELLED,
                        holder,
                        from
                    )
                } else {
                    val myFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    //System.out.println (myFormat.format (date));
                    val inputString1 = orderList.order_time
                    val inputString2 = session.getData(Constant.current_date)
                    try {
                        val date1 = myFormat.parse(inputString1)
                        val date2 = myFormat.parse(inputString2)
                        assert(date1 != null)
                        assert(date2 != null)
                        val diff = date2.time - date1.time
                        // System.out.println("Days: "+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
                        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= Session(
                                activity
                            ).getData(Constant.max_product_return_days).toInt()
                        ) {
                            updateOrderStatus(
                                activity,
                                orderList,
                                orderItem,
                                Constant.RETURNED,
                                holder,
                                from
                            )
                        } else {
                            val snackBar = Snackbar.make(
                                activity.findViewById(android.R.id.content),
                                activity.resources.getString(R.string.product_return) + Session(
                                    activity
                                ).getData(Constant.max_product_return_days)
                                    .toInt() + activity.getString(R.string.day_max_limit),
                                Snackbar.LENGTH_INDEFINITE
                            )
                            snackBar.setAction(activity.resources.getString(R.string.ok)) { snackBar.dismiss() }
                            snackBar.setActionTextColor(Color.RED)
                            val snackBarView = snackBar.view
                            val textView =
                                snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            textView.maxLines = 5
                            snackBar.show()
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }
            }
            if (from == "detail") {
                if (orderList.active_status.equals(
                        "delivered",
                        ignoreCase = true
                    ) && session.getData(
                        Constant.ratings
                    ) == "1"
                ) {
                    holder.lytRatings.visibility = View.VISIBLE
                    if (orderItem.isReview_status) {
                        holder.ratingProduct.rating = orderItem.rate.toFloat()
                        holder.tvAddUpdateReview.setText(R.string.update)
                    }
                } else {
                    holder.lytRatings.visibility = View.GONE
                }
                if (orderItem.active_status.equals(
                        "cancelled",
                        ignoreCase = true
                    ) || orderItem.active_status.equals("returned", ignoreCase = true)
                ) {
                    holder.tvStatus.setTextColor(
                        ContextCompat.getColor(
                            activity, R.color.red
                        )
                    )
                    holder.tvStatus.text = toTitleCase(orderItem.active_status)
                }
                holder.ratingProduct.onRatingBarChangeListener =
                    OnRatingBarChangeListener { ratingBar: RatingBar, _: Float, _: Boolean ->
                        addUpdateReview(
                            holder,
                            orderItem,
                            ratingBar.rating,
                            orderItem.review,
                            java.lang.Boolean.parseBoolean(orderItem.return_status),
                            orderItem.product_id
                        )
                    }
                holder.tvAddUpdateReview.setOnClickListener {
                    addUpdateReview(
                        holder,
                        orderItem,
                        holder.ratingProduct.rating,
                        orderItem.review,
                        java.lang.Boolean.parseBoolean(orderItem.return_status),
                        orderItem.product_id
                    )
                }
                if (orderList.active_status.equals(
                        "cancelled",
                        ignoreCase = true
                    ) || orderList.active_status.equals(
                        "returned",
                        ignoreCase = true
                    ) || orderItem.active_status.equals(
                        "cancelled",
                        ignoreCase = true
                    ) || orderItem.active_status.equals("returned", ignoreCase = true)
                ) {
                    holder.btnCancelOrReturn.visibility = View.GONE
                } else if (orderList.active_status.equals("delivered", ignoreCase = true)) {
                    holder.btnCancelOrReturn.text = activity.getString(R.string.return_item)
                    holder.btnCancelOrReturn.visibility = if (orderItem.return_status.equals(
                            "1",
                            ignoreCase = true
                        )
                    ) View.VISIBLE else View.GONE
                } else {
                    if (orderItem.cancelable_status.equals("1", ignoreCase = true)) {
                        if (orderItem.till_status.equals("received", ignoreCase = true)) {
                            if (orderList.active_status.equals("received", ignoreCase = true)) {
                                holder.btnCancelOrReturn.text =
                                    activity.getString(R.string.cancel_item)
                                holder.btnCancelOrReturn.visibility = View.VISIBLE
                            } else {
                                holder.btnCancelOrReturn.visibility = View.GONE
                            }
                        } else if (orderItem.till_status.equals("processed", ignoreCase = true)) {
                            holder.btnCancelOrReturn.text = activity.getString(R.string.cancel_item)
                            if (orderList.active_status.equals(
                                    "received",
                                    ignoreCase = true
                                ) || orderList.active_status.equals(
                                    "processed",
                                    ignoreCase = true
                                )
                            ) {
                                holder.btnCancelOrReturn.visibility = View.VISIBLE
                            } else {
                                holder.btnCancelOrReturn.visibility = View.GONE
                            }
                        } else if (orderItem.till_status.equals("shipped", ignoreCase = true)) {
                            holder.btnCancelOrReturn.text = activity.getString(R.string.cancel_item)
                            if (orderList.active_status.equals(
                                    "received",
                                    ignoreCase = true
                                ) || orderList.active_status.equals(
                                    "processed",
                                    ignoreCase = true
                                ) || orderList.active_status.equals("shipped", ignoreCase = true)
                            ) {
                                holder.btnCancelOrReturn.visibility = View.VISIBLE
                            } else {
                                holder.btnCancelOrReturn.visibility = View.GONE
                            }
                        }
                    } else {
                        holder.btnCancelOrReturn.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOrderStatus(
        activity: Activity,
        orderList: OrderList,
        orderItem: OrderItem,
        status: String,
        holder: ViewHolder,
        from: String
    ) {
        val alertDialog = AlertDialog.Builder(activity)
        // Setting Dialog Message
        if (status == Constant.CANCELLED) {
            alertDialog.setTitle(activity.resources.getString(R.string.cancel_order))
            alertDialog.setMessage(activity.resources.getString(R.string.cancel_msg))
        } else if (status == Constant.RETURNED) {
            alertDialog.setTitle(activity.resources.getString(R.string.return_item))
            alertDialog.setMessage(activity.resources.getString(R.string.return_msg))
        }
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()

        // Setting OK Button
        alertDialog.setPositiveButton(activity.resources.getString(R.string.yes)) { _: DialogInterface, _: Int ->
            val params: MutableMap<String, String> = HashMap()
            params[Constant.UPDATE_ORDER_ITEM_STATUS] = Constant.GetVal
            params[Constant.ORDER_ITEM_ID] = orderItem.id
            params[Constant.ORDER_ID] = orderList.id
            params[Constant.STATUS] = status
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    // System.out.println("================= " + response);
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                if (status == Constant.CANCELLED) {
                                    holder.btnCancelOrReturn.visibility = View.GONE
                                    holder.tvStatus.text = status
                                    holder.tvStatus.setTextColor(Color.RED)
                                    if (from == "detail") {
                                        if (orderTrackerArrayList.size == 1) {
                                            OrderDetailFragment.btnCancel.visibility = View.GONE
                                            OrderDetailFragment.lytTracker.visibility = View.GONE
                                        }
                                    }
                                    orderItem.active_status = status
                                    getWalletBalance(activity, Session(activity))
                                } else {
                                    holder.btnCancelOrReturn.visibility = View.GONE
                                    holder.tvStatus.setTextColor(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.red
                                        )
                                    )
                                    holder.tvStatus.text = toTitleCase(status)
                                }
                                Constant.isOrderCancelled = true
                            }
                            Toast.makeText(
                                activity,
                                jsonObject.getString("message"),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, params, false)
        }
        alertDialog.setNegativeButton(activity.resources.getString(R.string.no)) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    override fun getItemCount(): Int {
        return orderTrackerArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvPayType: TextView = itemView.findViewById(R.id.tvPayType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvStatusDate: TextView = itemView.findViewById(R.id.tvStatusDate)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val imgOrder: ImageView = itemView.findViewById(R.id.imgOrder)
        val tvCardDetail: CardView = itemView.findViewById(R.id.tvCardDetail)
        val btnCancelOrReturn: Button = itemView.findViewById(R.id.btnCancelOrReturn)
        val lytRatings: RelativeLayout = itemView.findViewById(R.id.lytRatings)
        val ratingProduct: RatingBar = itemView.findViewById(R.id.ratingProduct)
        val tvAddUpdateReview: TextView = itemView.findViewById(R.id.tvAddUpdateReview)

    }

    private fun addUpdateReview(
        holder: ViewHolder,
        orderItem: OrderItem,
        rating: Float,
        review: String,
        isUpdate: Boolean,
        productId: String
    ) {
        try {
            @SuppressLint("InflateParams") val sheetView =
                activity.layoutInflater.inflate(R.layout.dialog_review, null)
            val mBottomSheetDialog = BottomSheetDialog(activity, R.style.BottomSheetTheme)
            mBottomSheetDialog.setContentView(sheetView)
            mBottomSheetDialog.show()
            mBottomSheetDialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            val imgClose = sheetView.findViewById<ImageView>(R.id.imgClose)
            val ratingProduct = sheetView.findViewById<RatingBar>(R.id.ratingProduct)
            val edtReviewMessage = sheetView.findViewById<EditText>(R.id.edtReviewMessage)
            val btnCancel = sheetView.findViewById<Button>(R.id.btnCancel)
            val btnVerify = sheetView.findViewById<Button>(R.id.btnVerify)
            mBottomSheetDialog.setCancelable(true)
            if (isUpdate) {
                edtReviewMessage.setText(review)
            }
            ratingProduct.rating = rating
            imgClose.setOnClickListener { mBottomSheetDialog.dismiss() }
            btnCancel.setOnClickListener { mBottomSheetDialog.dismiss() }
            btnVerify.setOnClickListener {
                setReview(
                    holder,
                    orderItem,
                    ratingProduct.rating,
                    edtReviewMessage.text.toString(),
                    productId,
                    mBottomSheetDialog
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setReview(
        holder: ViewHolder,
        orderItem: OrderItem,
        rating: Float,
        review: String,
        productId: String,
        mBottomSheetDialog: BottomSheetDialog
    ) {
        run {
            val params: MutableMap<String, String> = HashMap()
            params[Constant.ADD_PRODUCT_REVIEW] = Constant.GetVal
            params[Constant.PRODUCT_ID] = productId
            if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
                session.getData(
                    Constant.ID
                )
            params[Constant.RATE] = "" + rating
            params[Constant.REVIEW] = review
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    // System.out.println("================= " + response);
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                holder.ratingProduct.rating = rating
                                holder.tvAddUpdateReview.setText(R.string.update)
                                holder.tvAddUpdateReview.setText(R.string.update)
                                orderItem.isReview_status = true
                                orderItem.rate = "" + rating
                                orderItem.review = review
                                notifyDataSetChanged()
                            }
                            Toast.makeText(
                                activity,
                                jsonObject.getString("message"),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            mBottomSheetDialog.dismiss()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false)
        }
    }

}