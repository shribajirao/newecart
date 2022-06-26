package wrteam.ecart.shop.model


import java.io.Serializable

class Section : Serializable {
    lateinit var id: String
    lateinit var title: String
    lateinit var short_description: String
    lateinit var style: String
    lateinit var products: ArrayList<ProductList?>
}