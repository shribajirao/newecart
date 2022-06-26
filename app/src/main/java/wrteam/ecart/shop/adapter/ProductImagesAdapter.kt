package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.helper.*
import wrteam.ecart.shop.helper.ApiConfig.Companion.requestToVolley
import wrteam.ecart.shop.model.Attachment

@SuppressLint("NotifyDataSetChanged")
class ProductImagesAdapter(
    val activity: Activity,
    val images: ArrayList<Attachment>,
    var from: String,
    private var orderId: String
) : RecyclerView.Adapter<ProductImagesAdapter.ImageHolder>() {
    val session: Session = Session(activity)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        return ImageHolder(
            LayoutInflater.from(
                activity
            ).inflate(R.layout.lyt_image_list, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ImageHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val image = images[position]
        if (from == "api") {
            Picasso.get().load(image.image)
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProductImage)
        } else {
            holder.imgProductImage.setImageBitmap(BitmapFactory.decodeFile(image.image))
        }
        holder.imgProductImageDelete.setOnClickListener {
            if (orderId == "0") {
                images.remove(image)
                notifyDataSetChanged()
            } else {
                removeImage(activity, image.id, image)
            }
        }
    }

    private fun removeImage(activity: Activity, id: String, image: Attachment) {
        val alertDialog = AlertDialog.Builder(
            activity
        )
        // Setting Dialog Message
        alertDialog.setTitle(R.string.remove_image)
        alertDialog.setMessage(R.string.remove_image_msg)
        alertDialog.setCancelable(false)
        val alertDialog1 = alertDialog.create()

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes) { dialog: DialogInterface, _: Int ->
            val params: MutableMap<String, String> = HashMap()
            params[Constant.DELETE_BANK_TRANSFER_ATTACHMENT] = Constant.GetVal
            params[Constant.ORDER_ID] = orderId
            params[Constant.ID] = id
            requestToVolley(object : VolleyCallback {
                override fun onSuccess(result: Boolean, response: String) {
                    if (result) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                images.remove(image)
                                notifyDataSetChanged()
                            } else {
                                dialog.dismiss()
                            }
                        } catch (e: JSONException) {
                            dialog.dismiss()
                            e.printStackTrace()
                        }
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, params, false)
        }
        alertDialog.setNegativeButton(R.string.no) { _: DialogInterface, _: Int -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProductImage: ImageView = itemView.findViewById(R.id.imgProductImage)
        val imgProductImageDelete: ImageView = itemView.findViewById(R.id.imgProductImageDelete)

    }

}