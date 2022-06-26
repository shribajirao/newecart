package wrteam.ecart.shop.model


import java.io.Serializable

class SupportTicket : Serializable {
    lateinit var id: String
    lateinit var type_id: String
    lateinit var title: String
    lateinit var status: String
    lateinit var message: String
    lateinit var type: String
    lateinit var image: ArrayList<String>
}