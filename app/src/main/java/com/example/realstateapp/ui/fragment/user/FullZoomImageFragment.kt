package com.example.realstateapp.ui.fragment.user

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.realstateapp.R
import com.example.realstateapp.databinding.FragmentFullZoomImageBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class FullZoomImageFragment : Fragment() {

    private var _binding: FragmentFullZoomImageBinding? = null
    private val args by navArgs<FullZoomImageFragmentArgs>()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullZoomImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = args.imageUrl
        Glide.with(binding.homeImageView.context).load(url).into(binding.homeImageView)

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}