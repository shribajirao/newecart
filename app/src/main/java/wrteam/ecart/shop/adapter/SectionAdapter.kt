package wrteam.ecart.shop.adapter

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import wrteam.ecart.shop.R
import wrteam.ecart.shop.activity.MainActivity
import wrteam.ecart.shop.adapter.SectionAdapter.SectionHolder
import wrteam.ecart.shop.fragment.ProductListFragment
import wrteam.ecart.shop.helper.ApiConfig.Companion.getOfferImage
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.model.Section

class SectionAdapter(
    val activity: Activity,
    private val sectionList: ArrayList<Section>,
    val jsonArray: JSONArray
) : RecyclerView.Adapter<SectionHolder>() {
    var hashMap: HashMap<String, Long> = HashMap()
    private var jsonArrayImages: JSONArray
    override fun getItemCount(): Int {
        return sectionList.size
    }

    override fun onBindViewHolder(holder: SectionHolder, position: Int) {
        try {
            val section = sectionList[position]
            holder.tvTitle.text = section.title
            holder.tvSubTitle.text = section.short_description
            holder.lytBelowSectionOfferImages.layoutManager = LinearLayoutManager(activity)
            holder.lytBelowSectionOfferImages.isNestedScrollingEnabled = false
            try {
                jsonArrayImages =
                    jsonArray.getJSONObject(position).getJSONArray(Constant.OFFER_IMAGES)
                getOfferImage(activity, jsonArrayImages, holder.lytBelowSectionOfferImages)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            for (product in section.products) {
                for (variant in product!!.variants) {
                    val unitMeasurement = if (variant.measurement_unit_name.equals(
                            "kg",
                            ignoreCase = true
                        ) || variant.measurement_unit_name.equals("ltr", ignoreCase = true)
                    ) 1000 else 1.toLong()
                    val unit = variant.measurement.toDouble().toLong() * unitMeasurement
                    if (!hashMap.containsKey(variant.product_id)) {
                        hashMap[variant.product_id] =
                            (variant.stock.toDouble() * (if (variant.stock_unit_name.equals(
                                    "kg",
                                    ignoreCase = true
                                ) || variant.stock_unit_name.equals("ltr", ignoreCase = true)
                            ) 1000 else 1) - unit * variant.cart_count.toLong()).toLong()
                    } else {
                        hashMap.replace(
                            variant.product_id,
                            hashMap[variant.product_id]?.minus(unit * variant.cart_count.toLong())!!)
                    }
                }
            }
            when (section.style) {
                "style_1" -> {
                    holder.recyclerView.layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    val adapter =
                        AdapterStyle1(activity, section.products, R.layout.offer_layout, hashMap)
                    holder.recyclerView.adapter = adapter
                }
                "style_2" -> {
                    holder.recyclerView.layoutManager = LinearLayoutManager(activity)
                    val adapterStyle2 = AdapterStyle2(activity, section.products, hashMap)
                    holder.recyclerView.adapter = adapterStyle2
                }
                "style_3" -> {
                    holder.recyclerView.layoutManager = GridLayoutManager(activity, 2)
                    val adapter3 =
                        AdapterStyle1(activity, section.products, R.layout.lyt_style_3, hashMap)
                    holder.recyclerView.adapter = adapter3
                }
            }
            holder.tvMore.setOnClickListener {
                val fragment: Fragment = ProductListFragment()
                val bundle = Bundle()
                bundle.putString(Constant.FROM, "section")
                bundle.putString(Constant.NAME, section.title)
                bundle.putString(Constant.ID, section.id)
                fragment.arguments = bundle
                MainActivity.fm.beginTransaction().add(R.id.container, fragment)
                    .addToBackStack(null).commit()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.section_layout, parent, false)
        return SectionHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class SectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvSubTitle: TextView = itemView.findViewById(R.id.tvSubTitle)
        val tvMore: TextView = itemView.findViewById(R.id.tvMore)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        val lytBelowSectionOfferImages: RecyclerView = itemView.findViewById(R.id.lytBelowSectionOfferImages)

    }

    init {
        jsonArrayImages = JSONArray()
    }
}