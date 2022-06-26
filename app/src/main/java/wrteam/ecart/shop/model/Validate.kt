package wrteam.ecart.shop.model


import java.io.Serializable

class Validate : Serializable {
    var isError = false
    lateinit var message: String
    lateinit var discounted_amount: String
    lateinit var discount: String
}