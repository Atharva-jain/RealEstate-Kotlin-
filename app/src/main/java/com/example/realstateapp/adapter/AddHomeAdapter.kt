package com.example.realstateapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realstateapp.R
import com.example.realstateapp.data.remote.Home
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AddHomeAdapter(
    options: FirestoreRecyclerOptions<Home>,
    private val listener: OnClickedAddHome
) : FirestoreRecyclerAdapter<Home, AddHomeAdapter.UserListHolder>(options) {
    private val TAG = "UserListAdapter"

    inner class UserListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvOwnHome: CardView = itemView.findViewById(R.id.ownHouseCardView)
        val ivOwnHome: ImageView = itemView.findViewById(R.id.ownHouseImageView)
        val ivEditButton: ImageView = itemView.findViewById(R.id.ownHouseEditButton)
        val ivDeleteButton: ImageView = itemView.findViewById(R.id.ownHouseDeleteButton)
        val tvOwnHome: TextView = itemView.findViewById(R.id.ownHouseTextView)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder {
        return UserListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.your_home_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UserListHolder, position: Int, model: Home) {
        val url = model.frontHouseImage
        Glide.with(holder.ivOwnHome.context).load(url).into(holder.ivOwnHome)
        holder.tvOwnHome.text = model.title
        holder.ivEditButton.setOnClickListener{
            listener.editHouseClicked(model)
        }
        holder.ivDeleteButton.setOnClickListener{
            listener.deleteHouseClicked(model)
        }
        holder.cvOwnHome.setOnClickListener {
            listener.onClickedAddHome(model)
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        Log.d(TAG, "OnDataChanged View holder has called")
        if (snapshots.isEmpty()) {
            Log.d(TAG, "List is empty")
            listener.isListEmpty(true)
        } else {
            Log.d(TAG, "List is not empty")
            listener.isListEmpty(false)
        }
    }
}

interface OnClickedAddHome {
    fun onClickedAddHome(home: Home)
    fun deleteHouseClicked(home: Home)
    fun editHouseClicked(home: Home)
    fun isListEmpty(value: Boolean)
}

