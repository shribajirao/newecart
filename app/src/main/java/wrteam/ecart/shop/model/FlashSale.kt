package wrteam.ecart.shop.model


import java.io.Serializable

class FlashSale : Serializable {
    lateinit var price: String
    lateinit var discounted_price: String
    lateinit var start_date: String
    lateinit var end_date: String
    var is_start = false
}