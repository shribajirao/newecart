package wrteam.ecart.shop.model


import java.io.Serializable

class Variants : Serializable {
    lateinit var id: String
    lateinit var product_id: String
    lateinit var type: String
    lateinit var measurement: String
    lateinit var price: String
    lateinit var discounted_price: String
    lateinit var serve_for: String
    lateinit var stock: String
    lateinit var measurement_unit_name: String
    lateinit var stock_unit_name: String
    lateinit var cart_count: String
    lateinit var is_flash_sales: String
    lateinit var flash_sales: ArrayList<FlashSale>
    lateinit var images: ArrayList<String>

}