@file:Suppress("PrivatePropertyName", "PrivatePropertyName", "PrivatePropertyName",
    "PrivatePropertyName", "PrivatePropertyName", "PrivatePropertyName", "PrivatePropertyName"
)

package wrteam.ecart.shop.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.PaymentActivity
import wrteam.ecart.shop.adapter.CheckoutItemListAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.getCartItemCount
import wrteam.ecart.shop.helper.ApiConfig.Companion.getWalletBalance
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.Cart
import wrteam.ecart.shop.model.PromoCode

@SuppressLint("NotifyDataSetChanged")
class CheckoutFragment : Fragment() {
    private var pCode: String = ""
    private var appliedCode = ""
    private var deliveryCharge = "0"
    private var pCodeDiscount = 0.0
    private var subtotal = 0.0
    private var dCharge = 0.0
    private lateinit var tvConfirmOrder: TextView
    private lateinit var tvSaveAmount: TextView
    private lateinit var tvAlert: TextView
    private lateinit var tvTotalBeforeTax: TextView
    private lateinit var tvDeliveryCharge: TextView
    private lateinit var tvSubTotal: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var processLyt: LinearLayout
    private lateinit var lytSaveAmount: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var root: View
    private lateinit var confirmLyt: RelativeLayout
    private lateinit var btnApply: Button
    private lateinit var tvPromoCode: TextView
    private lateinit var session: Session
    private lateinit var activity: Activity
    private lateinit var checkoutItemListAdapter: CheckoutItemListAdapter
    private lateinit var carts: ArrayList<Cart>
    private lateinit var promoCodes: ArrayList<PromoCode?>
    private var originalAmount = 0f
    private var discountedAmount = 0f
    private lateinit var mShimmerViewContainer: ShimmerFrameLayout
    private lateinit var from: String
    private lateinit var variantIdList: ArrayList<String>
    private lateinit var qtyList: ArrayList<String>
    private lateinit var lytAddress: LinearLayout
    private var offset = 0
    private var total = 0
    private var isLoadMore = false
    private lateinit var promoCodeAdapter: PromoCodeAdapter
    private lateinit var lytPromoCode: LinearLayout
    private lateinit var tvPromoDiscount: TextView
    private var minimum_amount_for_free_delivery = 0.0
    private var delivery_charge = 0.0
    private lateinit var bundle: Bundle

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_checkout, container, false)
        activity = requireActivity()
        session = Session(activity)
        bundle = requireArguments()
        tvAlert = root.findViewById(R.id.tvAlert)
        tvPromoCode = root.findViewById(R.id.tvPromoCode)
        tvSubTotal = root.findViewById(R.id.tvSubTotal)
        tvTotalItems = root.findViewById(R.id.tvTotalItems)
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge)
        confirmLyt = root.findViewById(R.id.confirmLyt)
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder)
        processLyt = root.findViewById(R.id.processLyt)
        tvTotalBeforeTax = root.findViewById(R.id.tvTotalBeforeTax)
        tvSaveAmount = root.findViewById(R.id.tvSaveAmount)
        lytSaveAmount = root.findViewById(R.id.lytSaveAmount)
        btnApply = root.findViewById(R.id.btnApply)
        recyclerView = root.findViewById(R.id.recyclerView)
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        lytAddress = root.findViewById(R.id.lytAddress)
        lytPromoCode = root.findViewById(R.id.lytPromoCode)
        tvPromoDiscount = root.findViewById(R.id.tvPromoDiscount)

        recyclerView.layoutManager = LinearLayoutManager(getActivity())

        from = bundle.getString("from").toString()
        delivery_charge = bundle.getDouble("delivery_charge")
        minimum_amount_for_free_delivery = bundle.getDouble("minimum_amount_for_free_delivery")
        lytAddress.visibility = if (from == "cart") View.GONE else View.VISIBLE
        processLyt.weightSum = (if (from == "cart") 2 else 3.toFloat()) as Float
        Constant.isCODAllow = true
        isSoldOut = false
        setHasOptionsMenu(true)


        tvTotalItems.text = Constant.TOTAL_CART_ITEM.toString() + " Items"
        variantIdList = ArrayList()
        qtyList = ArrayList()
        Constant.FLOAT_TOTAL_AMOUNT = 0.0

        tvConfirmOrder.setOnClickListener {
            if (subtotal != 0.0 && Constant.FLOAT_TOTAL_AMOUNT != 0.toDouble()) {
                startActivity(
                    Intent(activity, PaymentActivity::class.java)
                        .putExtra("subtotal", ("" + subtotal).toDouble())
                        .putExtra("total", ("" + Constant.FLOAT_TOTAL_AMOUNT).toDouble())
                        .putExtra("pCodeDiscount", ("" + pCodeDiscount).toDouble())
                        .putExtra("pCode", pCode)
                        .putExtra(
                            "area_id",
                            if (bundle.getString("area_id") == null) "" else bundle.getString(
                                "area_id"
                            )
                        )
                        .putExtra("qtyList", qtyList)
                        .putExtra("variantIdList", variantIdList)
                        .putExtra("delivery_charge", delivery_charge)
                        .putExtra("address", bundle.getString("address"))
                        .putExtra(Constant.FROM, from)
                )
            }
        }
        if (isConnected(activity)) {
            getWalletBalance(activity, session)
            getCartData()
        }
        btnApply.setOnClickListener { getPromoCode() }
        tvPromoCode.setOnClickListener { getPromoCode() }
        return root
    }

    private fun getPromoCode() {
        if (btnApply.tag == "applied") {
            pCode = ""
            btnApply.setBackgroundColor(
                ContextCompat.getColor(
                    activity,
                    R.color.colorPrimary
                )
            )
            btnApply.text = activity.getString(R.string.view_offers)
            tvPromoCode.text = activity.getString(R.string.select_a_promo_code)
            btnApply.tag = "not_applied"
            isApplied = false
            appliedCode = ""
            lytPromoCode.visibility = View.GONE
            dCharge =
                if (tvDeliveryCharge.text.toString() == getString(R.string.free)) 0.0 else delivery_charge
            subtotal -= pCodeDiscount
            pCodeDiscount = 0.0
            tvSubTotal.text = String.format(
                "%s%s",
                session.getData(Constant.currency),
                stringFormat("" + subtotal)
            )
            setTotalData()
        } else {
            openDialog(activity)
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setTotalData() {
        try {
            if (originalAmount - discountedAmount != 0f) {
                lytSaveAmount.visibility = View.VISIBLE
                if (pCodeDiscount != 0.0) {
                    tvSaveAmount.text =
                        session.getData(Constant.currency) + stringFormat("" + (originalAmount - discountedAmount + pCodeDiscount))
                } else {
                    tvSaveAmount.text =
                        session.getData(Constant.currency) + stringFormat("" + (originalAmount - discountedAmount - pCodeDiscount))
                }
            } else {
                if (pCodeDiscount == 0.0) {
                    lytSaveAmount.visibility = View.GONE
                }
            }
            subtotal = Constant.FLOAT_TOTAL_AMOUNT
            tvTotalBeforeTax.text =
                session.getData(Constant.currency) + stringFormat("" + Constant.FLOAT_TOTAL_AMOUNT)
            if (Constant.FLOAT_TOTAL_AMOUNT <= minimum_amount_for_free_delivery) {
                tvDeliveryCharge.text = session.getData(Constant.currency) + delivery_charge
                subtotal += delivery_charge
                deliveryCharge = "" + delivery_charge
            } else {
                tvDeliveryCharge.text = resources.getString(R.string.free)
                deliveryCharge = "0"
                delivery_charge = 0.0
            }
            dCharge =
                if (tvDeliveryCharge.text.toString() == getString(R.string.free)) 0.0 else delivery_charge
            if (pCode.isNotEmpty()) {
                subtotal -= pCodeDiscount
            }
            tvSubTotal.text = session.getData(Constant.currency) + stringFormat("" + subtotal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCartData() {
        carts = ArrayList()

        recyclerView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        getCartItemCount(activity, session)
        subtotal = 0.0
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_USER_CART] = Constant.GetVal
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                        val gson = Gson()
                        for (i in 0 until jsonArray.length()) {
                            try {
                                val cart = gson.fromJson(
                                    jsonArray.getJSONObject(i).toString(),
                                    Cart::class.java
                                )
                                variantIdList.add(cart.product_variant_id)
                                qtyList.add(cart.qty)
                                var price: Float
                                val qty = cart.qty.toInt()
                                val taxPercentage = cart.tax_percentage
                                if (cart.discounted_price == "0" || cart.discounted_price == "") {
                                    price =
                                        cart.price.toFloat() + cart.price.toFloat() * taxPercentage.toFloat() / 100
                                } else {
                                    originalAmount += cart.price.toFloat() * qty
                                    discountedAmount += cart.discounted_price.toFloat() * qty
                                    price =
                                        cart.discounted_price.toFloat() + cart.discounted_price.toFloat() * taxPercentage.toFloat() / 100
                                }
                                Constant.FLOAT_TOTAL_AMOUNT += price * qty
                                carts.add(cart)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        setTotalData()

                        checkoutItemListAdapter = CheckoutItemListAdapter(activity,carts)
                        recyclerView.adapter = checkoutItemListAdapter

                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                        confirmLyt.visibility = View.VISIBLE
                        mShimmerViewContainer.stopShimmer()
                        mShimmerViewContainer.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }, activity, Constant.CART_URL, params, false)

        setTotalData()
        checkoutItemListAdapter = CheckoutItemListAdapter(activity, carts)
        recyclerView.adapter = checkoutItemListAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    fun openDialog(activity: Activity) {
        offset = 0
        val alertDialog = AlertDialog.Builder(requireContext())
        val inflater1 =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater1.inflate(R.layout.dialog_promo_code_selection, null)
        alertDialog.setView(dialogView)
        alertDialog.setCancelable(true)
        val dialog = alertDialog.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val scrollView: NestedScrollView = dialogView.findViewById(R.id.scrollView)
        val tvAlert: TextView = dialogView.findViewById(R.id.tvAlert)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
        val shimmerFrameLayout: ShimmerFrameLayout =
            dialogView.findViewById(R.id.shimmerFrameLayout)
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        shimmerFrameLayout.visibility = View.VISIBLE
        shimmerFrameLayout.startShimmer()
        tvAlert.text = getString(R.string.no_promo_code_found)
        btnCancel.setOnClickListener { dialog.dismiss() }
        getPromoCodes(
            recyclerView,
            tvAlert,
            linearLayoutManager,
            scrollView,
            dialog,
            shimmerFrameLayout
        )
        dialog.show()
    }

    private fun getPromoCodes(
        recyclerView: RecyclerView,
        tvAlert: TextView,
        linearLayoutManager: LinearLayoutManager,
        scrollView: NestedScrollView,
        dialog: AlertDialog,
        shimmerFrameLayout: ShimmerFrameLayout
    ) {
        promoCodes = ArrayList()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_PROMO_CODES] = Constant.GetVal
        params[Constant.USER_ID] = "" + session.getData(Constant.ID)
        params[Constant.AMOUNT] = java.lang.String.valueOf(Constant.FLOAT_TOTAL_AMOUNT)
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            try {
                                total = jsonObject.getString(Constant.TOTAL).toInt()
                                val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject1 = jsonArray.getJSONObject(i)
                                    val promoCode =
                                        Gson().fromJson(
                                            jsonObject1.toString(),
                                            PromoCode::class.java
                                        )
                                    promoCodes.add(promoCode)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (offset == 0) {
                                recyclerView.visibility = View.VISIBLE
                                tvAlert.visibility = View.GONE
                                promoCodeAdapter = PromoCodeAdapter(activity, promoCodes, dialog)
                                promoCodeAdapter.setHasStableIds(true)
                                recyclerView.adapter = promoCodeAdapter
                                shimmerFrameLayout.visibility = View.GONE
                                shimmerFrameLayout.stopShimmer()
                                scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->

                                    // if (diff == 0) {
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        if (promoCodes.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == promoCodes.size - 1) {
                                                    //bottom of list!
                                                    promoCodes.add(PromoCode())
                                                    promoCodeAdapter.notifyItemInserted(promoCodes.size - 1)
                                                    offset += Constant.LOAD_ITEM_LIMIT + 20
                                                    val params1: MutableMap<String, String> =
                                                        HashMap()
                                                    params1[Constant.GET_PROMO_CODES] =
                                                        Constant.GetVal
                                                    params1[Constant.USER_ID] =
                                                        "" + session.getData(Constant.ID)
                                                    params1[Constant.AMOUNT] =
                                                        java.lang.String.valueOf(Constant.FLOAT_TOTAL_AMOUNT)
                                                    requestToVolley(
                                                        object : VolleyCallback {
                                                            override fun onSuccess(
                                                                result: Boolean,
                                                                response: String
                                                            ) {
                                                                if (result) {
                                                                    try {
                                                                        val jsonObject1 =
                                                                            JSONObject(response)
                                                                        if (!jsonObject1.getBoolean(
                                                                                Constant.ERROR
                                                                            )
                                                                        ) {
                                                                            promoCodes.removeAt(
                                                                                promoCodes.size - 1
                                                                            )
                                                                            promoCodeAdapter.notifyItemRemoved(
                                                                                promoCodes.size
                                                                            )
                                                                            val jsonObject =
                                                                                JSONObject(response)
                                                                            val jsonArray =
                                                                                jsonObject.getJSONArray(
                                                                                    Constant.DATA
                                                                                )

                                                                            for (i in 0 until jsonArray.length()) {
                                                                                val jsonObject2 =
                                                                                    jsonArray.getJSONObject(
                                                                                        i
                                                                                    )
                                                                                val promoCode =
                                                                                    Gson().fromJson(
                                                                                        jsonObject2.toString(),
                                                                                        PromoCode::class.java
                                                                                    )
                                                                                promoCodes.add(
                                                                                    promoCode
                                                                                )
                                                                            }
                                                                            promoCodeAdapter.notifyDataSetChanged()
                                                                            promoCodeAdapter.setLoaded()
                                                                            isLoadMore = false
                                                                        }
                                                                    } catch (e: JSONException) {
                                                                        e.printStackTrace()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        activity,
                                                        Constant.PROMO_CODE_CHECK_URL,
                                                        params1,
                                                        false
                                                    )
                                                }
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            shimmerFrameLayout.visibility = View.GONE
                            shimmerFrameLayout.stopShimmer()
                            recyclerView.visibility = View.GONE
                            tvAlert.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        shimmerFrameLayout.visibility = View.GONE
                        shimmerFrameLayout.stopShimmer()
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.PROMO_CODE_CHECK_URL, params, false)
    }

    inner class PromoCodeAdapter(
        val activity: Activity,
        private val promoCodes: ArrayList<PromoCode?>,
        val dialog: AlertDialog
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // for load more
        val viewTypeItem = 0
        val viewTypeLoading = 1
        private var isLoading = false
        val session = Session(activity)
        fun add(position: Int, promoCode: PromoCode) {
            promoCodes.add(position, promoCode)
            notifyItemInserted(position)
        }

        fun setLoaded() {
            isLoading = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View
            return when (viewType) {
                viewTypeItem -> {
                    view =
                        LayoutInflater.from(activity)
                            .inflate(R.layout.lyt_promo_code_list, parent, false)
                    ItemHolder(view)
                }
                viewTypeLoading -> {
                    view = LayoutInflater.from(activity)
                        .inflate(R.layout.item_progressbar, parent, false)
                    ViewHolderLoading(view)
                }
                else -> throw IllegalArgumentException("unexpected viewType: $viewType")
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
            if (holderParent is ItemHolder) {
                try {
                    val promoCode = promoCodes[position]!!
                    holderParent.tvMessage.text = promoCode.message
                    holderParent.tvPromoCode.text = promoCode.promo_code
                    if (promoCode.is_validate[0].isError) {
                        holderParent.tvMessageAlert.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.promo_code_fail
                            )
                        )
                        holderParent.tvMessageAlert.text = promoCode.is_validate[0].message
                        holderParent.tvApply.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.gray
                            )
                        )
                    } else {
                        holderParent.tvMessageAlert.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.colorPrimary
                            )
                        )
                        holderParent.tvMessageAlert.text =
                            activity.getString(R.string.you_will_save) + session.getData(
                                Constant.currency
                            ) + promoCode.is_validate[0].discount + activity.getString(R.string.with_this_code)
                        holderParent.tvApply.setTextColor(
                            ContextCompat.getColor(
                                activity, R.color.colorPrimary
                            )
                        )
                    }
                    holderParent.tvApply.setOnClickListener {
                        if (!promoCode.is_validate[0].isError) {
                            pCode = promoCode.promo_code
                            btnApply.setBackgroundColor(
                                ContextCompat.getColor(
                                    activity, R.color.light_green
                                )
                            )
                            btnApply.text = activity.getString(R.string.remove_offer)
                            btnApply.tag = "applied"
                            isApplied = true
                            appliedCode = tvPromoCode.text.toString()
                            dCharge =
                                if (tvDeliveryCharge.text.toString() == getString(R.string.free)) 0.0 else delivery_charge
                            subtotal =
                                promoCode.is_validate[0].discounted_amount.toDouble()
                            pCodeDiscount =
                                promoCode.is_validate[0].discounted_amount.toDouble()
                            tvSubTotal.text = session.getData(Constant.currency) + stringFormat(
                                "" + promoCode.is_validate[0].discounted_amount
                            )
                            lytPromoCode.visibility = View.VISIBLE
                            tvPromoCode.text = promoCode.promo_code
                            tvPromoDiscount.text =
                                "-" + session.getData(Constant.currency) + stringFormat(
                                    "" + promoCode.is_validate[0].discount
                                )
                            dialog.dismiss()
                            setTotalData()
                        } else {
                            ObjectAnimator.ofFloat(
                                holderParent.tvMessageAlert,
                                "translationX",
                                0f,
                                25f,
                                -25f,
                                25f,
                                -25f,
                                15f,
                                -15f,
                                6f,
                                -6f,
                                0f
                            ).setDuration(300).start()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (holderParent is ViewHolderLoading) {
                holderParent.progressBar.isIndeterminate = true
            }
        }

        override fun getItemCount(): Int {
            return promoCodes.size
        }

        override fun getItemViewType(position: Int): Int {
            return viewTypeItem
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        internal inner class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
            val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

        }

        internal inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
            val tvPromoCode: TextView = itemView.findViewById(R.id.tvPromoCode)
            val tvMessageAlert: TextView = itemView.findViewById(R.id.tvMessageAlert)
            val tvApply: TextView = itemView.findViewById(R.id.tvApply)

        }

    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.checkout)
        activity.invalidateOptionsMenu()
        hideKeyboard()
    }

    fun hideKeyboard() {
        try {
            val inputMethodManager =
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputMethodManager.hideSoftInputFromWindow(root.applicationWindowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_layout).isVisible = false
        menu.findItem(R.id.toolbar_cart).isVisible = false
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = false
        activity.invalidateOptionsMenu()
    }

    companion object {
        var isApplied = false
        var isSoldOut = false
    }
}