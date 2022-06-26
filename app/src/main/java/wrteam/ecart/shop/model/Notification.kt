package wrteam.ecart.shop.model


import java.io.Serializable

class Notification : Serializable {
    lateinit var id: String
    lateinit var name: String
    lateinit var subtitle: String
    lateinit var type: String
    lateinit var type_id: String
    lateinit var image: String
}