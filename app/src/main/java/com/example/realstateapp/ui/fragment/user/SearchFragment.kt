package com.example.realstateapp.ui.fragment.user

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realstateapp.adapter.AddHomeAdapter
import com.example.realstateapp.adapter.SearchHouseAdapter
import com.example.realstateapp.adapter.SearchHouseHasClicked
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.databinding.FragmentSearchBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant
import com.example.realstateapp.utils.Constant.SEARCH_HOUSE_TIME_DELAY
import com.example.realstateapp.utils.Constant.TITLE
import com.example.realstateapp.utils.Resources
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment(), SearchHouseHasClicked {

    private val TAG = "SearchFragment"
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var searchAdapter: SearchHouseAdapter? = null
    private val firebaseInstance = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        setUpRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvNoResult.visibility = View.GONE
        var job: Job? = null
        val viewModel = (activity as RealStateActivity).viewModel
        (activity as RealStateActivity).binding.searchHouseTextInputEditText.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_HOUSE_TIME_DELAY)
                editable?.let {
                    try {
                        if (editable.toString().isNotEmpty()) {
                            val search = editable.toString().lowercase()
                            if (!isFilterValueIsEmpty()) {
                                alreadyWeHaveToPerformSearchQueryFromFilter(search)
                            } else {
                                viewModel.getSearchData(search)
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
        (activity as RealStateActivity).viewModel.searchHouseData.observe(
            viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        hideSearchProgressBar()
                        response.data?.let { data ->
                            Log.d(TAG, "Getter data $data")
                            if (data.isNotEmpty()) {
                                binding.cvNoResult.visibility = View.GONE
                                searchAdapter?.differ?.submitList(data)
                            } else {
                                hideSearchProgressBar()
                                searchAdapter?.differ?.submitList(emptyList())
                                binding.cvNoResult.visibility = View.VISIBLE
                            }
                        }
                    }
                    is Resources.Error -> {
                        hideSearchProgressBar()
                        response.message?.let { message ->
                            binding.cvNoResult.visibility = View.VISIBLE
                            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                    is Resources.Loading -> {
                        showSearchProgressBar()
                    }
                }
            })
        alreadyWeHaveToPerformSearchQueryFromFilter()
    }

    private fun isFilterValueIsEmpty(): Boolean {
        val propertyType = (activity as RealStateActivity).viewModel.getPropertyType()
        val residentialType = (activity as RealStateActivity).viewModel.getResidentalType()
        val bhk = (activity as RealStateActivity).viewModel.getBhk()
        val state = (activity as RealStateActivity).viewModel.getState()
        val city = (activity as RealStateActivity).viewModel.getCity()
        val min = (activity as RealStateActivity).viewModel.getMin()
        val max = (activity as RealStateActivity).viewModel.getMax()
        if (
            propertyType == "" &&
            residentialType == "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            return true
        }
        return false
    }

    private fun alreadyWeHaveToPerformSearchQueryFromFilter(query: String): Boolean {
        val propertyType = (activity as RealStateActivity).viewModel.getPropertyType()
        val residentialType = (activity as RealStateActivity).viewModel.getResidentalType()
        val bhk = (activity as RealStateActivity).viewModel.getBhk()
        val state = (activity as RealStateActivity).viewModel.getState()
        val city = (activity as RealStateActivity).viewModel.getCity()
        val min = (activity as RealStateActivity).viewModel.getMin()
        val max = (activity as RealStateActivity).viewModel.getMax()

        Log.d(TAG, "All values $propertyType $residentialType $bhk $state $city $min $max")

        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply with query")
            (activity as RealStateActivity).viewModel.searchPropertyType(query, propertyType)
        }
        if (
            query != "" &&
            propertyType != "" &&
            residentialType != "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search with re apply q")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialType(
                query,
                propertyType,
                residentialType
            )
        }
        if (
            query != "" &&
            propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhk(
                query,
                propertyType,
                residentialType,
                bhk
            )
        }
        if (
            query != "" &&
            propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhkAsState(
                query,
                propertyType,
                residentialType,
                bhk,
                state
            )
        }
        if (
            query != "" &&
            propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
                query,
                propertyType,
                residentialType,
                bhk,
                state,
                city
            )
        }

        if (
            query != "" &&
            propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
                query,
                propertyType,
                residentialType,
                bhk,
                state,
                city,
                min,
                max
            )
        }
        if (
            query != "" &&
            propertyType == "" &&
            residentialType == "" &&
            bhk != "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchBhk(query, bhk)
        }
        if (
            query != "" &&
            propertyType == "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchBhkAsState(query, bhk, state)
        }
        if (
            query != "" &&
            propertyType == "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchBhkAsStateAsCity(
                query,
                bhk,
                state,
                city
            )
        }

        if (
            query != "" &&
            propertyType == "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchState(query, state)
        }

        if (
            query != "" &&
            propertyType == "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchStateAsCity(query, state, city)
        }


        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhk(
                query,
                propertyType,
                bhk
            )
        }

//        searchPropertyTypeAsBhkAsState

        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhkAsState(
                query,
                propertyType,
                bhk,
                state
            )
        }

//        searchPropertyTypeAsBhkAsStateAsCity
//        searchPropertyTypeAsBhkAsStateAsCityAsMinMax
        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhkAsStateAsCity(
                query,
                propertyType,
                bhk,
                state,
                city
            )
        }

