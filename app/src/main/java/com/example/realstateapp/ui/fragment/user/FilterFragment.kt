package com.example.realstateapp.ui.fragment.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.realstateapp.R
import com.example.realstateapp.RealStateNavDirections
import com.example.realstateapp.databinding.FragmentFilterBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant.BUY
import com.example.realstateapp.utils.Constant.RENT
import com.example.realstateapp.utils.Constant.WARNING
import com.google.android.material.snackbar.Snackbar


class FilterFragment : Fragment() {


    private var mRentMinOrMax: Array<String> = emptyArray()
    private var mBuyMinOrMax: Array<String> = emptyArray()
    private val TAG: String = "FilterFragment"
    private var _binding: FragmentFilterBinding? = null
    private var mState = ""
    private var mCity = ""
    private var mPropertyType = ""
    private var mResidentialType = ""
    private var mMin = ""
    private var mMax = ""
    private var mBHK = ""
    private var mRupeesMin = 0
    private var mRupeesMax = 0

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnItemClickedListener()

        val propertyType = (activity as RealStateActivity).viewModel.getPropertyType()
        val stateS = (activity as RealStateActivity).viewModel.getState()
        binding.filterPropertyTypeText.setText((activity as RealStateActivity).viewModel.getPropertyType())
        binding.filterResidentialTypeText.setText((activity as RealStateActivity).viewModel.getResidentalType())
        binding.filterBhkText.setText((activity as RealStateActivity).viewModel.getBhk())
        binding.filterStateText.setText(stateS)
        binding.filterCityText.setText((activity as RealStateActivity).viewModel.getCity())

