package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.ProductDetailFragment
import wrteam.ecart.shop.fragment.SubCategoryFragment
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.helper.Session
import wrteam.ecart.shop.model.Notification

class NotificationAdapter(
    val activity: Activity,
    private val notifications: ArrayList<Notification?>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    var session = Session(activity)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_notification_list, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val notification = notifications[position]!!
            if (notification.image.isNotEmpty()) {
                holderParent.image.visibility = View.VISIBLE
                Picasso.get()
                    .load(notification.image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holderParent.image)
            } else {
                holderParent.image.visibility = View.GONE
            }
            if (notification.name.isNotEmpty()) {
                holderParent.tvTitle.visibility = View.VISIBLE
            } else {
                holderParent.tvTitle.visibility = View.GONE
            }
            if (notification.subtitle.isNotEmpty()) {
                holderParent.tvMessage.visibility = View.VISIBLE
            } else {
                holderParent.tvMessage.visibility = View.GONE
            }
            holderParent.tvTitle.text = Html.fromHtml(notification.name, 0)
            holderParent.tvMessage.text = Html.fromHtml(notification.subtitle, 0)
            val type = notification.type
            if (type.equals("category", ignoreCase = true)) {
                holderParent.tvRedirect.visibility = View.VISIBLE
                holderParent.tvRedirect.text = activity.getString(R.string.go_to_category)
                holderParent.lytMain.setOnClickListener {
                    val fragment: Fragment = SubCategoryFragment()
                    val bundle = Bundle()
                    bundle.putString(Constant.ID, notification.type_id)
                    bundle.putString(Constant.NAME, notification.name)
                    bundle.putString(Constant.FROM, "category")
                    fragment.arguments = bundle
                    (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment).addToBackStack(null).commit()
                }
            } else if (type.equals("product", ignoreCase = true)) {
                holderParent.tvRedirect.visibility = View.VISIBLE
                holderParent.tvRedirect.text = activity.getString(R.string.go_to_product)
                holderParent.lytMain.setOnClickListener {
                    val activity1 = activity as AppCompatActivity
                    val fragment: Fragment = ProductDetailFragment()
                    val bundle = Bundle()
                    bundle.putInt("variantsPosition", 0)
                    bundle.putString("id", notification.type_id)
                    bundle.putString(Constant.FROM, "notification")
                    bundle.putInt("position", 0)
                    fragment.arguments = bundle
                    activity1.supportFragmentManager.beginTransaction()
                        .add(R.id.container, fragment).addToBackStack(null).commit()
                }
            }
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        val product = notifications[position]!!
        return product.id.toInt().toLong()
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvRedirect: TextView = itemView.findViewById(R.id.tvRedirect)
        var lytMain: LinearLayout = itemView.findViewById(R.id.lytMain)

    }

}