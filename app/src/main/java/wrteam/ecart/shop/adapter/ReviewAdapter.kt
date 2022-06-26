package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.model.Review

class ReviewAdapter(val activity: Activity, private val reviews: ArrayList<Review?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(LayoutInflater.from(activity).inflate(R.layout.lyt_review_list, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val review = reviews[position]!!
            Picasso.get()
                .load(review.user_profile.ifEmpty { "-" })
                .fit()
                .centerInside() //
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holderParent.imgProfile)
            holderParent.ratingReview.rating = review.ratings.toFloat()
            holderParent.tvDate.text =
                activity.getString(R.string.reviewed_on) + review.date_added.split(" ".toRegex())
                    .toTypedArray()[0]
            holderParent.tvName.text = review.username
            holderParent.tvMessage.text = review.review
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val product = reviews[position]!!
        return product.product_id.toInt().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val ratingReview: RatingBar = itemView.findViewById(R.id.ratingReview)

    }
}