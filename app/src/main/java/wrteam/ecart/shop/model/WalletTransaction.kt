package wrteam.ecart.shop.model


import java.io.Serializable

class WalletTransaction : Serializable {
    lateinit var id: String
    lateinit var user_id: String
    lateinit var order_id: String
    lateinit var type: String
    lateinit var amount: String
    lateinit var status: String
    lateinit var message: String
    lateinit var date_created: String
}