//        searchPropertyTypeAsBhkAsStateAsCityAsMinMax

        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
                query,
                propertyType,
                bhk,
                state,
                city,
                min,
                max
            )
        }

//        searchPropertyTypeAsState

        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsState(
                query,
                propertyType,
                state
            )
        }

//        searchPropertyTypeAsStateAsCity
        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsStateAsCity(
                query,
                propertyType,
                state,
                city
            )
        }

//        searchPropertyTypeAsStateAsCityAsMinMax
        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city != "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsStateAsCityAsMinMax(
                query,
                propertyType,
                state,
                city,
                min,
                max
            )
        }

//        searchPropertyTypeAsMinMax
        if (
            query != "" &&
            propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsMinMax(
                query,
                propertyType,
                min,
                max
            )
        }

        return false
    }

    private fun alreadyWeHaveToPerformSearchQueryFromFilter(): Boolean {
        val propertyType = (activity as RealStateActivity).viewModel.getPropertyType()
        val residentialType = (activity as RealStateActivity).viewModel.getResidentalType()
        val bhk = (activity as RealStateActivity).viewModel.getBhk()
        val state = (activity as RealStateActivity).viewModel.getState()
        val city = (activity as RealStateActivity).viewModel.getCity()
        val min = (activity as RealStateActivity).viewModel.getMin()
        val max = (activity as RealStateActivity).viewModel.getMax()

        Log.d(TAG, "All values $propertyType $residentialType $bhk $state $city $min $max")
        if (propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyType(propertyType)
        }
        if (propertyType != "" &&
            residentialType != "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialType(
                propertyType,
                residentialType
            )
        }
        if (propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhk(
                propertyType,
                residentialType,
                bhk
            )
        }
        if (propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhkAsState(
                propertyType,
                residentialType,
                bhk,
                state
            )
        }
        if (propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
                propertyType,
                residentialType,
                bhk,
                state,
                city
            )
        }

        if (propertyType != "" &&
            residentialType != "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min != 0 &&
            max != 0
        ) {

            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
                propertyType,
                residentialType,
                bhk,
                state,
                city,
                min,
                max
            )
        }
        if (propertyType == "" &&
            residentialType == "" &&
            bhk != "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchBhk(bhk)
        }
        if (propertyType == "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchBhkAsState(bhk, state)
        }
        if (propertyType == "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchBhkAsStateAsCity(
                bhk,
                state,
                city
            )
        }

        if (propertyType == "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchState(state)
        }

        if (propertyType == "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchStateAsCity(state, city)
        }
//        searchPropertyTypeAsBhk
        if (propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state == "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhk(propertyType, bhk)
        }

//        searchPropertyTypeAsBhkAsState

        if (propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhkAsState(
                propertyType,
                bhk,
                state
            )
        }

//        searchPropertyTypeAsBhkAsStateAsCity
//        searchPropertyTypeAsBhkAsStateAsCityAsMinMax
        if (propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhkAsStateAsCity(
                propertyType,
                bhk,
                state,
                city
            )
        }

//        searchPropertyTypeAsBhkAsStateAsCityAsMinMax

        if (propertyType != "" &&
            residentialType == "" &&
            bhk != "" &&
            state != "" &&
            city != "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
                propertyType,
                bhk,
                state,
                city,
                min,
                max
            )
        }

//        searchPropertyTypeAsState

        if (propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city == "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsState(propertyType, state)
        }

//        searchPropertyTypeAsStateAsCity
        if (propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city != "" &&
            min == 0 &&
            max == 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsStateAsCity(
                propertyType,
                state,
                city
            )
        }

//        searchPropertyTypeAsStateAsCityAsMinMax
        if (propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state != "" &&
            city != "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsStateAsCityAsMinMax(
                propertyType,
                state,
                city,
                min,
                max
            )
        }
//        searchPropertyTypeAsMinMax
        if (propertyType != "" &&
            residentialType == "" &&
            bhk == "" &&
            state == "" &&
            city == "" &&
            min != 0 &&
            max != 0
        ) {
            Log.d(TAG, "property search apply")
            (activity as RealStateActivity).viewModel.searchPropertyTypeAsMinMax(
                propertyType,
                min,
                max
            )
        }
        return false
    }

    private fun showSearchProgressBar() {
        binding.cvNoResult.visibility = View.GONE
        binding.spinKit.visibility = View.VISIBLE
    }

    private fun hideSearchProgressBar() {
        binding.spinKit.visibility = View.INVISIBLE
    }

    private fun setUpRecyclerView() {
        searchAdapter = SearchHouseAdapter((activity as RealStateActivity),this)
        binding.searchHouseRecyclerView.adapter = searchAdapter
        val layoutManager = LinearLayoutManager((activity as RealStateActivity))
        binding.searchHouseRecyclerView.layoutManager = layoutManager
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun searchHouseHasClicked(home: Home) {
        try {
            val action = SearchFragmentDirections.actionSearchHouseFragmentToHouseDetailFragment(home)
            findNavController().navigate(action)
        }catch (e: Exception){
            Log.d(TAG,"$e")
        }
    }

    override fun likedIsClicked(home: Home, value: Boolean) {
        if (value) {
            (activity as RealStateActivity).viewModel.addLikedHome(home = home)
            (activity as RealStateActivity).viewModel.addFavorite(home = home)
        } else {
            (activity as RealStateActivity).viewModel.removeLikedHome(home = home)
            (activity as RealStateActivity).viewModel.removeFavorite(home = home)
        }
    }

}