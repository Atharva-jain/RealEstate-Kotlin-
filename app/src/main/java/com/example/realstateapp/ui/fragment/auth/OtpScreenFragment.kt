package com.example.realstateapp.ui.fragment.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.realstateapp.R
import com.example.realstateapp.databinding.FragmentOtpScreenBinding
import com.example.realstateapp.ui.activities.AuthenticationActivity
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant.PHONENUMBER
import com.example.realstateapp.utils.Constant.TOKEN
import com.example.realstateapp.utils.Constant.VERIFICATIONID
import com.example.realstateapp.utils.Resources
import com.example.realstateapp.viewmodel.RealStateViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OtpScreenFragment : Fragment() {

    private var mToken: Any? = null
    private var mVerificationID: String? = null
    private var mPhoneNumber: String? = null
    private var _binding: FragmentOtpScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtpScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textChangeListener()
        mAuth = FirebaseAuth.getInstance()
        mPhoneNumber = arguments?.getString(PHONENUMBER)
        mVerificationID = arguments?.getString(VERIFICATIONID)
        mToken = arguments?.get(TOKEN)
        binding.tvSentPhoneNo.text = "OTP SEND ON $mPhoneNumber"
        binding.btVerifyOtpNumber.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                val code = binding.textOtpNumber.text.toString()
                val credential = PhoneAuthProvider.getCredential(mVerificationID!!, code)
                (activity as AuthenticationActivity).viewModel.signInWithPhoneAuthCredential(
                    credential
                )
            } else {
                Snackbar.make(requireView(), "NO Internet Connection", Snackbar.LENGTH_SHORT).show()
            }
        }
        (activity as AuthenticationActivity).viewModel.isValid.observe(viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        response.data?.let { value ->
                            if (value) {
                                val uid = FirebaseAuth.getInstance().currentUser?.uid
                                if (uid != null) {
                                    (activity as AuthenticationActivity).viewModel.isUserAvailable(
                                        uid
                                    )
                                } else {
                                    isVisible(true)
                                    isError(true)
                                }
                            } else {
                                isVisible(true)
                                isError(true)
                            }
                        }
                    }
                    is Resources.Error -> {
                        isVisible(true)
                        isError(true)
                    }
                    is Resources.Loading -> {
                        isVisible(false)
                    }
                }
            })

        (activity as AuthenticationActivity).viewModel.isUserAvailable.observe(
            viewLifecycleOwner,
            { response ->
                when (response) {
                    is Resources.Success -> {
                        response.data?.let { value ->
                            if (value) {
                                val intent =
                                    Intent(requireContext(), RealStateActivity::class.java)
                                startActivity(intent)
                                (activity as AuthenticationActivity).finish()
                            } else {
                                try {
                                    findNavController().navigate(R.id.action_otpScreenFragment_to_personalInformationFragment)
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                    is Resources.Error -> {
                        isVisible(true)
                        isError(true)
                    }
                    is Resources.Loading -> {
                        isVisible(false)
                    }
                }
            })
        binding.tvChangePhoneNumber.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_otpScreenFragment_to_phoneAuth)
            } catch (e: Exception) {
            }
        }

        binding.tvResendOtp.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                isVisible(false)
                val options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(mPhoneNumber!!)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(requireActivity())                 // Activity (for callback binding)
                    .setCallbacks(callbacks)
                    .setForceResendingToken(mToken as PhoneAuthProvider.ForceResendingToken)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("OtpScreenFragment", "onVerificationCompleted:$credential")

        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("OtpScreenFragment", "onVerificationFailed", e)
            if (e is FirebaseAuthInvalidCredentialsException) {
                isVisible(true)
                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                isVisible(true)
                Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            isVisible(true)
            Log.d("OtpScreenFragment", "onCodeSent:$verificationId")
            mVerificationID = verificationId
            mToken = token
        }
    }

    private fun textChangeListener() {
        binding.textOtpNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("PhoneAuth", "$s $count")
                isError(false)
                if (s?.length == 6) {
                    Log.d("PhoneAuth", "Text greater than 6 ")
                    binding.btVerifyOtpNumber.isEnabled = true
                    binding.btVerifyOtpNumber.text = resources.getString(R.string.continues)
                } else {
                    binding.btVerifyOtpNumber.isEnabled = false
                    binding.btVerifyOtpNumber.text =
                        resources.getString(R.string.enter_verification_code)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun isVisible(value: Boolean) {
        if (value) {
            binding.btVerifyOtpNumber.isEnabled = true
            binding.textOtpNumber.isEnabled = true
            binding.tvChangePhoneNumber.isEnabled = true
            binding.tvResendOtp.isEnabled = true
            binding.spinKit.visibility = View.INVISIBLE
        } else {
            binding.btVerifyOtpNumber.isEnabled = false
            binding.textOtpNumber.isEnabled = false
            binding.tvChangePhoneNumber.isEnabled = false
            binding.tvResendOtp.isEnabled = false
            binding.spinKit.visibility = View.VISIBLE
        }
    }

    private fun isError(value: Boolean) {
        if (value) {
            binding.inputOtpNumber.isErrorEnabled = true
            binding.inputOtpNumber.error = "Enter Valid Otp"
        } else {
            binding.inputOtpNumber.isErrorEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}