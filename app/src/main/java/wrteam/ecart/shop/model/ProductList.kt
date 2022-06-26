package wrteam.ecart.shop.model


import java.io.Serializable

class ProductList : Serializable {
    lateinit var id: String

    lateinit var name: String
    lateinit var indicator: String
    lateinit var image: String
    lateinit var ratings: String
    lateinit var number_of_ratings: String
    lateinit var total_allowed_quantity: String
    lateinit var slug: String
    lateinit var description: String
    lateinit var status: String
    lateinit var category_name: String
    lateinit var tax_percentage: String
    lateinit var price: String
    var is_favorite = false
    lateinit var variants: ArrayList<Variants>
}