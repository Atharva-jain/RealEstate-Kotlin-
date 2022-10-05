package com.example.realstateapp.ui.fragment.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realstateapp.R
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.databinding.FragmentMoreHomesBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.Constant
import com.example.realstateapp.utils.Constant.CITY
import com.example.realstateapp.utils.Constant.FAVORITE
import com.example.realstateapp.utils.Constant.HISTORY
import com.example.realstateapp.utils.Constant.HOME
import com.example.realstateapp.utils.Constant.PRICE
import com.example.realstateapp.utils.Constant.STATE
import com.example.realstateapp.utils.Constant.TIMESNAP
import com.example.realstateapp.utils.ConvertDigitIntoValues
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MoreHomesFragment : Fragment() {

    private lateinit var mAdapter: FirestorePagingAdapter<Home, MoreHomesFragment.PagingViewHolder>
    private var _binding: FragmentMoreHomesBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<MoreHomesFragmentArgs>()
    private val mInstance = FirebaseFirestore.getInstance()
    private val mHomeCollection = mInstance.collection(HOME)
    private val mFavoriteCollection = mInstance.collection(FAVORITE)
    private val mHistoryCollection = mInstance.collection(HISTORY)
    private lateinit var query: Query

    //  private lateinit var adapter: FirestorePagingAdapter
    private val TAG = "MoreHomesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMoreHomesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val value = args.collection
        val collection = value.collection
        val type = value.type
        Log.d(TAG,"$value $collection $type ${Constant.HOME_COLLECTION_LATEST}")
        when (type) {
            Constant.HOME_COLLECTION_LATEST -> {
                Log.d(TAG, "Latest are requested")
                query = mHomeCollection.orderBy(TIMESNAP, Query.Direction.DESCENDING)
            }
            Constant.HOME_COLLECTION_TOP -> {
                Log.d(TAG, "Top are requested")
                query = mHomeCollection.orderBy(PRICE)
            }
            Constant.HOME_COLLECTION_STATE -> {
                Log.d(TAG, "State are requested")
                query = mHomeCollection.orderBy(PRICE).whereEqualTo(STATE, type)
            }
            Constant.HOME_COLLECTION_CITY -> {
                Log.d(TAG, "City are requested")
                query = mHomeCollection.orderBy(PRICE).whereEqualTo(CITY, type)
            }
            Constant.FAVORITE_COLLECTION -> {
                Log.d(TAG, "Favorite are requested")
                query = mFavoriteCollection.orderBy(TIMESNAP, Query.Direction.DESCENDING)
            }
            Constant.HISTORY_COLLECTION -> {
                Log.d(TAG, "History are requested")
                query = mHistoryCollection.orderBy(TIMESNAP, Query.Direction.DESCENDING)
            }
            else -> {
                Log.d(TAG, "Else has run")
                query = mHomeCollection.orderBy(TIMESNAP, Query.Direction.DESCENDING)
            }
        }

        val pagedList = PagedList.Config.Builder()
            .setInitialLoadSizeHint(10)
            .setPageSize(3)
            .build()

        val options: FirestorePagingOptions<Home> = FirestorePagingOptions.Builder<Home>()
            .setQuery(query, pagedList, Home::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<Home, PagingViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagingViewHolder {
                return PagingViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.house_layout, parent, false
                    )
                )
            }

            override fun onBindViewHolder(holder: PagingViewHolder, position: Int, model: Home) {
                val url = model.frontHouseImage
                Glide.with(holder.ivHomeImage.context).load(url).into(holder.ivHomeImage)
                holder.tvTitle.text = model.title
                holder.tvAddress.text = model.address
                val price =
                    ConvertDigitIntoValues.setValueAccordingToLacCr(model.priceType, model.price)
                val houseIsLiked =
                    (activity as RealStateActivity).viewModel.checkHomeLikedOrNot(model)
                holder.cbLiked.isChecked = houseIsLiked
                holder.tvPrice.text = "$price ${model.priceType.uppercase()}";
                holder.cvHome.setOnClickListener {
                    navigate(model)
                }
                holder.cbLiked.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        addFavorite(model)
                    } else {
                        removeFavorite(model)
                    }
                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                        Log.d(TAG,"Loading state")
                    }

                    LoadingState.LOADING_MORE -> {
                        Log.d(TAG,"Loading More")
                    }

                    LoadingState.LOADED -> {
                        Log.d(TAG,"Loaded")
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG,"Loading Error ")
                    }

                    LoadingState.FINISHED -> {
                        Log.d(TAG,"Loading Finish")
                    }
                }
            }
        }

        val gridLayout = GridLayoutManager((activity as RealStateActivity), 2)
        binding.rvSeeMore.adapter = mAdapter
        mAdapter.startListening()
        binding.rvSeeMore.layoutManager = gridLayout

    }

    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    inner class PagingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvHome: CardView = itemView.findViewById(R.id.cv_house)
        val ivHomeImage: ImageView = itemView.findViewById(R.id.iv_house)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_address)
        val cbLiked: CheckBox = itemView.findViewById(R.id.cb_liked)
    }

    private fun navigate(home: Home) {
        try {
            val action =
                MoreHomesFragmentDirections.actionMoreHomesFragmentToHouseDetailFragment(home)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    private fun addFavorite(home: Home) {
        (activity as RealStateActivity).viewModel.addLikedHome(home = home)
        (activity as RealStateActivity).viewModel.addFavorite(home = home)
    }

    private fun removeFavorite(home: Home) {
        (activity as RealStateActivity).viewModel.removeLikedHome(home = home)
        (activity as RealStateActivity).viewModel.removeFavorite(home = home)
    }

}