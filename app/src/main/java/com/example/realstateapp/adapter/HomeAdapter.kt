package com.example.realstateapp.adapter

import android.util.Log
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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class HomeAdapter(
    private val activity: RealStateActivity,
    private val listener: HomeListener
) : RecyclerView.Adapter<HomeAdapter.HomeHolder>() {

    private val TAG = "HomeAdapter"

    private val differCallBack = object : DiffUtil.ItemCallback<Home>() {
        override fun areItemsTheSame(oldItem: Home, newItem: Home): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Home, newItem: Home): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    inner class HomeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvHome: CardView = itemView.findViewById(R.id.cv_house)
        val ivHomeImage: ImageView = itemView.findViewById(R.id.iv_house)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_address)
        val cbLiked: CheckBox = itemView.findViewById(R.id.cb_liked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        return HomeHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.house_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        val model = differ.currentList[position]
        val url = model.frontHouseImage
        Glide.with(holder.ivHomeImage.context).load(url).into(holder.ivHomeImage)
        holder.tvTitle.text = model.title
        holder.tvAddress.text = model.address
        val price = ConvertDigitIntoValues.setValueAccordingToLacCr(model.priceType, model.price)
        val houseIsLiked = activity.viewModel.checkHomeLikedOrNot(model)
        holder.cbLiked.isChecked = houseIsLiked
        holder.tvPrice.text = "$price ${model.priceType.uppercase()}";
        holder.cvHome.setOnClickListener {
            listener.onClickedHome(model)
        }
        holder.cbLiked.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listener.onClickedLiked(model, true)
            } else {
                listener.onClickedLiked(model, false)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}

interface HomeListener {
    fun onClickedHome(home: Home)
    fun onClickedLiked(home: Home, value: Boolean)
}


//override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.HomeHolder {
//    return HomeHolder(
//        LayoutInflater.from(parent.context).inflate(
//            R.layout.house_layout, parent, false
//        )
//    )
//}
//
//override fun onBindViewHolder(holder: HomeAdapter.HomeHolder, position: Int, model: Home) {
//    val url = model.frontHouseImage
//    Glide.with(holder.ivHomeImage.context).load(url).into(holder.ivHomeImage)
//    holder.tvTitle.text = model.title
//    holder.tvAddress.text = model.address
//    val price = ConvertDigitIntoValues.setValueAccordingToLacCr(model.priceType,model.price)
//    val houseIsLiked = activity.viewModel.checkHomeLikedOrNot(model)
//    holder.cbLiked.isChecked = houseIsLiked
//    holder.tvPrice.text = "$price ${model.priceType.uppercase()}";
//    holder.cvHome.setOnClickListener{
//        listener.onClickedHome(model)
//    }
//    holder.cbLiked.setOnCheckedChangeListener { _, isChecked ->
//        if(isChecked){
//            listener.onClickedLiked(model, true)
//        }else{
//            listener.onClickedLiked(model, false)
//        }
//    }
//}
//
//override fun onDataChanged() {
//    super.onDataChanged()
//    Log.d(TAG, "OnDataChanged View holder has called")
//    if (snapshots.isEmpty()) {
//        Log.d(TAG, "List is empty")
//        listener.isEmpty(true)
//    } else {
//        Log.d(TAG, "List is not empty")
//        listener.isEmpty(false)
//    }
//}