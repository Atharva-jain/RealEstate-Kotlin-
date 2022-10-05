package com.example.realstateapp.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.realstateapp.R
import com.example.realstateapp.RealStateNavDirections
import com.example.realstateapp.data.remote.ApiInterface
import com.example.realstateapp.databinding.ActivityRealStateBinding
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.viewmodel.RealStateViewModel
import com.example.realstateapp.viewmodel.RealStateViewModelFactory
import com.google.android.material.snackbar.Snackbar

class RealStateActivity : AppCompatActivity() {
    lateinit var navController: NavController
    lateinit var binding: ActivityRealStateBinding
    lateinit var viewModel: RealStateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealStateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.realStateFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        binding.realStateBottomNavigationView.setupWithNavController(navController)
        val repository = ApiInterface()
        val viewModelProviderFactory = RealStateViewModelFactory(application, repository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(RealStateViewModel::class.java)
        viewModel.setUser()
        viewModel.setAllLikeHome()
        binding.searchHouseTextInputLayout
            .setEndIconOnClickListener {
                try {
                    if (CheckInternetConnection.hasInternetConnection(this)) {
                        val action = RealStateNavDirections.actionGlobalFilterFragment()
                        navController.navigate(action)
                    } else {
                        val view: View = findViewById(R.id.content)
                        Snackbar.make(view, "No Internet Connection", Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                }
            }

        binding.searchHouseTextInputEditText.addTextChangedListener { editable ->
            val hasFocus = binding.searchHouseTextInputLayout.hasWindowFocus()
            Log.d("OKCOOLNOW","Search Run")
            val id = navController.currentDestination?.id
            if (id != R.id.searchHouseFragment) {
                if (binding.searchHouseTextInputEditText.text.toString().isNotEmpty() && hasFocus) {
                    val action = RealStateNavDirections.actionGlobalSearchFragment()
                    navController.navigate(action)
                }
            }
        }

    }
}