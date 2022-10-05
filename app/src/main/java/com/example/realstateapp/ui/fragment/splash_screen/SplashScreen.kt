package com.example.realstateapp.ui.fragment.splash_screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.realstateapp.R
import com.example.realstateapp.databinding.FragmentSplashScreenBinding
import com.example.realstateapp.ui.activities.AuthenticationActivity
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.Constant.SPLASH_SCREEN_TIME_OUT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SplashScreen : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!
    private var mUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            mUser = FirebaseAuth.getInstance().currentUser
            if(mUser != null){
                val intent = Intent(requireContext(), RealStateActivity::class.java)
                startActivity(intent)
                (activity as AuthenticationActivity).finish()
            }else{
                try {
                    findNavController().navigate(R.id.action_splashScreen_to_phoneAuth)
                } catch (e: Exception) {}
            }
        }, SPLASH_SCREEN_TIME_OUT)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}