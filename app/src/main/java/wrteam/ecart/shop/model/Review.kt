package wrteam.ecart.shop.model


import java.io.Serializable

class Review : Serializable {
    lateinit var id: String
    lateinit var product_id: String
    lateinit var user_id: String
    lateinit var review: String
    lateinit var status: String
    lateinit var date_added: String
    lateinit var ratings: String
    lateinit var username: String
    lateinit var user_profile: String
}