package com.example.realstateapp.ui.fragment.auth

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
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.realstateapp.R
import com.example.realstateapp.databinding.FragmentPhoneAuthBinding
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant.PHONENUMBER
import com.example.realstateapp.utils.Constant.TOKEN
import com.example.realstateapp.utils.Constant.VERIFICATIONID
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneAuth : Fragment() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    private var _binding: FragmentPhoneAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth
    private var mPhoneNumber: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhoneAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        textChangeListener()
        binding.btVerifyPhoneNumber.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                isVisible(false)
                mPhoneNumber = binding.textPhoneNumber.text.toString()
                if (mPhoneNumber.isNotEmpty() && mPhoneNumber.length >= 10) {
                    mPhoneNumber = "+91$mPhoneNumber"
                    val options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(mPhoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity())                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            } else {
                isVisible(true)
                Snackbar.make(requireView(), "NO Internet Connection", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("PhoneAuthFragment", "onVerificationCompleted:$credential")

        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("PhoneAuthFragment", "onVerificationFailed", e)
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
            Log.d("PhoneAuthFragment", "onCodeSent:$verificationId")
            storedVerificationId = verificationId
            resendToken = token
            val bundle = bundleOf(
                PHONENUMBER to mPhoneNumber,
                VERIFICATIONID to storedVerificationId,
                TOKEN to resendToken
            )
            try {
                findNavController().navigate(R.id.action_phoneAuth_to_otpScreenFragment, bundle)
            }catch (e: Exception){}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun textChangeListener() {
        binding.textPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("PhoneAuth", "$s $count")
                if (s?.length == 10) {
                    Log.d("PhoneAuth", "Text greater than 10 ")
                    binding.btVerifyPhoneNumber.isEnabled = true
                    binding.btVerifyPhoneNumber.text = resources.getString(R.string.continues)
                } else {
                    binding.btVerifyPhoneNumber.isEnabled = false
                    binding.btVerifyPhoneNumber.text =
                        resources.getString(R.string.enter_phone_number)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun isVisible(value: Boolean) {
        if (value) {
            binding.inputPhoneNumber.isEnabled = true
            binding.btVerifyPhoneNumber.isEnabled = true
            binding.spinKit.visibility = View.INVISIBLE
        } else {
            binding.inputPhoneNumber.isEnabled = false
            binding.btVerifyPhoneNumber.isEnabled = false
            binding.spinKit.visibility = View.VISIBLE
        }

    }
}