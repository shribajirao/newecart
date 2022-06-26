package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.fragment.AddressAddUpdateFragment
import wrteam.ecart.shop.fragment.AddressListFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.isConnected
import wrteam.ecart.shop.helper.ApiConfig.Companion.removeAddress
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Address

@SuppressLint("NotifyDataSetChanged")
class AddressAdapter(val activity: Activity, private val addresses: ArrayList<Address?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var id = "0"
    var session: Session = Session(activity)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.lyt_address_list, parent, false)
        return AddressItemHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        val holder = holderParent as AddressItemHolder
        val address = addresses[position]!!
        id = address.id
        holder.setIsRecyclable(false)
        if (Constant.selectedAddressId == id) {
            holder.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            holder.tvAddressType.background = ResourcesCompat.getDrawable(
                activity.resources, R.drawable.right_btn_bg, null
            )
            holder.tvDefaultAddress.background = ResourcesCompat.getDrawable(
                activity.resources, R.drawable.right_btn_bg, null
            )
            holder.imgSelect.setImageResource(R.drawable.ic_check_circle)
            holder.lytMain.setBackgroundResource(R.drawable.selected_shadow)
            Constant.DefaultPinCode = address.pincode
            Constant.DefaultCity = address.city_name
            setData(address)
        } else {
            holder.tvName.setTextColor(ContextCompat.getColor(activity, R.color.gray))
            holder.tvAddressType.background = ResourcesCompat.getDrawable(
                activity.resources, R.drawable.left_btn_bg, null
            )
            holder.tvDefaultAddress.background = ResourcesCompat.getDrawable(
                activity.resources, R.drawable.left_btn_bg, null
            )
            holder.imgSelect.setImageResource(R.drawable.ic_uncheck_circle)
            holder.lytMain.setBackgroundResource(R.drawable.address_card_shadow)
        }
        holder.tvAddress.text =
            address.address + ", " + address.landmark + ", " + address.city_name + ", " + address.area_name + ", " + address.state + ", " + address.country + ", " + activity.getString(
                R.string.pincode_
            ) + address.pincode
        if (address.is_default == "1") {
            holder.tvDefaultAddress.visibility = View.VISIBLE
        }
        holder.lytMain.setPadding(
            activity.resources.getDimension(R.dimen.dimen_15dp).toInt(),
            activity.resources.getDimension(R.dimen.dimen_15dp)
                .toInt(),
            activity.resources.getDimension(R.dimen.dimen_15dp).toInt(),
            activity.resources.getDimension(R.dimen.dimen_15dp)
                .toInt()
        )
        holder.tvName.text = address.name
        if (!address.type.equals("", ignoreCase = true)) {
            holder.tvAddressType.text = address.type
        }
        holder.tvMobile.text = address.mobile
        holder.imgDelete.setOnClickListener { 
            val builder = AlertDialog.Builder(
                activity
            )
            builder.setTitle(activity.resources.getString(R.string.delete_address))
            builder.setIcon(R.drawable.ic_delete)
            builder.setMessage(activity.resources.getString(R.string.delete_address_msg))
            builder.setCancelable(false)
            builder.setPositiveButton(activity.resources.getString(R.string.remove)) { _: DialogInterface, _: Int ->
                if (isConnected(activity)) {
                    addresses.remove(address)
                    notifyItemRemoved(position)
                    removeAddress(activity, address.id)
                }
                if (addresses.size == 0) {
                    AddressListFragment.selectedAddress = ""
                    AddressListFragment.tvAlert.visibility = View.VISIBLE
                } else {
                    AddressListFragment.tvAlert.visibility = View.GONE
                }
            }
            builder.setNegativeButton(activity.resources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }
        holder.lytMain.setOnClickListener { 
            setData(address)
            notifyDataSetChanged()
        }
        holder.imgEdit.setOnClickListener { 
            if (isConnected(
                    activity
                )
            ) {
                val fragment: Fragment = AddressAddUpdateFragment()
                val bundle = Bundle()
                bundle.putSerializable("model", address)
                bundle.putString("for", "update")
                bundle.putInt("position", position)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
        }
    }

    fun setData(address: Address) {
        AddressListFragment.selectedAddress =
            address.address + ", " + address.landmark + ", " + address.city_name + ", " + address.area_name + ", " + address.state + ", " + address.country + ", " + activity.getString(
                R.string.pincode_
            ) + address.pincode
        AddressListFragment.area_id = address.area_id
        Constant.selectedAddressId = address.id
        session.setData(Constant.LONGITUDE, address.longitude)
        session.setData(Constant.LATITUDE, address.latitude)
        if (session.getData(Constant.area_wise_delivery_charge) == "1") {
            AddressListFragment.minimum_amount_for_free_delivery =
                address.minimum_free_delivery_order_amount.toDouble()
            AddressListFragment.delivery_charge = address.delivery_charges.toDouble()
            AddressListFragment.minimum_amount_for_place_order =
                address.minimum_order_amount.toDouble()
        } else {
            AddressListFragment.minimum_amount_for_free_delivery =
                session.getData(Constant.min_order_amount_for_free_delivery)
                    .toDouble()
            AddressListFragment.delivery_charge = session.getData(Constant.delivery_charge)
                .toDouble()
            AddressListFragment.minimum_amount_for_place_order =
                session.getData(Constant.min_order_amount)
                    .toDouble()
        }
    }

    fun addAddress(address: Address) {
        addresses.add(address)
        notifyDataSetChanged()
    }

    fun updateAddress(position: Int, address: Address) {
        addresses[position] = address
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal class AddressItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvAddressType: TextView = itemView.findViewById(R.id.tvAddressType)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val tvDefaultAddress: TextView = itemView.findViewById(R.id.tvDefaultAddress)
        val imgEdit: ImageView = itemView.findViewById(R.id.imgEdit)
        val imgDelete: ImageView = itemView.findViewById(R.id.imgDelete)
        val imgSelect: ImageView = itemView.findViewById(R.id.imgSelect)
        val lytMain: LinearLayout = itemView.findViewById(R.id.lytMain)

    }

}