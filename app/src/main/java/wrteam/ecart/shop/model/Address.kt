@file:Suppress("PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName", "PropertyName",
    "PropertyName", "PropertyName", "PropertyName"
)

package wrteam.ecart.shop.model


import java.io.Serializable

class Address : Serializable {
    lateinit var id: String
    lateinit var user_id: String
    lateinit var type: String
    lateinit var name: String
    lateinit var mobile: String
    lateinit var alternate_mobile: String
    lateinit var address: String
    lateinit var landmark: String
    lateinit var area_id: String
    lateinit var city_id: String
    lateinit var pincode: String
    lateinit var state: String
    lateinit var country: String
    lateinit var city_name: String
    lateinit var area_name: String
    lateinit var is_default: String
    lateinit var latitude: String
    lateinit var longitude: String
    lateinit var minimum_free_delivery_order_amount: String
    lateinit var delivery_charges: String
    lateinit var minimum_order_amount: String
}