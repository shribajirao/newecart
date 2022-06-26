package wrteam.ecart.shop.model


import java.io.Serializable

class OfflineItems : Serializable {
    lateinit var id: String
    lateinit var product_id: String
    lateinit var type: String
    lateinit var measurement: String
    lateinit var price: String
    lateinit var discounted_price: String
    lateinit var serve_for: String
    lateinit var stock: String
    lateinit var name: String
    lateinit var image: String
    lateinit var unit: String
    lateinit var cart_count: String
    lateinit var stock_unit_name: String
    lateinit var total_allowed_quantity: String
}