package com.example.realstateapp.ui.fragment.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realstateapp.adapter.HomeAdapter
import com.example.realstateapp.adapter.HomeListener
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.data.remote.Values
import com.example.realstateapp.databinding.FragmentHomeBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.Constant
import com.example.realstateapp.utils.Resources
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore


class HomeFragment : Fragment(), HomeListener {

    private val TAG: String = "HomeFragment"
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var topHouseAdapter: HomeAdapter
    private lateinit var statesHouseAdapter: HomeAdapter
    private lateinit var citiesHouseAdapter: HomeAdapter
    private lateinit var latestHouseAdapter: HomeAdapter
    private val mInstance = FirebaseFirestore.getInstance()
    private var mState = ""
    private var mCity = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as RealStateActivity).viewModel.getUserAvailable()

        latestHouseAdapter = HomeAdapter((activity as RealStateActivity), this)
        topHouseAdapter = HomeAdapter((activity as RealStateActivity), this)
        statesHouseAdapter = HomeAdapter((activity as RealStateActivity), this)
        citiesHouseAdapter = HomeAdapter((activity as RealStateActivity), this)


        binding.rvLatestHouses.adapter = latestHouseAdapter
        binding.rvLatestHouses.layoutManager =
            LinearLayoutManager(
                (activity as RealStateActivity),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.rvTopHouses.adapter = topHouseAdapter
        binding.rvTopHouses.layoutManager =
            LinearLayoutManager(
                (activity as RealStateActivity),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.rvStates.adapter = statesHouseAdapter
        binding.rvStates.layoutManager =
            LinearLayoutManager(
                (activity as RealStateActivity),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.rvCities.adapter = citiesHouseAdapter
        binding.rvCities.layoutManager =
            LinearLayoutManager(
                (activity as RealStateActivity),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        (activity as RealStateActivity).viewModel.liveUser.observe(
            viewLifecycleOwner, { user ->
                if (user != null) {
                    val state = user.state
                    val city = user.city
                    mState = state
                    mCity = city
                    Log.d(TAG, "Home $user")
                    Log.d(TAG, "State $mState")
                    Log.d(TAG, "City $mCity")
                    (activity as RealStateActivity).viewModel.getTopHouse()
                    (activity as RealStateActivity).viewModel.getLatestHouse()
                    (activity as RealStateActivity).viewModel.getCitiesHouse(city)
                    (activity as RealStateActivity).viewModel.getStateHouse(state)
                }
            }
        )

        (activity as RealStateActivity).viewModel.topList.observe(
            viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        hideTopHouseProgressBar()
                        response.data?.let { data ->
                            Log.d(TAG, "Getter data $data")
                            if (data.isNotEmpty()) {
                                topHouseAdapter.differ.submitList(data)
                            } else {
                                hideTopHouseProgressBar()
                                topHouseAdapter.differ.submitList(emptyList())
                            }
                        }
                    }
                    is Resources.Error -> {
                        hideTopHouseProgressBar()
                        response.message?.let { message ->
                            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is Resources.Loading -> {
                        showTopHouseProgressBar()
                    }
                }
            })

        (activity as RealStateActivity).viewModel.stateList.observe(
            viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        hideTopHouseProgressBar()
                        response.data?.let { data ->
                            Log.d(TAG, "Getter data $data")
                            if (data.isNotEmpty()) {
                                statesHouseAdapter.differ.submitList(data)
                            } else {
                                hideTopHouseProgressBar()
                                statesHouseAdapter.differ.submitList(emptyList())
                            }
                        }
                    }
                    is Resources.Error -> {
                        hideTopHouseProgressBar()
                        response.message?.let { message ->
                            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is Resources.Loading -> {
                        showTopHouseProgressBar()
                    }
                }
            })

        (activity as RealStateActivity).viewModel.cityList.observe(
            viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        hideTopHouseProgressBar()
                        response.data?.let { data ->
                            Log.d(TAG, "Getter data $data")
                            if (data.isNotEmpty()) {
                                citiesHouseAdapter.differ.submitList(data)
                            } else {
                                hideTopHouseProgressBar()
                                citiesHouseAdapter.differ.submitList(emptyList())
                            }
                        }
                    }
                    is Resources.Error -> {
                        hideTopHouseProgressBar()
                        response.message?.let { message ->
                            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is Resources.Loading -> {
                        showTopHouseProgressBar()
                    }
                }
            })

        (activity as RealStateActivity).viewModel.latestList.observe(
            viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        hideTopHouseProgressBar()
                        response.data?.let { data ->
                            Log.d(TAG, "Getter data $data")
                            if (data.isNotEmpty()) {
                                latestHouseAdapter.differ.submitList(data)
                            } else {
                                hideTopHouseProgressBar()
                                latestHouseAdapter.differ.submitList(emptyList())
                            }
                        }
                    }
                    is Resources.Error -> {
                        hideTopHouseProgressBar()
                        response.message?.let { message ->
                            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is Resources.Loading -> {
                        showTopHouseProgressBar()
                    }
                }
            })

        binding.latestLayout.setOnClickListener {
            try {
                Log.d(TAG, "Latest Clicked")
                val values = Values(Constant.HOME, Constant.HOME_COLLECTION_LATEST)
                val action = HomeFragmentDirections.actionHomeFragmentToMoreHomesFragment(values)
                findNavController().navigate(action)
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "$e")
            }
        }

        binding.topLayout.setOnClickListener {
            try {
                Log.d(TAG, "Top Clicked")
                val values = Values("", Constant.HOME_COLLECTION_TOP)
                val action = HomeFragmentDirections.actionHomeFragmentToMoreHomesFragment(values)
                findNavController().navigate(action)
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "$e")
            }
        }

        binding.stateLayout.setOnClickListener {
            try {
                if (mState != "") {
                    Log.d(TAG, "State Clicked")
                    val values = Values(mState, Constant.HOME_COLLECTION_STATE)
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToMoreHomesFragment(values)
                    findNavController().navigate(action)
                }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "$e")
            }
        }

        binding.cityLayout.setOnClickListener {
            try {
                if (mCity != "") {
                    Log.d(TAG, "City Clicked")
                    val values = Values(mCity, Constant.HOME_COLLECTION_CITY)
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToMoreHomesFragment(values)
                    findNavController().navigate(action)
                }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "$e")
            }
        }
    }

    private fun showTopHouseProgressBar() {

    }

    private fun hideTopHouseProgressBar() {

    }

    override fun onResume() {
        super.onResume()
        (activity as RealStateActivity).viewModel.getUserAvailable()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClickedHome(home: Home) {
        try {
            val action =
                HomeFragmentDirections.actionHomeFragmentToHouseDetailFragment(home)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    override fun onClickedLiked(home: Home, value: Boolean) {
        if (value) {
            (activity as RealStateActivity).viewModel.addLikedHome(home = home)
            (activity as RealStateActivity).viewModel.addFavorite(home = home)
        } else {
            (activity as RealStateActivity).viewModel.removeLikedHome(home = home)
            (activity as RealStateActivity).viewModel.removeFavorite(home = home)
        }
    }

}