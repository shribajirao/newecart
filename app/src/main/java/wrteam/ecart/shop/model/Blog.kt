package wrteam.ecart.shop.model


import java.io.Serializable

class Blog : Serializable {
    lateinit var id: String
    lateinit var title: String
    lateinit var description: String
    lateinit var image: String
}