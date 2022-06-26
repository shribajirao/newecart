package wrteam.ecart.shop.model


import java.io.Serializable

class Product : Serializable {
    lateinit var id: String
    lateinit var name: String
    lateinit var slug: String
    lateinit var category_id: String
    lateinit var indicator: String
    lateinit var manufacturer: String
    lateinit var made_in: String
    lateinit var return_status: String
    lateinit var cancelable_status: String
    lateinit var till_status: String
    lateinit var image: String
    lateinit var size_chart: String
    lateinit var description: String
    lateinit var status: String
    lateinit var ratings: String
    lateinit var number_of_ratings: String
    lateinit var tax_percentage: String
    lateinit var total_allowed_quantity: String
    lateinit var shipping_delivery: String
    var is_favorite = false
    lateinit var variants: ArrayList<Variants>
    lateinit var other_images: ArrayList<String>
}