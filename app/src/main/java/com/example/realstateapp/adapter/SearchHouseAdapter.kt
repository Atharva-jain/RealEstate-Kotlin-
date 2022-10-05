package com.example.realstateapp.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realstateapp.R
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.ConvertDigitIntoValues


class SearchHouseAdapter(
    private val activity: RealStateActivity,
    private val listener: SearchHouseHasClicked
) : RecyclerView.Adapter<SearchHouseAdapter.SearchHouseHolder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<Home>() {
        override fun areItemsTheSame(oldItem: Home, newItem: Home): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Home, newItem: Home): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    inner class SearchHouseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val searchView: CardView = itemView.findViewById(R.id.search_house_result_layout)
        val houseImage: ImageView = itemView.findViewById(R.id.search_house_result_imageview)
        val houseName: TextView = itemView.findViewById(R.id.search_house_name_result_textview)
        val houseAddress: TextView =
            itemView.findViewById(R.id.search_house_address_result_textview)
        val price: TextView = itemView.findViewById(R.id.search_house_price_result_textview)
        val bhk: TextView = itemView.findViewById(R.id.search_house_bhk_result_textview)
        val heart: CheckBox = itemView.findViewById(R.id.search_cb_Heart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHouseHolder {
        return SearchHouseHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.search_house_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchHouseHolder, position: Int) {
        val model = differ.currentList[position]
        Glide.with(holder.houseImage.context).load(model.frontHouseImage).into(holder.houseImage)
        holder.houseName.text = model.title
        val price = ConvertDigitIntoValues.setValueAccordingToLacCr(model.priceType,model.price)
        holder.price.text = "$price ${model.priceType.uppercase()}";
        holder.bhk.text = model.bhk
        holder.houseAddress.text = model.address
        val houseIsLiked = activity.viewModel.checkHomeLikedOrNot(model)
        holder.heart.isChecked = houseIsLiked
        holder.searchView.setOnClickListener {
            listener.searchHouseHasClicked(model)
        }
        holder.heart.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                listener.likedIsClicked(model, true)
            }else{
                listener.likedIsClicked(model, false)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}

interface SearchHouseHasClicked {
    fun searchHouseHasClicked(home: Home)
    fun likedIsClicked(home: Home, value: Boolean)
}

