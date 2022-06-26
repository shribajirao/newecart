package wrteam.ecart.shop.model


import java.io.Serializable

class OrderItem : Serializable {
    lateinit var id: String
    lateinit var user_id: String
    lateinit var order_id: String
    lateinit var product_variant_id: String
    lateinit var quantity: String
    lateinit var price: String
    lateinit var discounted_price: String
    lateinit var tax_percentage: String
    lateinit var discount: String
    lateinit var product_id: String
    lateinit var variant_id: String
    lateinit var rate: String
    lateinit var review: String
    lateinit var product_name: String
    lateinit var image: String
    lateinit var return_status: String
    lateinit var cancelable_status: String
    lateinit var till_status: String
    lateinit var measurement: String
    lateinit var unit: String
    lateinit var active_status: String
    var isReview_status = false
}