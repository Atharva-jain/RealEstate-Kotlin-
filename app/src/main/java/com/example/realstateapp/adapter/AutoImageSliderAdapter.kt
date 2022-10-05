package com.example.realstateapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.realstateapp.R
import com.smarteist.autoimageslider.SliderViewAdapter


class AutoImageSliderAdapter(private val listener: OnImageClicked,val value: Boolean) :
    SliderViewAdapter<AutoImageSliderAdapter.AutoImageSliderHolder>() {

    var imageList: ArrayList<String> = ArrayList()

    class AutoImageSliderHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivAutoImageView)
        val deleteImage: ImageButton = itemView.findViewById(R.id.ivDeleteImageButton)
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?): AutoImageSliderHolder {
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.auto_image_slider_layout, parent, false)
        return AutoImageSliderHolder(view)
    }

    override fun onBindViewHolder(viewHolder: AutoImageSliderHolder, position: Int) {
        val url = imageList[position]
        Log.d("AutoImageSliderAdapter", url)
        Glide.with(viewHolder.imageView.context).load(url).into(viewHolder.imageView)
        if(value){
            viewHolder.deleteImage.visibility = View.GONE
        }else{
            viewHolder.deleteImage.visibility = View.VISIBLE
        }
        viewHolder.imageView.setOnClickListener{
            listener.onImageClicked(url)
        }
        viewHolder.deleteImage.setOnClickListener{
            listener.onDeleteImageClicked(url)
        }
    }
    fun renewItems(list: ArrayList<String>) {
        imageList = list
        notifyDataSetChanged()
    }

    fun deleteItem(url: String) {
        imageList.remove(url)
        notifyDataSetChanged()
    }

    fun addItem(url: String) {
        imageList.add(url)
        notifyDataSetChanged()
    }
}

interface OnImageClicked {
    fun onImageClicked(imageUrl: String)
    fun onDeleteImageClicked(imageUrl: String)
}

