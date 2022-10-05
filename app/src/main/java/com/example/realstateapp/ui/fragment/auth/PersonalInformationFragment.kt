package com.example.realstateapp.ui.fragment.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.realstateapp.R
import com.example.realstateapp.data.remote.User
import com.example.realstateapp.databinding.FragmentPersonalInformationBinding
import com.example.realstateapp.ui.activities.AuthenticationActivity
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Resources
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PersonalInformationFragment : Fragment() {

    private var mStateValue: String = ""
    private var mCityValue: String = ""
    private var mName: String = ""
    private var _binding: FragmentPersonalInformationBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth
    private var mCurrentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonalInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mCurrentUser = mAuth.currentUser
        setOnItemClickedListener()
        setOnTextChangedListener()
        val state = resources.getStringArray(R.array.state)
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_layout, R.id.tvDropDownItem, state)
        binding.textState.setAdapter(arrayAdapter)
        binding.btSubmit.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                mName = binding.textName.text.toString()
                if (mName.isNotEmpty()) {
                    if (mStateValue.isNotEmpty()) {
                        if (mCityValue.isNotEmpty()) {
                            val phoneNumber = mCurrentUser?.phoneNumber
                            val uid = mCurrentUser?.uid
                            if (phoneNumber != null && uid != null) {
                                val user = User(uid, mName, mStateValue, mCityValue, phoneNumber)
                                (activity as AuthenticationActivity).viewModel.addUser(user)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong Please Try Again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            isErrorCity(false)
                        }
                    } else {
                        isErrorState(false)
                    }
                } else {
                    isErrorName(false)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    "No Internet Connection", Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        (activity as AuthenticationActivity).viewModel.isUserAdded.observe(viewLifecycleOwner,{ response ->
            when (response) {
                is Resources.Success -> {
                    response.data?.let { value ->
                        if (value) {
                            val intent = Intent(requireContext(), RealStateActivity::class.java)
                            startActivity(intent)
                            (activity as AuthenticationActivity).finish()
                        } else {
                            isUserAdded(false)
                        }
                    }
                }
                is Resources.Error -> {
                    isUserAdded(false)
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong Please Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resources.Loading -> {
                    isUserAdded(true)
                }
            }
        })
    }

    private fun isUserAdded(value: Boolean) {
        if(value){
            binding.inputName.isEnabled = false
            binding.inputState.isEnabled = false
            binding.inputCity.isEnabled = false
            binding.btSubmit.isEnabled = false
            binding.spinKit.visibility = View.VISIBLE
        }else{
            binding.inputName.isEnabled = true
            binding.inputState.isEnabled = true
            binding.inputCity.isEnabled = true
            binding.btSubmit.isEnabled = true
            binding.spinKit.visibility = View.INVISIBLE
        }
    }

    private fun isErrorName(value: Boolean) {
        if (value) {
            binding.inputName.isErrorEnabled = false
        } else {
            binding.inputName.isErrorEnabled = true
            binding.inputName.error = "Please Enter Name"
        }
    }

    private fun isErrorState(value: Boolean) {
        if (value) {
            binding.inputState.isErrorEnabled = false
        } else {
            binding.inputState.isErrorEnabled = true
            binding.inputState.error = "Please Select State"
        }
    }

    private fun isErrorCity(value: Boolean) {
        if (value) {
            binding.inputCity.isErrorEnabled = false
        } else {
            binding.inputCity.isErrorEnabled = true
            binding.inputCity.error = "Please Select City"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setOnItemClickedListener() {
        binding.textState.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mStateValue = parent.getItemAtPosition(position).toString()
                Log.d("PersonalInfoFragment", mStateValue)
                binding.textCity.setText("")
                mCityValue = ""
                binding.inputState.isErrorEnabled = false
                setCityAccordingState(mStateValue)
            }
        binding.textCity.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mCityValue = parent.getItemAtPosition(position).toString()
                Log.d("PersonalInfoFragment", mCityValue)
                binding.inputCity.isErrorEnabled = false
            }
    }

    private fun setOnTextChangedListener() {
        binding.textName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputName.isErrorEnabled = false
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
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
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Arunachal Pradesh" -> {
                val cities = resources.getStringArray(R.array.arunachal_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Assam" -> {
                val cities = resources.getStringArray(R.array.assam_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Bihar" -> {
                val cities = resources.getStringArray(R.array.bihar_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Chandigarh" -> {
                val cities = resources.getStringArray(R.array.chandigarh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Chhattisgarh" -> {
                val cities = resources.getStringArray(R.array.chhattisgard_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Dadra and Nagar Haveli" -> {
                val cities = resources.getStringArray(R.array.dadra_and_nagar_haveli_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Daman and Diu" -> {
                val cities = resources.getStringArray(R.array.daman_and_Diu_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Delhi" -> {
                val cities = resources.getStringArray(R.array.delhi_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Goa" -> {
                val cities = resources.getStringArray(R.array.goa_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Gujarat" -> {
                val cities = resources.getStringArray(R.array.gujarat_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Haryana" -> {
                val cities = resources.getStringArray(R.array.haryana_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Himachal Pradesh" -> {
                val cities = resources.getStringArray(R.array.himachal_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Jammu and Kashmir (union territory)" -> {
                val cities = resources.getStringArray(R.array.jammu_and_kashmir_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Jharkhand" -> {
                val cities = resources.getStringArray(R.array.jharkhand_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Karnataka" -> {
                val cities = resources.getStringArray(R.array.karnataka_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Kerala" -> {
                val cities = resources.getStringArray(R.array.kerala_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Ladakh (union territory)" -> {
                val cities = resources.getStringArray(R.array.ladakh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Madhya Pradesh" -> {
                val cities = resources.getStringArray(R.array.madhya_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Maharashtra" -> {
                val cities = resources.getStringArray(R.array.maharashtra_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Manipur" -> {
                val cities = resources.getStringArray(R.array.manipur_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Meghalaya" -> {
                val cities = resources.getStringArray(R.array.meghalaya_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Mizoram" -> {
                val cities = resources.getStringArray(R.array.mizoram_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Nagaland" -> {
                val cities = resources.getStringArray(R.array.nagaland_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Odisha" -> {
                val cities = resources.getStringArray(R.array.odisha_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Puducherry (union territory)" -> {
                val cities = resources.getStringArray(R.array.puducherry_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Punjab" -> {
                val cities = resources.getStringArray(R.array.punjab_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Rajasthan" -> {
                val cities = resources.getStringArray(R.array.rajasthan_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Sikkim" -> {
                val cities = resources.getStringArray(R.array.sikkim_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Tamil Nadu" -> {
                val cities = resources.getStringArray(R.array.tamil_nadu_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Telangana" -> {
                val cities = resources.getStringArray(R.array.telangana_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Tripura" -> {
                val cities = resources.getStringArray(R.array.tripura_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Uttar Pradesh" -> {
                val cities = resources.getStringArray(R.array.uttar_pradesh_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }
            "Uttarakhand" -> {
                val cities = resources.getStringArray(R.array.uttarakhand_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }

            "West Bengal" -> {
                val cities = resources.getStringArray(R.array.west_bengal_cities)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textCity.setAdapter(arrayAdapter)
            }

        }
    }
}