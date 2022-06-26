package wrteam.ecart.shop.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.AdapterStyle1
import wrteam.ecart.shop.adapter.ProductLoadMoreAdapter
import wrteam.ecart.shop.adapter.ReviewAdapter
import wrteam.ecart.shop.adapter.SliderAdapter
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMarkers
import wrteam.ecart.shop.helper.ApiConfig.Companion.addMultipleProductInCart
import wrteam.ecart.shop.helper.ApiConfig.Companion.addOrRemoveFavorite
import wrteam.ecart.shop.helper.ApiConfig.Companion.buildCounterDrawable
import wrteam.ecart.shop.helper.ApiConfig.Companion.calculateDays
import wrteam.ecart.shop.helper.ApiConfig.Companion.getCartItemCount
import wrteam.ecart.shop.helper.ApiConfig.Companion.getDiscount
import wrteam.ecart.shop.helper.ApiConfig.Companion.getSettings
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.helper.ApiConfig.Companion.stringFormat
import wrteam.ecart.shop.helper.ApiConfig.Companion.toTitleCase
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.DatabaseHelper
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.helper.Utils.setFormatTime
import wrteam.ecart.shop.helper.VolleyCallback
import wrteam.ecart.shop.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min

@SuppressLint("SetTextI18n")
class ProductDetailFragment : Fragment() {
    lateinit var sliderArrayList: ArrayList<Slider>
    private lateinit var showDiscount: TextView
    private lateinit var tvMfg: TextView
    private lateinit var tvMadeIn: TextView
    lateinit var tvProductName: TextView
    lateinit var tvQuantity: TextView
    lateinit var tvPrice: TextView
    lateinit var tvOriginalPrice: TextView
    lateinit var tvMeasurement: TextView
    lateinit var tvStatus: TextView
    private lateinit var tvTitleMadeIn: TextView
    private lateinit var tvTitleMfg: TextView
    lateinit var tvTimer: TextView
    private lateinit var tvTimerTitle: TextView
    private lateinit var webViewDescription: WebView
    private lateinit var webViewShippingDetail: WebView
    lateinit var viewPager: ViewPager
    private lateinit var spinner: Spinner
    private lateinit var lytSpinner: LinearLayout
    private lateinit var imgIndicator: ImageView
    lateinit var mMarkersLayout: LinearLayout
    private lateinit var lytMfg: LinearLayout
    private lateinit var lytMadeIn: LinearLayout
    private lateinit var lytMainPrice: RelativeLayout
    lateinit var lytQuantity: RelativeLayout
    lateinit var scrollView: ScrollView
    lateinit var session: Session
    private lateinit var imgFav: ImageView
    lateinit var btnAddQty: ImageButton
    lateinit var btnMinusQty: ImageButton
    private lateinit var lytShare: LinearLayout
    private lateinit var lytSave: LinearLayout
    private lateinit var lytSimilar: LinearLayout
    lateinit var root: View
    lateinit var from: String
    lateinit var id: String
    lateinit var product: Product
    lateinit var productListItem: ProductList
    lateinit var databaseHelper: DatabaseHelper
    private lateinit var btnCart: Button
    lateinit var activity: Activity
    lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewReview: RecyclerView
    lateinit var relativeLayout: RelativeLayout
    private lateinit var lytTimer: RelativeLayout
    private lateinit var tvMore: TextView
    private lateinit var tvSizeCharts: TextView
    private lateinit var imgReturnable: ImageView
    private lateinit var imgCancellable: ImageView
    private lateinit var tvReturnable: TextView
    private lateinit var tvCancellable: TextView
    lateinit var taxPercentage: String
    private lateinit var lottieAnimationView: LottieAnimationView
    lateinit var mShimmerViewContainer: ShimmerFrameLayout
    lateinit var btnAddToCart: Button
    private lateinit var reviewArrayList: ArrayList<Review?>
    lateinit var reviewAdapter: ReviewAdapter
    private lateinit var ratingProduct1: RatingBar
    private lateinit var ratingProduct: RatingBar
    private lateinit var tvRatingProductCount: TextView
    private lateinit var tvRatingCount: TextView
    private lateinit var tvMoreReview: TextView
    private lateinit var tvReviewDetail: TextView
    private lateinit var tvSavePrice: TextView
    private lateinit var lytProductRatings: LinearLayout
    private lateinit var lytReview: RelativeLayout
    var isLogin = false
    private var isFavorite = false
    var variantPosition = 0
    var position = 0
    private var availableStock: Long = 0

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_product_detail, container, false)
        setHasOptionsMenu(true)
        activity = requireActivity()
        Constant.CartValues = HashMap()
        session = Session(activity)
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN)
        databaseHelper = DatabaseHelper(activity)
        assert(arguments != null)
        from = arguments?.getString(Constant.FROM).toString()
        taxPercentage = "0"
        variantPosition = arguments?.getInt("variantPosition", 0)!!
        id = requireArguments().getString("id").toString()
        if (from == "favorite" || from == "fragment" || from == "sub_cate" || from == "product" || from == "search" || from == "flash_sale") {
            position = arguments?.getInt("position")!!
        }
        lytQuantity = root.findViewById(R.id.lytQuantity)
        scrollView = root.findViewById(R.id.scrollView)
        mMarkersLayout = root.findViewById(R.id.layout_markers)
        sliderArrayList = ArrayList()
        viewPager = root.findViewById(R.id.viewPager)
        tvProductName = root.findViewById(R.id.tvProductName)
        tvOriginalPrice = root.findViewById(R.id.tvOriginalPrice)
        webViewDescription = root.findViewById(R.id.webViewDescription)
        webViewShippingDetail = root.findViewById(R.id.webViewShippingDetail)
        tvPrice = root.findViewById(R.id.tvPrice)
        tvMeasurement = root.findViewById(R.id.tvMeasurement)
        imgFav = root.findViewById(R.id.imgFav)
        lytMainPrice = root.findViewById(R.id.lytMainPrice)
        tvQuantity = root.findViewById(R.id.tvQuantity)
        tvStatus = root.findViewById(R.id.tvStatus)
        btnAddQty = root.findViewById(R.id.btnAddQty)
        btnMinusQty = root.findViewById(R.id.btnMinusQty)
        spinner = root.findViewById(R.id.spinner)
        lytSpinner = root.findViewById(R.id.lytSpinner)
        imgIndicator = root.findViewById(R.id.imgIndicator)
        showDiscount = root.findViewById(R.id.showDiscount)
        lytShare = root.findViewById(R.id.lytShare)
        lytSave = root.findViewById(R.id.lytSave)
        lytSimilar = root.findViewById(R.id.lytSimilar)
        tvMadeIn = root.findViewById(R.id.tvMadeIn)
        tvTitleMadeIn = root.findViewById(R.id.tvTitleMadeIn)
        tvMfg = root.findViewById(R.id.tvMfg)
        tvTitleMfg = root.findViewById(R.id.tvTitleMfg)
        tvTimer = root.findViewById(R.id.tvTimer)
        tvTimerTitle = root.findViewById(R.id.tvTimerTitle)
        lytMfg = root.findViewById(R.id.lytMfg)
        lytMadeIn = root.findViewById(R.id.lytMadeIn)
        btnCart = root.findViewById(R.id.btnCart)
        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerViewReview = root.findViewById(R.id.recyclerViewReview)
        relativeLayout = root.findViewById(R.id.relativeLayout)
        lytTimer = root.findViewById(R.id.lytTimer)
        tvMore = root.findViewById(R.id.tvMore)
        tvSizeCharts = root.findViewById(R.id.tvSizeCharts)
        ratingProduct1 = root.findViewById(R.id.ratingProduct1)
        ratingProduct = root.findViewById(R.id.ratingProduct)
        tvRatingProductCount = root.findViewById(R.id.tvRatingProductCount)
        tvRatingCount = root.findViewById(R.id.tvRatingCount)
        tvReviewDetail = root.findViewById(R.id.tvReviewDetail)
        tvSavePrice = root.findViewById(R.id.tvSavePrice)
        tvMoreReview = root.findViewById(R.id.tvMoreReview)
        lytProductRatings = root.findViewById(R.id.lytProductRatings)
        lytReview = root.findViewById(R.id.lytReview)
        tvReturnable = root.findViewById(R.id.tvReturnable)
        tvCancellable = root.findViewById(R.id.tvCancellable)
        imgReturnable = root.findViewById(R.id.imgReturnable)
        imgCancellable = root.findViewById(R.id.imgCancellable)
        btnAddToCart = root.findViewById(R.id.btnAddToCart)
        //        btnBuyNow = root.findViewById(R.id.btnBuyNow);
        lottieAnimationView = root.findViewById(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation("add_to_wish_list.json")
        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer)
        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recyclerViewReview.layoutManager = LinearLayoutManager(activity)
        if (session.getData(Constant.ratings) == "1") {
            lytProductRatings.visibility = View.VISIBLE
            lytReview.visibility = View.VISIBLE
        } else {
            lytProductRatings.visibility = View.GONE
            lytReview.visibility = View.GONE
        }
        getProductDetail(id)
        getSettings(activity)
        lytSpinner.setOnClickListener { spinner.performClick() }
        tvMore.setOnClickListener { showSimilar() }
        tvMoreReview.setOnClickListener {
            val fragment: Fragment = ReviewFragment()
            val bundle = Bundle()
            bundle.putString(Constant.FROM, from)
            bundle.putString(Constant.ID, id)
            fragment.arguments = bundle
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
                .commit()
        }
        lytSimilar.setOnClickListener { showSimilar() }
        btnCart.setOnClickListener {
            MainActivity.fm.beginTransaction().add(R.id.container, CartFragment())
                .addToBackStack(null).commit()
        }
        lytShare.setOnClickListener {
            val message = Constant.WebsiteUrl + "product/" + product.slug
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            sendIntent.putExtra(Intent.EXTRA_TEXT, message)
            sendIntent.type = "text/plain"
            val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_via))
            startActivity(shareIntent)
        }
        lytSave.setOnClickListener {
            if (isLogin) {
                isFavorite = product.is_favorite
                isFavorite = productListItem.is_favorite
                if (isConnected(activity)) {
                    if (isFavorite) {
                        isFavorite = false
                        lottieAnimationView.visibility = View.GONE
                        product.is_favorite = false
                        productListItem.is_favorite = false
                        imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                    } else {
                        isFavorite = true
                        product.is_favorite = true
                        productListItem.is_favorite = true
                        lottieAnimationView.visibility = View.VISIBLE
                        lottieAnimationView.playAnimation()
                    }
                    addOrRemoveFavorite(
                        activity,
                        session,
                        product.variants[0].product_id,
                        isFavorite
                    )
                }
            } else {
                isFavorite = databaseHelper.getFavoriteById(product.id)
                if (isFavorite) {
                    isFavorite = false
                    lottieAnimationView.visibility = View.GONE
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                } else {
                    isFavorite = true
                    lottieAnimationView.visibility = View.VISIBLE
                    lottieAnimationView.playAnimation()
                }
                databaseHelper.addOrRemoveFavorite(
                    product.variants[0].product_id,
                    isFavorite
                )
            }
            when (from) {
                "fragment", "sub_cate", "search" -> {
                    ProductListFragment.productArrayList[position]!!.is_favorite = isFavorite
                    ProductListFragment.productLoadMoreAdapter.notifyDataSetChanged()
                }
                "favorite" -> {
                    product.is_favorite = isFavorite
                    productListItem.is_favorite = isFavorite
                    if (isFavorite) {
                        FavoriteFragment.productArrayList.add(productListItem)
                    } else {
                        FavoriteFragment.productArrayList.removeAt(position)
                    }
                    FavoriteFragment.productLoadMoreAdapter.notifyDataSetChanged()
                }
            }
        }
        return root
    }

    private fun showSimilar() {
        val fragment: Fragment = ProductListFragment()
        val bundle = Bundle()
        bundle.putString("id", product.id)
        bundle.putString("cat_id", product.category_id)
        bundle.putString(Constant.FROM, "similar")
        bundle.putString("name", "Similar Products")
        fragment.arguments = bundle
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null)
            .commit()
    }

    private fun showSimilar(product: Product) {
        val hashMap = HashMap<String, Long>()
        val productArrayList = ArrayList<ProductList?>()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_SIMILAR_PRODUCT] = Constant.GetVal
        params[Constant.PRODUCT_ID] = product.id
        params[Constant.CATEGORY_ID] = product.category_id
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        params[Constant.LIMIT] = "" + Constant.LOAD_ITEM_LIMIT
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            val jsonArray = jsonObject1.getJSONArray(Constant.DATA)
                            try {
                                for (i in 0 until jsonArray.length()) {
                                    val product1 = Gson().fromJson(
                                        jsonArray.getJSONObject(i).toString(),
                                        ProductList::class.java
                                    )
                                    for (variant in product.variants) {
                                        val unitMeasurement =
                                            if (variant.measurement_unit_name.equals(
                                                    "kg",
                                                    ignoreCase = true
                                                ) || variant.measurement_unit_name.equals(
                                                    "ltr",
                                                    ignoreCase = true
                                                )
                                            ) 1000 else 1.toLong()
                                        val unit =
                                            variant.measurement.toDouble()
                                                .toLong() * unitMeasurement
                                        if (!hashMap.containsKey(variant.product_id)) {
                                            hashMap[variant.product_id] =
                                                (variant.stock.toDouble() * (if (variant.stock_unit_name.equals(
                                                        "kg",
                                                        ignoreCase = true
                                                    ) || variant.stock_unit_name.equals(
                                                        "ltr",
                                                        ignoreCase = true
                                                    )
                                                ) 1000 else 1) - unit * variant.cart_count.toLong()).toLong()
                                        } else {
                                            hashMap.replace(
                                                variant.product_id,
                                                hashMap[variant.product_id]?.minus(unit * variant.cart_count.toLong())
                                                    ?: 0
                                            )
                                        }
                                    }
                                    productArrayList.add(product1)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            val adapter = AdapterStyle1(
                                activity,
                                productArrayList,
                                R.layout.offer_layout,
                                hashMap
                            )
                            recyclerView.adapter = adapter
                            relativeLayout.visibility = View.VISIBLE
                        } else {
                            relativeLayout.visibility = View.GONE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }, activity, Constant.GET_SIMILAR_PRODUCT_URL, params, false)
    }

    private fun notifyData(count: Int) {
        when (from) {
            "fragment", "flash_sale", "search" -> {
                ProductListFragment.productArrayList[position]!!.variants[variantPosition].cart_count =
                    "" + count
                ProductListFragment.productLoadMoreAdapter.notifyItemChanged(
                    position,
                    ProductListFragment.productArrayList[position]
                )
                if (isLogin) {
                    getCartItemCount(activity, session)
                } else {
                    databaseHelper.getTotalItemOfCart(activity)
                }
            }
            "product" -> if (isLogin) {
                FavoriteFragment.productArrayList[position]?.variants!![variantPosition].cart_count =
                    "" + count
                FavoriteFragment.productLoadMoreAdapter.notifyItemChanged(
                    position,
                    FavoriteFragment.productArrayList[position]
                )
            } else {
                FavoriteFragment.productArrayList[position]!!.variants[variantPosition].cart_count =
                    "" + count
                FavoriteFragment.productLoadMoreAdapter.notifyItemChanged(
                    position,
                    FavoriteFragment.productArrayList[position]
                )
                databaseHelper.getTotalItemOfCart(activity)
            }
            "section", "share" -> if (!isLogin) {
                databaseHelper.getTotalItemOfCart(activity)
            } else {
                getCartItemCount(activity, session)
            }
        }
    }

    private fun getProductDetail(productId: String) {
        scrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        if (from == "share") {
            params[Constant.SLUG] = productId
        } else {
            params[Constant.PRODUCT_ID] = productId
        }
        if (session.getBoolean(Constant.IS_USER_LOGIN)) params[Constant.USER_ID] =
            session.getData(
                Constant.ID
            )
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject1 = JSONObject(response)
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                            for (i in 0 until jsonArray.length()) {
                                product = Gson().fromJson(
                                    jsonArray.getJSONObject(i).toString(),
                                    Product::class.java
                                )
                                productListItem = Gson().fromJson(
                                    jsonArray.getJSONObject(i).toString(),
                                    ProductList::class.java
                                )

                            }
                        }
                        setProductDetails(product)
                        showSimilar(product)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    scrollView.visibility = View.VISIBLE
                    mShimmerViewContainer.visibility = View.GONE
                    mShimmerViewContainer.stopShimmer()
                }
            }
        }, activity, Constant.GET_PRODUCT_DETAIL_URL, params, false)
    }

    private fun getReviews(productId: String) {
        reviewArrayList = ArrayList()
        scrollView.visibility = View.GONE
        mShimmerViewContainer.visibility = View.VISIBLE
        mShimmerViewContainer.startShimmer()
        val params: MutableMap<String, String> = HashMap()
        params[Constant.GET_PRODUCT_REVIEW] = Constant.GetVal
        params[Constant.LIMIT] = "5"
        params[Constant.OFFSET] = "0"
        if (from == "share") {
            params[Constant.SLUG] = productId
        } else {
            params[Constant.PRODUCT_ID] = productId
        }
        requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val jsonArrayReviews = jsonObject.getJSONArray(Constant.PRODUCT_REVIEW)
                            for (i in 0 until min(jsonArrayReviews.length(), 5)) {
                                val review = Gson().fromJson(
                                    jsonArrayReviews.getJSONObject(i).toString(),
                                    Review::class.java
                                )
                                reviewArrayList.add(review)
                            }
                            reviewAdapter = ReviewAdapter(activity, reviewArrayList)
                            recyclerViewReview.adapter = reviewAdapter
                        } else {
                            lytReview.visibility = View.GONE
                        }
                        scrollView.visibility = View.VISIBLE
                        mShimmerViewContainer.visibility = View.GONE
                        mShimmerViewContainer.stopShimmer()
                    } catch (e: JSONException) {
                        scrollView.visibility = View.VISIBLE
                        mShimmerViewContainer.visibility = View.GONE
                        mShimmerViewContainer.stopShimmer()
                    }
                }
            }
        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false)
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    fun setProductDetails(product: Product) {
        try {
            val variants = product.variants[variantPosition]
            availableStock =
                (product.variants[0].stock.toDouble() * if (variants.stock_unit_name.equals(
                        "kg",
                        ignoreCase = true
                    ) || variants.stock_unit_name.equals("ltr", ignoreCase = true)
                ) 1000 else 1).toLong()
            for (variants_ in product.variants) {
                val unitMeasurement = if (variants_.measurement_unit_name.equals(
                        "kg",
                        ignoreCase = true
                    ) || variants_.measurement_unit_name.equals("ltr", ignoreCase = true)
                ) 1000 else 1.toLong()
                val unit = variants_.measurement.toDouble().toLong() * unitMeasurement
                availableStock -= if (isLogin) {
                    unit * variants_.cart_count.toLong()
                } else {
                    unit * databaseHelper.checkCartItemExist(variants_.id, variants_.product_id)
                        .toLong()
                }
            }
            try {
                taxPercentage =
                    if (product.tax_percentage.toDouble() > 0) product.tax_percentage else "0"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ratingProduct1 = root.findViewById(R.id.ratingProduct1)
            ratingProduct = root.findViewById(R.id.ratingProduct)
            tvRatingProductCount = root.findViewById(R.id.tvRatingProductCount)
            tvRatingCount = root.findViewById(R.id.tvRatingCount)
            tvMoreReview = root.findViewById(R.id.tvMoreReview)
            if (session.getData(Constant.ratings) == "1") {
                ratingProduct1.rating = product.ratings.toFloat()
                ratingProduct.rating = product.ratings.toFloat()
                tvRatingProductCount.text = product.number_of_ratings
                tvRatingCount.text = product.ratings + getString(R.string.out_of_5)
                tvReviewDetail.text =
                    product.number_of_ratings + getString(R.string.global_ratings)
            }
            if (product.made_in.isNotEmpty()) {
                lytMadeIn.visibility = View.VISIBLE
                tvMadeIn.text = product.made_in
            }
            if (product.manufacturer.isNotEmpty()) {
                lytMfg.visibility = View.VISIBLE
                tvMfg.text = product.manufacturer
            }
            if (isLogin) {
                if (product.is_favorite) {
                    isFavorite = true
                    imgFav.setImageResource(R.drawable.ic_is_favorite)
                } else {
                    isFavorite = false
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                }
            } else {
                if (databaseHelper.getFavoriteById(product.id)) {
                    imgFav.setImageResource(R.drawable.ic_is_favorite)
                } else {
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite)
                }
            }
            if (isLogin) {
                if (Constant.CartValues.containsKey(variants.id)) {
                    tvQuantity.text = "" + Constant.CartValues[variants.id]
                } else {
                    tvQuantity.text = variants.cart_count
                }
            } else {
                tvQuantity.text =
                    databaseHelper.checkCartItemExist(variants.id, variants.product_id)
            }
            if (product.return_status.equals("1", ignoreCase = true)) {
                imgReturnable.setImageResource(R.drawable.ic_returnable)
                tvReturnable.text = session.getData(Constant.max_product_return_days).toInt()
                    .toString() + " Days Returnable."
            } else {
                imgReturnable.setImageResource(R.drawable.ic_not_returnable)
                tvReturnable.text = "Not Returnable."
            }
            if (product.cancelable_status.equals("1", ignoreCase = true)) {
                imgCancellable.setImageResource(R.drawable.ic_cancellable)
                tvCancellable.text = "Order Can Cancel Till Order " + toTitleCase(
                    product.till_status
                ) + "."
            } else {
                imgCancellable.setImageResource(R.drawable.ic_not_cancellable)
                tvCancellable.text = "Non Cancellable."
            }
            if (product.size_chart != "") {
                tvSizeCharts.visibility = View.VISIBLE
            } else {
                tvSizeCharts.visibility = View.GONE
            }
            tvSizeCharts.setOnClickListener {
                val sizeChart = ArrayList<Slider>()
                sizeChart.add(Slider(product.size_chart))
                val fragment: Fragment = FullScreenViewFragment()
                val bundle = Bundle()
                bundle.putInt("pos", position)
                bundle.putSerializable("images", sizeChart)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
            if (product.variants.size == 1) {
                spinner.visibility = View.GONE
                lytSpinner.visibility = View.GONE
                lytMainPrice.isEnabled = false
                session.setData(Constant.PRODUCT_VARIANT_ID, "" + 0)
                setSelectedData(variants)
            }
            if (product.indicator != "0") {
                imgIndicator.visibility = View.VISIBLE
                if (product.indicator == "1") imgIndicator.setImageResource(R.drawable.ic_veg_icon) else if (product.indicator == "2") imgIndicator.setImageResource(
                    R.drawable.ic_non_veg_icon
                )
            }

            val variantsName = arrayOfNulls<String>(product.variants.size)
            val variantsStockStatus = arrayOfNulls<String>(product.variants.size)
            for ((i, name) in product.variants.withIndex()) {
                variantsName[i] = name.measurement + " " + name.measurement_unit_name
                variantsStockStatus[i] = name.serve_for
            }

            spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        setSelectedData(product.variants[position])
                    }
                }

            val customAdapter =
                ProductLoadMoreAdapter.CustomAdapter(activity, variantsName, variantsStockStatus)
            spinner.adapter = customAdapter

            webViewDescription.isVerticalScrollBarEnabled = true
            webViewDescription.loadDataWithBaseURL(
                "",
                product.description,
                "text/html",
                "UTF-8",
                ""
            )
            webViewDescription.setBackgroundColor(
                ContextCompat.getColor(
                    activity, R.color.white
                )
            )
            webViewShippingDetail.isVerticalScrollBarEnabled = true
            webViewShippingDetail.loadDataWithBaseURL(
                "",
                product.shipping_delivery,
                "text/html",
                "UTF-8",
                ""
            )
            webViewShippingDetail.setBackgroundColor(
                ContextCompat.getColor(
                    activity, R.color.white
                )
            )
            tvProductName.text = product.name
            spinner.setSelection(variantPosition)
            viewPager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
                override fun onPageSelected(position: Int) {
                    addMarkers(position, sliderArrayList, mMarkersLayout, activity)
                }

                override fun onPageScrollStateChanged(i: Int) {}
            })
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    variantPosition = i
                    session.setData(Constant.PRODUCT_VARIANT_ID, "" + i)
                    setSelectedData(product.variants[i])
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            }
            if (session.getData(Constant.ratings) == "1") {
                getReviews(id)
            } else {
                scrollView.visibility = View.VISIBLE
                mShimmerViewContainer.visibility = View.GONE
                mShimmerViewContainer.stopShimmer()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        Constant.TOOLBAR_TITLE = getString(R.string.app_name)
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

    @SuppressLint("SetTextI18n")
    fun setSelectedData(variants: Variants) {

//        GST_Amount (Original Cost x GST %)/100
//        Net_Price Original Cost + GST Amount
        try {
            tvProductName.text = product.name
            tvMeasurement.text = variants.measurement + variants.measurement_unit_name
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                if (Constant.CartValues.containsKey(variants.id)) {
                    tvQuantity.text = "" + Constant.CartValues[variants.id]
                }
            } else {
                tvQuantity.text = session.getData(variants.id)
            }
            sliderArrayList = ArrayList()
            sliderArrayList.add(Slider(product.image))
            if (variants.images.size != 0) {
                val arrayList = variants.images
                for (i in arrayList.indices) {
                    sliderArrayList.add(Slider(arrayList[i]))
                }
            } else {
                val arrayList = product.other_images
                for (i in arrayList.indices) {
                    sliderArrayList.add(Slider(arrayList[i]))
                }
            }
            viewPager.adapter =
                SliderAdapter(sliderArrayList, activity, R.layout.lyt_detail_slider, "detail")
            addMarkers(0, sliderArrayList, mMarkersLayout, activity)
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
            if (variants.is_flash_sales.equals("false", ignoreCase = true)) {
                lytTimer.visibility = View.GONE
                discountedPrice =
                    (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                originalPrice =
                    (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                if (variants.discounted_price == "0" || variants.discounted_price == "") {
                    tvSavePrice.visibility = View.GONE
                    tvOriginalPrice.visibility = View.GONE
                    showDiscount.visibility = View.GONE
                    tvPrice.text =
                        session.getData(Constant.currency) + stringFormat("" + originalPrice)
                } else {
                    tvSavePrice.visibility = View.VISIBLE
                    tvOriginalPrice.visibility = View.VISIBLE
                    showDiscount.visibility = View.VISIBLE
                }
            } else {
                lytTimer.visibility = View.VISIBLE
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
                        startTimer(variants, different)
                    } else {
                        tvTimer.text = calculateDays(activity, days)
                    }
                    tvTimerTitle.text = activity.getString(R.string.ends_in)
                    if (flashSale.discounted_price == "0" || flashSale.discounted_price == "") {
                        tvSavePrice.visibility = View.GONE
                        showDiscount.visibility = View.GONE
                        tvPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + originalPrice)
                    } else {
                        tvSavePrice.visibility = View.VISIBLE
                        showDiscount.visibility = View.VISIBLE
                        discountedPrice =
                            (flashSale.discounted_price.toFloat() + flashSale.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        originalPrice =
                            (flashSale.price.toFloat() + flashSale.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        tvOriginalPrice.paintFlags =
                            tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        tvOriginalPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + originalPrice)
                        tvPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + discountedPrice)
                    }
                } else {
                    startDate = df.parse(session.getData(Constant.current_date))!!
                    endDate = df.parse(flashSale.start_date)!!
                    different = endDate.time - startDate.time
                    val days = different / (60 * 60 * 24 * 1000)
                    if (setFormatTime(days).equals("00", ignoreCase = true)) {
                        startTimer(variants, different)
                    } else {
                        tvTimer.text = calculateDays(activity, days)
                    }
                    tvTimerTitle.text = activity.getString(R.string.starts_in)
                    tvTimer.text = calculateDays(activity, abs(days))
                    if (variants.discounted_price == "0" || variants.discounted_price == "") {
                        tvSavePrice.visibility = View.GONE
                        showDiscount.visibility = View.GONE
                        tvPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + originalPrice)
                    } else {
                        showDiscount.visibility = View.VISIBLE
                        tvSavePrice.visibility = View.VISIBLE
                        discountedPrice =
                            (variants.discounted_price.toFloat() + variants.discounted_price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        originalPrice =
                            (variants.price.toFloat() + variants.price.toFloat() * taxPercentage.toFloat() / 100).toDouble()
                        tvOriginalPrice.paintFlags =
                            tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        tvOriginalPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + originalPrice)
                        tvPrice.text =
                            session.getData(Constant.currency) + stringFormat("" + discountedPrice)
                    }
                }
                showDiscount.text = "-" + getDiscount(originalPrice, discountedPrice)
            }
            tvOriginalPrice.paintFlags =
                tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvOriginalPrice.text =
                session.getData(Constant.currency) + stringFormat("" + originalPrice)
            tvPrice.text =
                session.getData(Constant.currency) + stringFormat("" + if (discountedPrice == 0.0) originalPrice else discountedPrice)
            showDiscount.text = "-" + getDiscount(originalPrice, discountedPrice)
            tvSavePrice.text =
                getString(R.string.you_save) + session.getData(Constant.currency) + stringFormat("" + (originalPrice - discountedPrice))
            if (variants.serve_for.equals(Constant.SOLD_OUT_TEXT, ignoreCase = true)) {
                tvStatus.visibility = View.VISIBLE
                lytQuantity.visibility = View.GONE
            } else {
                tvStatus.visibility = View.GONE
                lytQuantity.visibility = View.VISIBLE
            }
            if (isLogin) {
                if (Constant.CartValues.containsKey(variants.id)) {
                    tvQuantity.text = "" + Constant.CartValues[variants.id]
                } else {
                    tvQuantity.text = variants.cart_count
                }
                if (variants.cart_count == "0") {
                    btnAddToCart.visibility = View.VISIBLE
                } else {
                    if (session.getData(Constant.STATUS) == "1") {
                        btnAddToCart.visibility = View.GONE
                    } else {
                        btnAddToCart.visibility = View.VISIBLE
                    }
                }
            } else {
                if (databaseHelper.checkCartItemExist(variants.id, variants.product_id) == "0") {
                    btnAddToCart.visibility = View.VISIBLE
                } else {
                    btnAddToCart.visibility = View.GONE
                }
                tvQuantity.text =
                    databaseHelper.checkCartItemExist(variants.id, variants.product_id)
            }
            val maxCartCont: String =
                if (product.total_allowed_quantity == "" || product.total_allowed_quantity == "0") {
                    session.getData(Constant.max_cart_items_count)
                } else {
                    product.total_allowed_quantity
                }
            val isLoose = variants.type.equals("loose", ignoreCase = true)
            if (isLoose) {
                btnMinusQty.setOnClickListener {
                    removeLooseItemFromCartClickEvent(
                        variants
                    )
                }
                btnAddQty.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        variants,
                        maxCartCont
                    )
                }
                btnAddToCart.setOnClickListener {
                    addLooseItemToCartClickEvent(
                        variants,
                        maxCartCont
                    )
                }
            } else {
                btnMinusQty.setOnClickListener {
                    removeFromCartClickEvent(
                        variants,
                        maxCartCont
                    )
                }
                btnAddQty.setOnClickListener {
                    addToCartClickEvent(
                        variants,
                        maxCartCont
                    )
                }
                btnAddToCart.setOnClickListener {
                    addToCartClickEvent(
                        variants,
                        maxCartCont
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addLooseItemToCartClickEvent(variants: Variants, maxCartCont: String) {
        val unitMeasurement = if (variants.measurement_unit_name.equals(
                "kg",
                ignoreCase = true
            ) || variants.measurement_unit_name.equals("ltr", ignoreCase = true)
        ) 1000 else 1.toLong()
        val unit = variants.measurement.toDouble().toLong() * unitMeasurement
        if (session.getData(Constant.STATUS) == "1") {
            var count = tvQuantity.text.toString().toInt()
            if (count <= maxCartCont.toInt()) {
                count++
                if (availableStock >= unit) {
                    availableStock -= unit
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
                    notifyData(count)
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

    private fun removeLooseItemFromCartClickEvent(variants: Variants) {
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
                tvQuantity.text = "" + count
                addMultipleProductInCart(session, activity, Constant.CartValues)
                if (count > 0) {
                    btnAddToCart.visibility = View.GONE
                    if (Constant.CartValues.containsKey(variants.id)) {
                        Constant.CartValues.replace(variants.id, "" + count)
                    } else {
                        Constant.CartValues[variants.id] = "" + count
                    }
                } else {
                    btnAddToCart.visibility = View.VISIBLE
                }
            } else {
                tvQuantity.text = "" + count
                databaseHelper.addToCart(variants.id, variants.product_id, "" + count)
                databaseHelper.getTotalItemOfCart(activity)
            }
            notifyData(count)
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.user_deactivate_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun addToCartClickEvent(variants: Variants, maxCartCont: String) {
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
                    val countList1 = ArrayList<Cart>()
                    val cart11 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart1 in Constant.countList) {
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
                    notifyData(count)
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

    private fun removeFromCartClickEvent(variants: Variants, maxCartCont: String) {
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
                    val countList1 = ArrayList<Cart>()
                    val cart11 = Cart(variants.product_id, variants.id, variants.cart_count)
                    for (cart1 in Constant.countList) {
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
                    notifyData(count)
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.toolbar_layout).isVisible = false
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.toolbar_cart).isVisible = true
        menu.findItem(R.id.toolbar_sort).isVisible = false
        menu.findItem(R.id.toolbar_search).isVisible = true
        menu.findItem(R.id.toolbar_cart).icon = buildCounterDrawable(
            Constant.TOTAL_CART_ITEM,
            activity
        )
        activity.invalidateOptionsMenu()
    }

    override fun onPause() {
        super.onPause()
        addMultipleProductInCart(session, activity, Constant.CartValues)
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

    private fun startTimer(variants: Variants, duration: Long) {
        try {
            object : CountDownTimer(duration, 1000) {
                override fun onTick(different: Long) {
                    val seconds = (different / 1000).toInt() % 60
                    val minutes = (different / (1000 * 60) % 60).toInt()
                    val hours = (different / (1000 * 60 * 60) % 24).toInt()
                    tvTimer.text =
                        setFormatTime(hours.toLong()) + ":" + setFormatTime(minutes.toLong()) + ":" + setFormatTime(
                            seconds.toLong()
                        )
                }

                @SuppressLint("NotifyDataSetChanged")
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