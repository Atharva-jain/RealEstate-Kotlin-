package com.example.realstateapp.ui.fragment.user

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.realstateapp.R
import com.example.realstateapp.adapter.AutoImageSliderAdapter
import com.example.realstateapp.adapter.OnImageClicked
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.databinding.FragmentOwnHomeDetailsBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant
import com.example.realstateapp.utils.ConvertDigitIntoValues
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView


class OwnHomeDetailsFragment : Fragment(), OnImageClicked {

    private val fireStorageReference = FirebaseStorage.getInstance().reference
    private var _binding: FragmentOwnHomeDetailsBinding? = null
    private val args by navArgs<OwnHomeDetailsFragmentArgs>()
    private val binding get() = _binding!!
    private lateinit var imageAdapter: AutoImageSliderAdapter
    private var loadedImageList: ArrayList<String> = ArrayList()
    private var mHome = Home()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOwnHomeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mHome = args.home
        val list = mHome.houseImages
        val title = mHome.title
        val address = mHome.address
        val bathroom = mHome.bathroom
        val bedroom = mHome.bedroom
        val bhk = mHome.bhk
        val city = mHome.city
        val description = mHome.description
        val phoneNumber = mHome.phoneNumber
        val price = mHome.price
        val priceType = mHome.priceType
        val rs = ConvertDigitIntoValues.setValueAccordingToLacCr(priceType, price)
        val propertyType = mHome.propertyType
        val residentialType = mHome.residentialType
        val state = mHome.state
        val zipCode = mHome.zipCode

        binding.tvTitle.text = title
        binding.tvAddress.text = address
        binding.ownBedroomNumberTextView.text = "$bedroom rooms"
        binding.ownBathNumberTextView.text = "$bathroom rooms"
        binding.tvDescription.text = description

        Log.d("OwnHomeDetailsFragment", "get date $list")
        loadedImageList = ArrayList(list)
        Log.d("OwnHomeDetailsFragment", "In ArrayDate $loadedImageList")
        imageAdapter = AutoImageSliderAdapter(this, false)
        imageAdapter.imageList = loadedImageList
        binding.ownHouseImageSlider.setSliderAdapter(imageAdapter)
        binding.ownHouseImageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        binding.ownHouseImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        binding.ownHouseImageSlider.autoCycleDirection =
            SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH;
        binding.ownHouseImageSlider.indicatorSelectedColor = Color.WHITE;
        binding.ownHouseImageSlider.indicatorUnselectedColor = Color.GRAY;
        binding.ownHouseImageSlider.scrollTimeInSec = 4; //set scroll delay in seconds :
        binding.ownHouseImageSlider.startAutoCycle();

