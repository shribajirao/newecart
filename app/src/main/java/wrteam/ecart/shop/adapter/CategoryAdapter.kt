package wrteam.ecart.shop.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import wrteam.ecart.shop.R
import wrteam.ecart.shop.fragment.SubCategoryFragment
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.model.Category

class CategoryAdapter(
    val context: Context,
    private val categoryList: ArrayList<Category>,
    val layout: Int,
    val from: String,
    private val visibleNumber: Int
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = categoryList[position]
        holder.tvTitle.text = model.name
        Picasso.get()
            .load(model.image)
            .fit()
            .centerInside()
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgCategory)
        holder.lytMain.setOnClickListener {
            val fragment: Fragment = SubCategoryFragment()
            val bundle = Bundle()
            bundle.putString(Constant.ID, model.id)
            bundle.putString(Constant.NAME, model.name)
            bundle.putString(Constant.FROM, "category")
            fragment.arguments = bundle
            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment).addToBackStack(null).commit()
        }
    }

    override fun getItemCount(): Int {
        val categories: Int = if (categoryList.size > visibleNumber && from == "home") {
            visibleNumber
        } else {
            categoryList.size
        }
        return categories
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val imgCategory: ImageView = itemView.findViewById(R.id.imgCategory)
        val lytMain: LinearLayout = itemView.findViewById(R.id.lytMain)

    }
}