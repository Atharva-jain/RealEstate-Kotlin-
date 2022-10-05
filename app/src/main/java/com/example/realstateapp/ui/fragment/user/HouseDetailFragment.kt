package com.example.realstateapp.ui.fragment.user

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.realstateapp.R
import com.example.realstateapp.adapter.AutoImageSliderAdapter
import com.example.realstateapp.adapter.OnImageClicked
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.databinding.FragmentHouseDetailBinding
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import android.widget.Toast

import android.content.pm.PackageManager

import android.content.Intent
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.Constant.REQUEST_CODE
import androidx.activity.result.ActivityResultCallback

import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import com.example.realstateapp.utils.ConvertDigitIntoValues


class HouseDetailFragment : Fragment(), OnImageClicked {

    private val TAG: String = "HomeDetailFragment"
    private var _binding: FragmentHouseDetailBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<OwnHomeDetailsFragmentArgs>()
    private lateinit var imageAdapter: AutoImageSliderAdapter
    private var loadedImageList: ArrayList<String> = ArrayList()
    private var mHome = Home()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHouseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mHome = args.home
        (activity as RealStateActivity).viewModel.addHistory(mHome)
        val houseIsLiked = (activity as RealStateActivity).viewModel.checkHomeLikedOrNot(mHome)
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
        imageAdapter = AutoImageSliderAdapter(this, true)
        imageAdapter.imageList = loadedImageList
        binding.ownHouseImageSlider.setSliderAdapter(imageAdapter)
        binding.ownHouseImageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        binding.ownHouseImageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        binding.ownHouseImageSlider.autoCycleDirection =
            SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH;
        binding.ownHouseImageSlider.indicatorSelectedColor = Color.WHITE
        binding.ownHouseImageSlider.indicatorUnselectedColor = Color.GRAY
        binding.ownHouseImageSlider.scrollTimeInSec = 4 //set scroll delay in seconds :
        binding.ownHouseImageSlider.startAutoCycle()
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

        binding.connectToWhatsAppButton.setOnClickListener {
            try {
                val installed = appInstalledOrNot("com.whatsapp")
                if (installed) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data =
                        Uri.parse("http://api.whatsapp.com/send?phone=+91$phoneNumber&text=Hello")
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Whats app not installed on your device",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "Whats app not installed in your phone",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "$e")
                e.printStackTrace()
            }
        }

        binding.dialToOwnerButton.setOnClickListener {
            try {
                mPermissionResult.launch(Manifest.permission.CALL_PHONE);
                val phoneIntent = Intent(Intent.ACTION_CALL)
                phoneIntent.data = Uri.parse(
                    "tel:"
                            + phoneNumber
                )
                startActivity(phoneIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "We are not able to place the call Can you please try again later",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(TAG, "$e")
            }
        }

        binding.cbHeart.isChecked = houseIsLiked
        binding.cbHeart.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                (activity as RealStateActivity).viewModel.addLikedHome(home = mHome)
                (activity as RealStateActivity).viewModel.addFavorite(home = mHome)
            } else {
                (activity as RealStateActivity).viewModel.removeLikedHome(home = mHome)
                (activity as RealStateActivity).viewModel.removeFavorite(home = mHome)
            }
        }


    }

    override fun onImageClicked(imageUrl: String) {
        try {
            val action =
                HouseDetailFragmentDirections.actionHouseDetailFragmentToFullZoomImageFragment(
                    imageUrl
                )
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    private val mPermissionResult = registerForActivityResult(
        RequestPermission()
    ) { result ->
        if (result) {
            Log.e(TAG, "onActivityResult: PERMISSION GRANTED")
        } else {
            Log.e(TAG, "onActivityResult: PERMISSION DENIED")
        }
    }

    //Create method appInstalledOrNot
    private fun appInstalledOrNot(url: String): Boolean {
        val packageManager: PackageManager = (activity as RealStateActivity).packageManager
        val app_installed: Boolean = try {
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }


    override fun onDeleteImageClicked(imageUrl: String) {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}