        binding.addImageFloatingActionButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                val galleryIntent = Intent()
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                galleryIntent.type = "image/*"
                galleryResultLauncher.launch(galleryIntent)
            } else {
                Snackbar.make(requireView(), "No Internet Connection", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.countryNameTextview.text = getString(R.string.india)
        binding.stateNameTextview.text = state
        binding.cityNameTextview.text = city
        binding.zipCodeNameTextview.text = zipCode
        binding.propertyTypeNameTextview.text = propertyType
        binding.bhkNameTextview.text = bhk
        binding.residentialNameTextview.text = residentialType
        binding.priceNameTextview.text = "$rs ${priceType.uppercase()}"
        binding.addressNameTextview.text = address
        binding.contactNameTextview.text = phoneNumber

        binding.deleteButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                createDeleteDialogBox(mHome)
            } else {
                Snackbar.make(requireView(), Constant.NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.updateButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                try {
                    val action =
                        OwnHomeDetailsFragmentDirections.actionOwnHomeDetailsFragmentToEditHouseFragment(
                            mHome
                        )
                    findNavController().navigate(action)
                } catch (e: Exception) {
                }
            } else {
                Snackbar.make(requireView(), Constant.NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        (activity as RealStateActivity).viewModel.isImageUploaded.observe(
            viewLifecycleOwner,
            { url ->
                try {
                    loadedImageList.add(url.toString())
                    imageAdapter.renewItems(loadedImageList)
                    binding.ownHouseImageSlider.refreshDrawableState()
                    val uid = mHome.uid
                    val url = url.toString()
                    (activity as RealStateActivity).viewModel.addHouseImages(
                        uid,
                        url
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "error on image uploaded $e")
                }
            }
        )

    }

    private fun createDeleteDialogBox(home: Home) {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.deleteHouse
                ) { dialog, id ->
                    try {
                        (activity as RealStateActivity).viewModel.deleteHome(home)
                        findNavController().navigate(R.id.action_ownHomeDetailsFragment_to_addHomeFragment)
                    } catch (e: Exception) {
                    }
                }
                setNegativeButton(
                    R.string.cancelHome
                ) { dialog, id ->
                    dialog.dismiss()
                }
            }
            builder.create()
        }
        alertDialog?.setTitle("Delete house")
        alertDialog?.setMessage("Sure you want delete house")
        alertDialog?.setIcon(R.drawable.ic_delete)
        alertDialog?.show()
    }


    private val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val uri = data?.data
                if (uri != null) {
                    (activity as RealStateActivity).viewModel.uploadHomeImage(
                        (activity as RealStateActivity),
                        uri
                    )
                }
            }
        }

//    val imageExtension =
//        GetImageExtension.getFileExtension((activity as RealStateActivity), uri)
//    if (imageExtension != null) {
//        try {
//            val fileRef: StorageReference =
//                fireStorageReference.child("${System.currentTimeMillis()}.$imageExtension")
//            fileRef.putFile(uri)
//                .addOnSuccessListener { task ->
//                    try {
//                        fileRef.downloadUrl.addOnSuccessListener { uri ->
//                            loadedImageList.add(uri.toString())
//                            imageAdapter.renewItems(loadedImageList)
//                            binding.ownHouseImageSlider.refreshDrawableState()
//                            val uid = mHome.uid
//                            val url = uri.toString()
//                            (activity as RealStateActivity).viewModel.addHouseImages(
//                                uid,
//                                url
//                            )
//                        }
//                    } catch (e: Exception) {
//                        Log.d(TAG, "$e")
//                    }
//                }
//                .addOnFailureListener {
//                    Toast.makeText(
//                        requireContext(),
//                        it.localizedMessage,
//                        Toast.LENGTH_LONG
//                    ).show()
//
//                }
//        } catch (e: Exception) {
//            Log.d(TAG, "User Url are not updated $e")
//            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_LONG)
//                .show()
//        }

    override fun onImageClicked(imageUrl: String) {
        try {
            val action =
                OwnHomeDetailsFragmentDirections.actionOwnHomeDetailsFragmentToFullZoomImageFragment(
                    imageUrl
                )
            findNavController().navigate(action)
        } catch (e: Exception) {
        }
    }

    override fun onDeleteImageClicked(imageUrl: String) {
        if (CheckInternetConnection.hasInternetConnection(requireContext())) {
            createDeleteImageDialogBox(imageUrl)
        } else {
            Snackbar.make(requireView(), Constant.NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private fun createDeleteImageDialogBox(imageUrl: String) {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.deleteHouse
                ) { dialog, id ->
                    imageAdapter.deleteItem(imageUrl)
                    binding.ownHouseImageSlider.refreshDrawableState()
                    val uid = mHome.uid
                    (activity as RealStateActivity).viewModel.deleteHouseImages(uid, imageUrl)
                }
                setNegativeButton(
                    R.string.cancelHome
                ) { dialog, id ->
                    dialog.dismiss()
                }
            }
            builder.create()
        }
        alertDialog?.setTitle("Remove Image")
        alertDialog?.setMessage("Sure you want remove image")
        alertDialog?.setIcon(R.drawable.ic_delete)
        alertDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}