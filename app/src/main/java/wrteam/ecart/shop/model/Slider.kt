package wrteam.ecart.shop.model


import java.io.Serializable

class Slider(val image: String) : Serializable {
    lateinit var type: String
    lateinit var type_id: String
    lateinit var name: String
    lateinit var slider_url: String

}