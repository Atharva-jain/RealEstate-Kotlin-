package com.example.realstateapp.ui.fragment.user

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.realstateapp.R
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.databinding.FragmentEditHouseBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.ConvertDigitIntoValues
import com.example.realstateapp.utils.GetImageExtension
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList


class EditHouseFragment : Fragment() {

    private var _binding: FragmentEditHouseBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditHouseFragmentArgs>()
    private val fireStorageReference = FirebaseStorage.getInstance().reference
    private val TAG: String = "FillHouseFragment"
    private var mFrontHouseImage = ""
    private var mState = ""
    private var mCity = ""
    private var mPropertyType = ""
    private var mResidentialType = ""
    private var mPriceType = ""
    private var mBHK = ""
    private var mBedroom = ""
    private var mBathroom = ""
    private val houseArrayList = ArrayList<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditHouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val home = args.home
        val ownerUid: String = home.ownerUid
        val uid = home.uid
        val title = home.title
        mState = home.state
        mCity = home.city
        val address = home.address
        val zipCode = home.zipCode
        mPropertyType = home.propertyType
        mResidentialType = home.residentialType
        val price: Int = home.price
        mPriceType = home.priceType
        val rs = ConvertDigitIntoValues.setValueAccordingToLacCr(mPriceType, price)
        mBHK = home.bhk
        mBedroom = home.bedroom
        mBathroom = home.bathroom
        val description = home.description
        val phoneNumber = home.phoneNumber
        mFrontHouseImage = home.frontHouseImage

        binding.textTitle.setText(title)
        binding.textState.setText(mState)
        binding.textCity.setText(mCity)
        binding.textAddress.setText(address)
        binding.textZipcode.setText(zipCode)
        binding.textPropertyType.setText(mPropertyType)
        binding.textResidentialType.setText(mResidentialType)
        binding.textPrice.setText(rs.toString())
        binding.textPriceValue.setText(mPriceType)
        binding.textBHK.setText(mBHK)
        binding.textBedroom.setText(mBedroom)
        binding.textBathroom.setText(mBathroom)
        binding.textPhoneNumber.setText(phoneNumber)
        binding.textDescription.setText(description)
        Glide.with(binding.houseImageView.context).load(mFrontHouseImage)
            .into(binding.houseImageView)
        setCityAccordingState(mState)
        setResidentialAccordingPropertyType(mPropertyType)
        setPriceTypeAccordingPropertyType(mPropertyType)

