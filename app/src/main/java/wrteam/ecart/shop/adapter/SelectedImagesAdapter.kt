package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import java.io.File

@SuppressLint("NotifyDataSetChanged")
class SelectedImagesAdapter(val activity: Activity, private val receiptImages: ArrayList<String>) :
    RecyclerView.Adapter<SelectedImagesAdapter.ImageHolder>() {
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
        Picasso.get().load(File(receiptImages[position]))
            .fit()
            .centerInside()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgProductImage)

        holder.imgProductImageDelete.setOnClickListener {
            receiptImages.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int {
        return receiptImages.size
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