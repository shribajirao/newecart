package wrteam.ecart.shop.model


import java.io.Serializable

class SupportChat : Serializable {
    lateinit var id: String
    lateinit var type: String
    lateinit var message: String
    lateinit var date_created: String
    lateinit var ticket_id: String
    lateinit var attachments: ArrayList<String>
}