        val date = Calendar.getInstance().time
        setOnTextChangedListener()
        setOnItemClickedListener()
        val state = resources.getStringArray(R.array.state)
        val stateArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            state
        )
        binding.textState.setAdapter(stateArrayAdapter)
        val sell_rent = resources.getStringArray(R.array.property_types)
        val sell_rent_ArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            sell_rent
        )
        binding.textPropertyType.setAdapter(sell_rent_ArrayAdapter)
        val BHK = resources.getStringArray(R.array.bhk_types)
        val bhkArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            BHK
        )
        binding.textBHK.setAdapter(bhkArrayAdapter)
        val bedroom = resources.getStringArray(R.array.bedroom_types)
        val bedroomArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            bedroom
        )
        binding.textBedroom.setAdapter(bedroomArrayAdapter)
        val bathroom = resources.getStringArray(R.array.bathroom_types)
        val bathroomArrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_layout,
            R.id.tvDropDownItem,
            bathroom
        )
        binding.textBathroom.setAdapter(bathroomArrayAdapter)

        (activity as RealStateActivity).viewModel.isImageUploaded.observe(
            viewLifecycleOwner,
            { url ->
                try {
                    Log.d(TAG, "Image is upload")
                    isProgressBarVisible(false)
                    houseArrayList.add(url)
                    mFrontHouseImage = url
                    Glide.with(binding.houseImageView.context).load(url)
                        .into(binding.houseImageView)
                } catch (e: Exception) {
                    Log.d(TAG, "error on image uploaded $e")
                }
            }
        )

        (activity as RealStateActivity).viewModel.isImageProgress.observe(viewLifecycleOwner,
            { progress ->
                try {
                    binding.imageProgressBar.progress = progress.toInt()
                    binding.imageProgressTextView.text = "${progress}%"
                } catch (e: Exception) {
                    Log.d(TAG, "error on image process $e")
                }
            }
        )

        binding.btUpdateHome.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                val title = binding.textTitle.text.toString()
                val address = binding.textAddress.text.toString()
                val zipCode = binding.textZipcode.text.toString()
                val price = binding.textPrice.text.toString()
                val phoneNumber = binding.textPhoneNumber.text.toString()
                val description = binding.textDescription.text.toString()
                if (checkFieldIsEmpty(
                        title,
                        mState,
                        mCity,
                        address,
                        zipCode,
                        mPropertyType,
                        mResidentialType,
                        price,
                        mPriceType,
                        mBHK,
                        mBedroom,
                        mBathroom,
                        phoneNumber,
                        description
                    )
                ) {
                    if (ownerUid != null) {
                        try {
                            val priceValue = setValueAccordingToLacCr(mPriceType, price)
                            val home = Home(
                                ownerUid = ownerUid,
                                uid = uid,
                                title = title,
                                state = mState,
                                city = mCity,
                                address = address,
                                zipCode = zipCode,
                                propertyType = mPropertyType,
                                residentialType = mResidentialType,
                                price = priceValue,
                                priceType = mPriceType,
                                bhk = mBHK,
                                bedroom = mBedroom,
                                bathroom = mBathroom,
                                phoneNumber = phoneNumber,
                                description = description,
                                timeStamp = date,
                                frontHouseImage = mFrontHouseImage,
                                houseImages = houseArrayList
                            )
                            try {
                                (activity as RealStateActivity).viewModel.addHome(home)
                                findNavController().navigate(R.id.action_editHouseFragment_to_addHomeFragment)
                            } catch (e: Exception) {
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Please Something went wrong Please try again",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                Snackbar.make(requireView(), "No Internet Connection", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.houseImageView.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                val galleryIntent = Intent()
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                galleryIntent.type = "image/*"
                galleryResultLauncher.launch(galleryIntent)
            } else {
                Snackbar.make(requireView(), "No Internet Connection", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uri = data?.data
                if (uri != null) {
                    isProgressBarVisible(true)
                    (activity as RealStateActivity).viewModel.uploadHomeImage(
                        (activity as RealStateActivity),
                        uri
                    )
                }
            }
        }


    /*   val imageExtension =
           GetImageExtension.getFileExtension((activity as RealStateActivity), uri)
       if (imageExtension != null) {
           isProgressBarVisible(true)
           try {
               val fileRef: StorageReference =
                   fireStorageReference.child("${System.currentTimeMillis()}.$imageExtension")
               fileRef.putFile(uri)
                   .addOnSuccessListener { task ->
                       fileRef.downloadUrl.addOnSuccessListener { uri ->
                           isProgressBarVisible(false)
                           houseArrayList.add(uri.toString())
                           mFrontHouseImage = uri.toString()
                           Glide.with(binding.houseImageView.context).load(uri)
                               .into(binding.houseImageView)
                       }.addOnFailureListener {
                           Toast.makeText(
                               requireContext(),
                               it.localizedMessage,
                               Toast.LENGTH_LONG
                           ).show()
                       }
                   }
                   .addOnProgressListener { task ->
                       val progress =
                           (100 * task.bytesTransferred) / task.totalByteCount
                       binding.imageProgressBar.progress = progress.toInt()
                       binding.imageProgressTextView.text = "${progress}%"
                   }
                   .addOnFailureListener {
                       Toast.makeText(
                           requireContext(),
                           it.localizedMessage,
                           Toast.LENGTH_LONG
                       ).show()
                       isProgressBarVisible(false)
                   }
           } catch (e: Exception) {
               Log.d(TAG, "User Url are not updated $e")
               Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG)
                   .show()
               isProgressBarVisible(false)
           }*/

    private fun isProgressBarVisible(value: Boolean) {
        if (value) {
            binding.imageProgressBar.visibility = View.VISIBLE
            binding.imageProgressTextView.visibility = View.VISIBLE
            binding.houseImageView.visibility = View.INVISIBLE
            binding.btUpdateHome.isEnabled = false
        } else {
            binding.imageProgressBar.visibility = View.INVISIBLE
            binding.imageProgressTextView.visibility = View.INVISIBLE
            binding.houseImageView.visibility = View.VISIBLE
            binding.btUpdateHome.isEnabled = true
        }
    }

    private fun checkFieldIsEmpty(
        title: String,
        state: String,
        city: String,
        address: String,
        zipCode: String,
        propertyType: String,
        residentialType: String,
        price: String,
        priceType: String,
        bhk: String,
        bedroom: String,
        bathroom: String,
        phoneNumber: String,
        description: String
    ): Boolean {
        var check = true
        if (title.isEmpty()) {
            check = false
            isTitleErrorEnable(true)
        }
        if (state.isEmpty()) {
            check = false
            isStateErrorEnable(true)
        }
        if (city.isEmpty()) {
            check = false
            isCityErrorEnable(true)
        }
        if (address.isEmpty()) {
            check = false
            isAddressErrorEnable(true)
        }
        if (zipCode.isEmpty()) {
            check = false
            isZipCodeErrorEnable(true)
        }
        if (propertyType.isEmpty()) {
            check = false
            isPropertyTypeErrorEnable(true)
        }
        if (residentialType.isEmpty()) {
            check = false
            isResidentialErrorEnable(true)
        }
        if (price.isEmpty()) {
            check = false
            isPriceErrorEnable(true)
        }
        if (priceType.isEmpty()) {
            check = false
            isPriceTypeErrorEnable(true)
        }
        if (bhk.isEmpty()) {
            check = false
            isBHKTypeErrorEnable(true)
        }
        if (bedroom.isEmpty()) {
            check = false
            isBedroomTypeErrorEnable(true)
        }
        if (bathroom.isEmpty()) {
            check = false
            isBathroomTypeErrorEnable(true)
        }
        if (phoneNumber.isEmpty()) {
            check = false
            isPhoneNumberTypeErrorEnable(true)
        }
        if (description.isEmpty()) {
            check = false
            isDescriptionTypeErrorEnable(true)
        }
        return check
    }

    private fun setOnItemClickedListener() {
        binding.textState.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mState = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mState)
                binding.textCity.setText("")
                mCity = ""
                isStateErrorEnable(false)
                setCityAccordingState(mState)
            }
        binding.textCity.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mCity = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mCity)
                isCityErrorEnable(false)
            }
        binding.textPropertyType.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mPropertyType = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mPropertyType)
                mResidentialType = ""
                mPriceType = ""
                binding.textResidentialType.setText("")
                binding.textPriceValue.setText("")
                isPropertyTypeErrorEnable(false)
                setResidentialAccordingPropertyType(mPropertyType)
                setPriceTypeAccordingPropertyType(mPropertyType)
            }
        binding.textResidentialType.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mResidentialType = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mResidentialType)
                isResidentialErrorEnable(false)
            }
        binding.textPriceValue.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mPriceType = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mPriceType)
                isPriceTypeErrorEnable(false)
            }
        binding.textBHK.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mBHK = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mPriceType)
                isBHKTypeErrorEnable(false)
            }
        binding.textBedroom.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mBedroom = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mBedroom)
                isBedroomTypeErrorEnable(false)
            }
        binding.textBathroom.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mBathroom = parent.getItemAtPosition(position).toString()
                Log.d(TAG, mBathroom)
                isBathroomTypeErrorEnable(false)
            }
    }

    private fun setOnTextChangedListener() {
        binding.textTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isTitleErrorEnable(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.textAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isAddressErrorEnable(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.textZipcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isZipCodeErrorEnable(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.textPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isPriceErrorEnable(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.textPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isPhoneNumberTypeErrorEnable(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.textDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isDescriptionTypeErrorEnable(false)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun isTitleErrorEnable(value: Boolean) {
        if (value) {
            binding.inputTitle.isErrorEnabled = true
            binding.inputTitle.error = "Please Enter Title"
        } else {
            binding.inputTitle.isErrorEnabled = false
        }
    }

    private fun isStateErrorEnable(value: Boolean) {
        if (value) {
            binding.inputState.isErrorEnabled = true
            binding.inputState.error = "Please Select State"
        } else {
            binding.inputState.isErrorEnabled = false
        }
    }

    private fun isCityErrorEnable(value: Boolean) {
        if (value) {
            binding.inputCity.isErrorEnabled = true
            binding.inputCity.error = "Please Select City"
        } else {
            binding.inputCity.isErrorEnabled = false
        }
    }

    private fun isAddressErrorEnable(value: Boolean) {
        if (value) {
            binding.inputAddress.isErrorEnabled = true
            binding.inputAddress.error = "Please Enter Address"
        } else {
            binding.inputAddress.isErrorEnabled = false
        }
    }

    private fun isZipCodeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputZipcode.isErrorEnabled = true
            binding.inputZipcode.error = "Please Enter Zip Code"
        } else {
            binding.inputZipcode.isErrorEnabled = false
        }
    }

    private fun isPropertyTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputPropertyType.isErrorEnabled = true
            binding.inputPropertyType.error = "Please select any one from given option"
        } else {
            binding.inputPropertyType.isErrorEnabled = false
        }
    }

    private fun isResidentialErrorEnable(value: Boolean) {
        if (value) {
            binding.inputResidentialType.isErrorEnabled = true
            binding.inputResidentialType.error = "Please Select Residential"
        } else {
            binding.inputResidentialType.isErrorEnabled = false
        }
    }

    private fun isPriceErrorEnable(value: Boolean) {
        if (value) {
            binding.inputPrice.isErrorEnabled = true
            binding.inputPrice.error = "Please Enter Price"
        } else {
            binding.inputPrice.isErrorEnabled = false
        }
    }

    private fun isPriceTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputPriceValue.isErrorEnabled = true
            binding.inputPriceValue.error = "Please Select Price Type"
        } else {
            binding.inputPriceValue.isErrorEnabled = false
        }
    }

    private fun isBHKTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputBHK.isErrorEnabled = true
            binding.inputBHK.error = "Please Select BHK"
        } else {
            binding.inputBHK.isErrorEnabled = false
        }
    }

    private fun isBedroomTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputBedroom.isErrorEnabled = true
            binding.inputBedroom.error = "Please Select Bedroom"
        } else {
            binding.inputBedroom.isErrorEnabled = false
        }
    }

    private fun isBathroomTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputBathroom.isErrorEnabled = true
            binding.inputBathroom.error = "Please Select Bathroom"
        } else {
            binding.inputBathroom.isErrorEnabled = false
        }
    }

    private fun isPhoneNumberTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputPhoneNumber.isErrorEnabled = true
            binding.inputPhoneNumber.error = "Please Enter Phone Number"
        } else {
            binding.inputPhoneNumber.isErrorEnabled = false
        }
    }

    private fun isDescriptionTypeErrorEnable(value: Boolean) {
        if (value) {
            binding.inputDescription.isErrorEnabled = true
            binding.inputDescription.error = "Please Enter Description"
        } else {
            binding.inputDescription.isErrorEnabled = false
        }
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

    private fun setResidentialAccordingPropertyType(propertyType: String) {
        Log.d(TAG, "SetResidential function called $propertyType")
        when (propertyType) {
            "Sell" -> {
                Log.d(TAG, "SetResidential function called Sell $propertyType")
                val cities = resources.getStringArray(R.array.residential_sell_types)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textResidentialType.setAdapter(arrayAdapter)
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
                binding.textResidentialType.setAdapter(arrayAdapter)
            }
        }
    }

    private fun setPriceTypeAccordingPropertyType(propertyType: String) {
        Log.d(TAG, "SetPrice function called $propertyType")
        when (propertyType) {
            "Sell" -> {
                Log.d(TAG, "SetPrice function called Sell $propertyType")
                val cities = resources.getStringArray(R.array.price_sell_types)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textPriceValue.setAdapter(arrayAdapter)
            }
            "Rent" -> {
                Log.d(TAG, "SetPrice function called Rent $propertyType")
                val cities = resources.getStringArray(R.array.price_rent_types)
                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_layout,
                    R.id.tvDropDownItem,
                    cities
                )
                binding.textPriceValue.setAdapter(arrayAdapter)
            }
        }
    }

    private fun setValueAccordingToLacCr(mPriceType: String, price: String): Int {
        if (mPriceType != "") {
            try {
                val rs = Integer.parseInt(price)
                var value = 0
                if (mPriceType == "lac") {
                    value = rs * 100000
                    return value
                }
                if (mPriceType == "cr") {
                    value = rs * 10000000
                    return value
                }
                return rs
            } catch (e: Exception) {
                Log.d(TAG, "error to converting prices $e")
            }
        }
        return 0
    }

}