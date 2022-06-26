package wrteam.ecart.shop.model


import java.io.Serializable

class Cart(var product_id: String, var product_variant_id: String, var qty: String) : Serializable {
    lateinit var id: String
    lateinit var user_id: String
    lateinit var save_for_later: String
    lateinit var date_created: String
    lateinit var is_cod_allowed: String
    lateinit var type: String
    lateinit var measurement: String
    lateinit var price: String
    lateinit var discounted_price: String
    lateinit var serve_for: String
    lateinit var stock: String
    lateinit var name: String
    lateinit var slug: String
    lateinit var image: String
    lateinit var tax_percentage: String
    lateinit var tax_title: String
    lateinit var total_allowed_quantity: String
    lateinit var unit: String
    lateinit var stock_unit_name: String

}