        val state = resources.getStringArray(R.array.state)
        val stateArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            state
        )
        binding.filterStateText.setAdapter(stateArrayAdapter)

        val sell_rent = resources.getStringArray(R.array.property_buy_types)
        val sell_rent_ArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            sell_rent
        )
        binding.filterPropertyTypeText.setAdapter(sell_rent_ArrayAdapter)

        val BHK = resources.getStringArray(R.array.bhk_types)
        val bhkArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            BHK
        )
        binding.filterBhkText.setAdapter(bhkArrayAdapter)

        binding.filterButton.setOnClickListener {
            try {
                if (CheckInternetConnection.hasInternetConnection(requireContext())) {
//                    dataValueIsChanged()
                    val action = RealStateNavDirections.actionGlobalSearchFragment()
                    findNavController().navigate(action)

                } else {
                    Snackbar.make(requireView(), WARNING, Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
            }
        }

        if (propertyType != "") {
            setResidentialAccordingPropertyType(propertyType)
            Log.d(TAG, "Property is not Empty $propertyType")
            if (propertyType == BUY) {
                val min = (activity as RealStateActivity).viewModel.getMin().toString()
                Log.d(TAG, "Min Value in Buy ${getValueAccordingToBuy(min)} $min")
                binding.filterMinText.setText(getValueAccordingToBuy(min))
                val max = (activity as RealStateActivity).viewModel.getMax().toString()
                Log.d(TAG, "Max Value in Buy ${getValueAccordingToBuy(max)} $max")
                binding.filterMaxText.setText(getValueAccordingToBuy(max))
            } else if (propertyType == RENT) {
                val min = (activity as RealStateActivity).viewModel.getMin().toString()
                Log.d(TAG, "Min Value in Buy ${getValueAccordingToRent(min)} $min")
                binding.filterMinText.setText(getValueAccordingToRent(min))
                val max = (activity as RealStateActivity).viewModel.getMax().toString()
                Log.d(TAG, "Min Value in Buy ${getValueAccordingToRent(max)} $max")
                binding.filterMaxText.setText(getValueAccordingToRent(max))
            }
            setMinOrMaxPriceAccordingToPropertyType(propertyType)
        }
        if (stateS != "") {
            setCityAccordingState(stateS)
        }

        binding.cvClear.setOnClickListener {
            (activity as RealStateActivity).viewModel.setPropertyType("")
            (activity as RealStateActivity).viewModel.setResidentalType("")
            (activity as RealStateActivity).viewModel.setBhk("")
            (activity as RealStateActivity).viewModel.setState("")
            (activity as RealStateActivity).viewModel.setCity("")
            (activity as RealStateActivity).viewModel.setMax(0)
            (activity as RealStateActivity).viewModel.setMin(0)
            binding.filterPropertyTypeText.setText("")
            binding.filterResidentialTypeText.setText("")
            binding.filterBhkText.setText("")
            binding.filterStateText.setText("")
            binding.filterCityText.setText("")
            binding.filterMaxText.setText("")
            binding.filterMinText.setText("")
        }

        Log.d(TAG, "Values of $")

    }

    private fun dataValueIsChanged(): Boolean {
        val propertyType = (activity as RealStateActivity).viewModel.getPropertyType()
        val residentType = (activity as RealStateActivity).viewModel.getResidentalType()
        val bhk = (activity as RealStateActivity).viewModel.getBhk()
        val city = (activity as RealStateActivity).viewModel.getCity()
        val state = (activity as RealStateActivity).viewModel.getState()
        val min = (activity as RealStateActivity).viewModel.getMin()
        val max = (activity as RealStateActivity).viewModel.getMax()
        Log.d(TAG,"Before $propertyType $residentType $bhk $city $state $min $max")
        Log.d(TAG,"Before $mPropertyType $mResidentialType $mBHK $mCity $mState $mMin $mMax")
        if (propertyType == "" || mPropertyType != propertyType) {

            (activity as RealStateActivity).viewModel.setPropertyType(mPropertyType)
        }
        if (residentType == "" || mResidentialType != residentType) {
            (activity as RealStateActivity).viewModel.setResidentalType(mResidentialType)
        }
        if (bhk == "" || mBHK != bhk) {
            (activity as RealStateActivity).viewModel.setBhk(mBHK)
        }
        if (state == "" || mState != state) {
            (activity as RealStateActivity).viewModel.setState(mState)
        }
        if (city == "" || mCity != city) {
            (activity as RealStateActivity).viewModel.setCity(mCity)
        }
        if (max == 0 || mRupeesMax != max) {
            (activity as RealStateActivity).viewModel.setMax(mRupeesMax)
        }
        if (min == 0 || mRupeesMin != min) {
            (activity as RealStateActivity).viewModel.setMin(mRupeesMin)
        }
        Log.d(TAG,"After $propertyType $residentType $bhk $city $state $min $max")
        Log.d(TAG,"After $mPropertyType $mResidentialType $mBHK $mCity $mState $mMin $mMax")
        return true
    }

    private fun setOnItemClickedListener() {
        try {
            binding.filterStateText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mState = parent.getItemAtPosition(position).toString()
                    Log.d(TAG, mState)
                    binding.filterCityText.setText("")
                    mCity = ""
                    setCityAccordingState(mState)
                    (activity as RealStateActivity).viewModel.setState(mState)
                    (activity as RealStateActivity).viewModel.setCity(mCity)
                }
            binding.filterCityText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mCity = parent.getItemAtPosition(position).toString()
                    Log.d(TAG, mCity)
                    (activity as RealStateActivity).viewModel.setCity(mCity)
                }
            binding.filterPropertyTypeText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mPropertyType = parent.getItemAtPosition(position).toString()
                    Log.d(TAG, mPropertyType)
                    mResidentialType = ""
                    binding.filterResidentialTypeText.setText("")
                    binding.filterMaxText.setText("")
                    binding.filterMinText.setText("")
                    setResidentialAccordingPropertyType(mPropertyType)
                    setMinOrMaxPriceAccordingToPropertyType(mPropertyType)
                    (activity as RealStateActivity).viewModel.setPropertyType(mPropertyType)
                    (activity as RealStateActivity).viewModel.setResidentalType(mResidentialType)
                    (activity as RealStateActivity).viewModel.setMax(0)
                    (activity as RealStateActivity).viewModel.setMin(0)
                }
            binding.filterResidentialTypeText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mResidentialType = parent.getItemAtPosition(position).toString()
                    Log.d(TAG, mResidentialType)
                    (activity as RealStateActivity).viewModel.setResidentalType(mResidentialType)
                }
            binding.filterMinText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mMin = parent.getItemAtPosition(position).toString()
                    if (mPropertyType == "Buy") {
                        val value = mBuyMinOrMax[position]
                        Log.d(TAG, "IS $value on Buy")
                        mRupeesMin = addZerosAccordingToBuyValue(value)
                        Log.d(TAG, "IS Rupees Saved In Buy $mRupeesMin")
                        (activity as RealStateActivity).viewModel.setMin(mRupeesMin)
                    } else if (mPropertyType == "Rent") {
                        val value = mRentMinOrMax[position]
                        Log.d(TAG, "IS $value on Rent")
                        mRupeesMin = addZerosAccordingToRentValue(value)
                        Log.d(TAG, "IS Rupees Saved In Rent $mRupeesMin")
                        (activity as RealStateActivity).viewModel.setMin(mRupeesMin)
                    }
                }
            binding.filterMaxText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mMax = parent.getItemAtPosition(position).toString()
                    if (mPropertyType == "Buy") {
                        val value = mBuyMinOrMax[position]
                        Log.d(TAG, "IS $value on Buy")
                        mRupeesMax = addZerosAccordingToBuyValue(value)
                        Log.d(TAG, "IS Rupees Saved In Buy $mRupeesMax")
                        (activity as RealStateActivity).viewModel.setMax(mRupeesMax)
                    } else if (mPropertyType == "Rent") {
                        val value = mRentMinOrMax[position]
                        Log.d(TAG, "IS $value on Rent")
                        mRupeesMax = addZerosAccordingToRentValue(value)
                        Log.d(TAG, "IS Rupees Saved in Rent $mRupeesMax")
                        (activity as RealStateActivity).viewModel.setMax(mRupeesMax)
                    }
                }
            binding.filterBhkText.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    mBHK = parent.getItemAtPosition(position).toString()
                    Log.d(TAG, mBHK)
                    (activity as RealStateActivity).viewModel.setBhk(mBHK)
                }
        } catch (e: Exception) {
        }
    }

    private fun addZerosAccordingToRentValue(value: String): Int {
        var rs = 0
        when (value) {
            "5000" -> {
                rs = 5000
            }
            "10000" -> {
                rs = 10000
            }
            "15000" -> {
                rs = 15000
            }
            "20000" -> {
                rs = 20000
            }
            "25000" -> {
                rs = 25000
            }
            "30000" -> {
                rs = 30000
            }
            "35000" -> {
                rs = 35000
            }
            "40000" -> {
                rs = 40000
            }
            "50000" -> {
                rs = 50000
            }
            "60000" -> {
                rs = 60000
            }
            "85000" -> {
                rs = 85000
            }
            "1" -> {
                rs = 100000
            }
            "1.5" -> {
                rs = 150000
            }
            "2" -> {
                rs = 200000
            }
            "4" -> {
                rs = 400000
            }
            "7" -> {
                rs = 700000
            }
            "10" -> {
                rs = 1000000
            }
        }
        return rs
    }

    private fun addZerosAccordingToBuyValue(value: String): Int {
        var rs = 0
        when (value) {
            "5" -> {
                rs = 500000

            }
            "10" -> {
                rs = 1000000
            }
            "20" -> {
                rs = 2000000

            }
            "30" -> {
                rs = 3000000

            }
            "40" -> {
                rs = 4000000
            }
            "50" -> {
                rs = 5000000
            }
            "60" -> {
                rs = 6000000
            }
            "70" -> {
                rs = 7000000
            }
            "80" -> {
                rs = 8000000
            }
            "90" -> {
                rs = 9000000
            }
            "1" -> {
                rs = 10000000
            }
            "1.2" -> {
                rs = 12000000
            }
            "1.4" -> {
                rs = 14000000
            }
            "1.6" -> {
                rs = 16000000
            }
            "1.8" -> {
                rs = 18000000
            }
            "2" -> {
                rs = 20000000
            }
            "2.3" -> {
                rs = 23000000
            }
            "2.6" -> {
                rs = 26000000
            }
            "3" -> {
                rs = 30000000
            }
            "3.5" -> {
                rs = 35000000
            }
            "4" -> {
                rs = 40000000
            }
            "4.5" -> {
                rs = 45000000
            }
            "5.2" -> {
                rs = 52000000
            }
            "10.2" -> {
                rs = 10200000
            }
            "20.2" -> {
                rs = 20200000
            }
        }
        return rs
    }

    private fun setCityAccordingState(state: String) {
        when (state) {
            "Andhra Pradesh" -> {
                val cities = resources.getStringArray(R.array.andhra_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Arunachal Pradesh" -> {
                val cities = resources.getStringArray(R.array.arunachal_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Assam" -> {
                val cities = resources.getStringArray(R.array.assam_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Bihar" -> {
                val cities = resources.getStringArray(R.array.bihar_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Chandigarh" -> {
                val cities = resources.getStringArray(R.array.chandigarh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Chhattisgarh" -> {
                val cities = resources.getStringArray(R.array.chhattisgard_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Dadra and Nagar Haveli" -> {
                val cities = resources.getStringArray(R.array.dadra_and_nagar_haveli_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Daman and Diu" -> {
                val cities = resources.getStringArray(R.array.daman_and_Diu_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Delhi" -> {
                val cities = resources.getStringArray(R.array.delhi_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Goa" -> {
                val cities = resources.getStringArray(R.array.goa_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Gujarat" -> {
                val cities = resources.getStringArray(R.array.gujarat_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Haryana" -> {
                val cities = resources.getStringArray(R.array.haryana_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Himachal Pradesh" -> {
                val cities = resources.getStringArray(R.array.himachal_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Jammu and Kashmir (union territory)" -> {
                val cities = resources.getStringArray(R.array.jammu_and_kashmir_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Jharkhand" -> {
                val cities = resources.getStringArray(R.array.jharkhand_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Karnataka" -> {
                val cities = resources.getStringArray(R.array.karnataka_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Kerala" -> {
                val cities = resources.getStringArray(R.array.kerala_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Ladakh (union territory)" -> {
                val cities = resources.getStringArray(R.array.ladakh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Madhya Pradesh" -> {
                val cities = resources.getStringArray(R.array.madhya_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Maharashtra" -> {
                val cities = resources.getStringArray(R.array.maharashtra_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Manipur" -> {
                val cities = resources.getStringArray(R.array.manipur_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Meghalaya" -> {
                val cities = resources.getStringArray(R.array.meghalaya_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Mizoram" -> {
                val cities = resources.getStringArray(R.array.mizoram_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Nagaland" -> {
                val cities = resources.getStringArray(R.array.nagaland_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Odisha" -> {
                val cities = resources.getStringArray(R.array.odisha_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Puducherry (union territory)" -> {
                val cities = resources.getStringArray(R.array.puducherry_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Punjab" -> {
                val cities = resources.getStringArray(R.array.punjab_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Rajasthan" -> {
                val cities = resources.getStringArray(R.array.rajasthan_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Sikkim" -> {
                val cities = resources.getStringArray(R.array.sikkim_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Tamil Nadu" -> {
                val cities = resources.getStringArray(R.array.tamil_nadu_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Telangana" -> {
                val cities = resources.getStringArray(R.array.telangana_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Tripura" -> {
                val cities = resources.getStringArray(R.array.tripura_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Uttar Pradesh" -> {
                val cities = resources.getStringArray(R.array.uttar_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }
            "Uttarakhand" -> {
                val cities = resources.getStringArray(R.array.uttarakhand_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }

            "West Bengal" -> {
                val cities = resources.getStringArray(R.array.west_bengal_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterCityText.setAdapter(arrayAdapter)
            }

        }
    }

    private fun setResidentialAccordingPropertyType(propertyType: String) {
        Log.d(TAG, "SetResidential function called $propertyType")
        when (propertyType) {
            "Buy" -> {
                Log.d(TAG, "SetResidential function called Sell $propertyType")
                val cities = resources.getStringArray(R.array.residential_sell_types)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterResidentialTypeText.setAdapter(arrayAdapter)
            }
            "Rent" -> {
                Log.d(TAG, "SetResidential function called Rent $propertyType")
                val cities = resources.getStringArray(R.array.residential_rent_types)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.filterResidentialTypeText.setAdapter(arrayAdapter)
            }
        }
    }

    private fun setMinOrMaxPriceAccordingToPropertyType(propertyType: String) {
        Log.d(TAG, "SetResidential function called $propertyType")
        when (propertyType) {
            "Buy" -> {
                Log.d(TAG, "SetResidential function called Buy $propertyType")
                mBuyMinOrMax = resources.getStringArray(R.array.buy_price)
                val displayList = convertBuyValueIntoCrLac(mBuyMinOrMax)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    displayList
                )
                binding.filterMinText.setAdapter(arrayAdapter)
                binding.filterMaxText.setAdapter(arrayAdapter)
            }
            "Rent" -> {
                Log.d(TAG, "SetResidential function called Rent $propertyType")
                mRentMinOrMax = resources.getStringArray(R.array.rent_price)
                val displayList = convertRentValueIntoCrLac(mRentMinOrMax)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    displayList
                )
                binding.filterMinText.setAdapter(arrayAdapter)
                binding.filterMaxText.setAdapter(arrayAdapter)
            }
        }
    }

    private fun convertRentValueIntoCrLac(minOrMax: Array<String>): ArrayList<String> {
        val list = ArrayList<String>()
        minOrMax.forEach { value ->
            when (value) {
                "5000" -> {
                    list.add("\u20B9 5000")
                }
                "10000" -> {
                    list.add("\u20B9 10000")
                }
                "15000" -> {
                    list.add("\u20B9 15000")
                }
                "20000" -> {
                    list.add("\u20B9 20000")
                }
                "25000" -> {
                    list.add("\u20B9 25000")
                }
                "30000" -> {
                    list.add("\u20B9 30000")
                }
                "35000" -> {
                    list.add("\u20B9 35000")
                }
                "40000" -> {
                    list.add("\u20B9 40000")
                }
                "50000" -> {
                    list.add("\u20B9 50000")
                }
                "60000" -> {
                    list.add("\u20B9 60000")
                }
                "85000" -> {
                    list.add("\u20B9 85000")
                }
                "1" -> {
                    list.add("\u20B9 1 Lac")
                }
                "1.5" -> {
                    list.add("\u20B9 1.5 Lac")
                }
                "2" -> {
                    list.add("\u20B9 2 Lac")
                }
                "4" -> {
                    list.add("\u20B9 4 Lac")
                }
                "7" -> {
                    list.add("\u20B9 7 Lac")
                }
                "10" -> {
                    list.add("\u20B9 10 Lac")
                }
            }
        }
        return list
    }

    private fun convertBuyValueIntoCrLac(minOrMax: Array<String>): ArrayList<String> {
        val list = ArrayList<String>()
        minOrMax.forEach { value ->
            when (value) {
                "5" -> {
                    list.add("\u20B9 5 Lac")
                }
                "10" -> {
                    list.add("\u20B9 10 Lac")
                }
                "20" -> {
                    list.add("\u20B9 20 Lac")
                }
                "30" -> {
                    list.add("\u20B9 30 Lac")
                }
                "40" -> {
                    list.add("\u20B9 40 Lac")
                }
                "50" -> {
                    list.add("\u20B9 50 Lac")
                }
                "60" -> {
                    list.add("\u20B9 20 Lac")
                }
                "70" -> {
                    list.add("\u20B9 70 Lac")
                }
                "80" -> {
                    list.add("\u20B9 80 Lac")
                }
                "90" -> {
                    list.add("\u20B9 90 Lac")
                }
                "1" -> {
                    list.add("\u20B9 1 Cr")
                }
                "1.2" -> {
                    list.add("\u20B9 1.2 Cr")
                }
                "1.4" -> {
                    list.add("\u20B9 1.4 Cr")
                }
                "1.6" -> {
                    list.add("\u20B9 1.6 Cr")
                }
                "1.8" -> {
                    list.add("\u20B9 1.8 Cr")
                }
                "2" -> {
                    list.add("\u20B9 2 Cr")
                }
                "2.3" -> {
                    list.add("\u20B9 2.3 Cr")
                }
                "2.6" -> {
                    list.add("\u20B9 2.6 Cr")
                }
                "3" -> {
                    list.add("\u20B9 3 Cr")
                }
                "3.5" -> {
                    list.add("\u20B9 3.5 Cr")
                }
                "4" -> {
                    list.add("\u20B9 4 Cr")
                }
                "4.5" -> {
                    list.add("\u20B9 4.5 Cr")
                }
                "5.2" -> {
                    list.add("\u20B9 5.2 Cr")
                }
                "10.2" -> {
                    list.add("\u20B9 10.2 Cr")
                }
                "20.2" -> {
                    list.add("\u20B9 20.2 Cr")
                }

            }
        }
        return list
    }

    private fun getValueAccordingToRent(value: String): String {
        var rupees = ""
        when (value) {
            "5000" -> {
                rupees = "\u20B9 5000"
            }
            "10000" -> {
                rupees = "\u20B9 10000"
            }
            "15000" -> {
                rupees = "\u20B9 15000"
            }
            "20000" -> {
                rupees = "\u20B9 20000"
            }
            "25000" -> {
                rupees = "\u20B9 25000"
            }
            "30000" -> {
                rupees = "\u20B9 30000"
            }
            "35000" -> {
                rupees = "\u20B9 35000"
            }
            "40000" -> {
                rupees = "\u20B9 40000"
            }
            "50000" -> {
                rupees = "\u20B9 50000"
            }
            "60000" -> {
                rupees = "\u20B9 60000"
            }
            "85000" -> {
                rupees = "\u20B9 85000"
            }
            "100000" -> {
                rupees = "\u20B9 1 Lac"
            }
            "150000" -> {
                rupees = "\u20B9 1.5 Lac"
            }
            "200000" -> {
                rupees = "\u20B9 2 Lac"
            }
            "400000" -> {
                rupees = "\u20B9 4 Lac"
            }
            "700000" -> {
                rupees = "\u20B9 7 Lac"
            }
            "1000000" -> {
                rupees = "\u20B9 10 Lac"
            }
        }
        return rupees
    }

    private fun getValueAccordingToBuy(value: String): String {
        var rupees = ""
        when (value) {
            "500000" -> {
                rupees = "\u20B9 5 Lac"
            }
            "1000000" -> {
                rupees = "\u20B9 10 Lac"
            }
            "2000000" -> {
                rupees = "\u20B9 20 Lac"
            }
            "3000000" -> {
                rupees = "\u20B9 30 Lac"
            }
            "4000000" -> {
                rupees = "\u20B9 40 Lac"
            }
            "5000000" -> {
                rupees = "\u20B9 50 Lac"
            }
            "6000000" -> {
                rupees = "\u20B9 20 Lac"
            }
            "7000000" -> {
                rupees = "\u20B9 70 Lac"
            }
            "8000000" -> {
                rupees = "\u20B9 80 Lac"
            }
            "9000000" -> {
                rupees = "\u20B9 90 Lac"
            }
            "10000000" -> {
                rupees = "\u20B9 1 Cr"
            }
            "12000000" -> {
                rupees = "\u20B9 1.2 Cr"
            }
            "14000000" -> {
                rupees = "\u20B9 1.4 Cr"
            }
            "16000000" -> {
                rupees = "\u20B9 1.6 Cr"
            }
            "18000000" -> {
                rupees = "\u20B9 1.8 Cr"
            }
            "20000000" -> {
                rupees = "\u20B9 2 Cr"
            }
            "23000000" -> {
                rupees = "\u20B9 2.3 Cr"
            }
            "26000000" -> {
                rupees = "\u20B9 2.6 Cr"
            }
            "30000000" -> {
                rupees = "\u20B9 3 Cr"
            }
            "35000000" -> {
                rupees = "\u20B9 3.5 Cr"
            }
            "40000000" -> {
                rupees = "\u20B9 4 Cr"
            }
            "45000000" -> {
                rupees = "\u20B9 4.5 Cr"
            }
            "52000000" -> {
                rupees = "\u20B9 5.2 Cr"
            }
            "10200000" -> {
                rupees = "\u20B9 10.2 Cr"
            }
            "20200000" -> {
                rupees = "\u20B9 20.2 Cr"
            }
        }
        return rupees
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}