package com.example.realstateapp.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.realstateapp.R
import com.example.realstateapp.data.remote.ApiInterface
import com.example.realstateapp.databinding.ActivityAuthenticationBinding
import com.example.realstateapp.viewmodel.RealStateViewModel
import com.example.realstateapp.viewmodel.RealStateViewModelFactory

class AuthenticationActivity : AppCompatActivity() {
    lateinit var viewModel: RealStateViewModel
    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = ApiInterface()
        val viewModelProviderFactory = RealStateViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(RealStateViewModel::class.java)
    }
}