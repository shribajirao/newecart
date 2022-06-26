package wrteam.ecart.shop.model


import java.io.Serializable

class OrderTracker(var id: String, var user_id: String) : Serializable {
    lateinit var otp: String
    lateinit var mobile: String
    lateinit var order_note: String
    lateinit var total: String
    lateinit var delivery_charge: String
    lateinit var tax_amount: String
    lateinit var tax_percentage: String
    lateinit var wallet_balance: String
    lateinit var discount: String
    lateinit var promo_code: String
    lateinit var promo_discount: String
    lateinit var final_total: String
    lateinit var payment_method: String
    lateinit var address: String
    lateinit var latitude: String
    lateinit var longitude: String
    lateinit var delivery_time: String
    lateinit var seller_notes: String
    lateinit var local_pickup: String
    lateinit var pickup_time: String
    lateinit var active_status: String
    lateinit var date_added: String
    lateinit var order_time: String
    lateinit var order_from: String
    lateinit var total_attachment: String
    lateinit var user_name: String
    lateinit var discount_rupees: String
    lateinit var bank_transfer_message: String
    lateinit var bank_transfer_status: String
    lateinit var status_name: ArrayList<String>
    lateinit var status_time: ArrayList<String>
    lateinit var items: ArrayList<OrderItem>
    lateinit var attachment: ArrayList<Attachment>

}