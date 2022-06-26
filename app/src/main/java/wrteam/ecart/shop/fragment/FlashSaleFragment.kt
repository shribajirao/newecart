package wrteam.ecart.shop.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import wrteam.ecart.shop.R
import wrteam.ecart.shop.adapter.FlashSaleAdapter
import wrteam.ecart.shop.helper.Constant
import wrteam.ecart.shop.model.ProductList

class FlashSaleFragment : Fragment() {
    lateinit var root: View
    lateinit var jsonObject: JSONObject
    lateinit var recyclerView: RecyclerView
    lateinit var activity: Activity
    private lateinit var productArrayList: ArrayList<ProductList?>
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            root = inflater.inflate(R.layout.fragment_flash_sale, container, false)

            activity = requireActivity()
            productArrayList = ArrayList()

            recyclerView = root.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            
            jsonObject = JSONObject(requireArguments().getString("data")!!)
            val jsonArray = jsonObject.getJSONArray(Constant.PRODUCTS)
            for (i in 0 until jsonArray.length()) {
                val product = Gson().fromJson(jsonArray[i].toString(), ProductList::class.java)
                productArrayList.add(product)
            }
            val flashSaleAdapter = FlashSaleAdapter(activity, productArrayList)
            recyclerView.adapter = flashSaleAdapter
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return root
    }

    companion object {
        fun addFragment(jsonObject: JSONObject): FlashSaleFragment {
            val fragment = FlashSaleFragment()
            val args = Bundle()
            args.putString("data", jsonObject.toString())
            fragment.arguments = args
            return fragment
        }
    }
}