package wrteam.ecart.shop.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import wrteam.ecart.shop.R
import wrteam.ecart.shop.model.Faq

class FaqAdapter(val activity: Activity, private val faqs: ArrayList<Faq?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // for load more
    val viewTypeItem = 0
    val viewTypeLoading = 1
    var visible = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(activity).inflate(R.layout.lyt_faq_list, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderParent: RecyclerView.ViewHolder, position: Int) {
        if (holderParent is ItemHolder) {
            val faq = faqs[position]!!
            if (faq.question.isNotEmpty() && faq.answer.isNotBlank()) {
                holderParent.tvQue.text = faq.question
                holderParent.tvAns.text = faq.answer
                holderParent.tvAns.visibility = View.GONE
            } else {
                holderParent.mainLyt.visibility = View.GONE
            }
            holderParent.mainLyt.setOnClickListener {
                if (visible) {
                    visible = false
                    holderParent.tvAns.visibility = View.GONE
                } else {
                    visible = true
                    holderParent.tvAns.visibility = View.VISIBLE
                }
            }
        } else if (holderParent is ViewHolderLoading) {
            holderParent.progressBar.isIndeterminate = true
        }
    }

    override fun getItemCount(): Int {
        return faqs.size
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeItem
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    internal class ViewHolderLoading(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.itemProgressbar)

    }

    internal class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQue: TextView = itemView.findViewById(R.id.tvQue)
        val tvAns: TextView = itemView.findViewById(R.id.tvAns)
        val mainLyt: RelativeLayout = itemView.findViewById(R.id.mainLyt)

    }
}