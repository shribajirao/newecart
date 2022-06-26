package wrteam.ecart.shop.model


import java.io.Serializable

class PromoCode : Serializable {
    lateinit var promo_code: String
    lateinit var message: String
    lateinit var discount: String
    lateinit var is_validate: ArrayList<Validate>
}