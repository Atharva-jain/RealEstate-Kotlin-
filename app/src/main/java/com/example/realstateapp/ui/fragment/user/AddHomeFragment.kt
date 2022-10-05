package com.example.realstateapp.ui.fragment.user

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realstateapp.R
import com.example.realstateapp.adapter.AddHomeAdapter
import com.example.realstateapp.adapter.OnClickedAddHome
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.databinding.FragmentAddHomeBinding
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.CheckInternetConnection
import com.example.realstateapp.utils.Constant.HOME
import com.example.realstateapp.utils.Constant.NO_INTERNET_CONNECTION
import com.example.realstateapp.utils.Constant.WARNING
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class AddHomeFragment : Fragment(), OnClickedAddHome {

    private var _binding: FragmentAddHomeBinding? = null
    private val binding get() = _binding!!
    private val firebaseInstance = FirebaseFirestore.getInstance()
    private val youHomeCollection = firebaseInstance.collection(HOME)
    private lateinit var yourHomeAdapter: AddHomeAdapter

    override fun onStart() {
        super.onStart()
        yourHomeAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        yourHomeAdapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addHouseExtendedFab.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                try {
                    findNavController().navigate(R.id.action_addHomeFragment_to_fillHouseDetails)
                } catch (e: Exception) {
                }
            } else {
                Snackbar.make(requireView(), NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG).show()
            }

        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val query = youHomeCollection.whereEqualTo("ownerUid", uid)
            val recyclerOptions =
                FirestoreRecyclerOptions.Builder<Home>().setQuery(query, Home::class.java)
                    .build()
            yourHomeAdapter = AddHomeAdapter(recyclerOptions, this)
            binding.addHouseRecyclerView.adapter = yourHomeAdapter
            yourHomeAdapter.startListening()
            binding.addHouseRecyclerView.layoutManager =
                LinearLayoutManager((activity as RealStateActivity))
        } else {
            Toast.makeText(requireContext(), WARNING, Toast.LENGTH_LONG).show()
        }
    }

    override fun onClickedAddHome(home: Home) {
        if (CheckInternetConnection.hasInternetConnection(requireContext())) {
            try {
                val action = AddHomeFragmentDirections.actionAddHomeFragmentToOwnHomeDetailsFragment(home)
                findNavController().navigate(action)
            } catch (e: Exception) {
            }
        } else {
            Snackbar.make(requireView(), NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun deleteHouseClicked(home: Home) {
        if (CheckInternetConnection.hasInternetConnection(requireContext())) {
            createDeleteDialogBox(home)
        } else {
            Snackbar.make(requireView(), NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun createDeleteDialogBox(home: Home) {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    R.string.deleteHouse
                ) { dialog, id ->
                    (activity as RealStateActivity).viewModel.deleteHome(home)
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

    override fun editHouseClicked(home: Home) {
        if (CheckInternetConnection.hasInternetConnection(requireContext())) {
            try {
                val action =
                    AddHomeFragmentDirections.actionAddHomeFragmentToEditHouseFragment(home)
                findNavController().navigate(action)
            } catch (e: Exception) {
            }

        } else {
            Snackbar.make(requireView(), NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun isListEmpty(value: Boolean) {
        if (CheckInternetConnection.hasInternetConnection(requireContext())) {
            if(value){
                binding.emptyHouseLayout.visibility = View.VISIBLE
            }else{
                binding.emptyHouseLayout.visibility = View.INVISIBLE
            }
        } else {
            Snackbar.make(requireView(), NO_INTERNET_CONNECTION, Snackbar.LENGTH_LONG).show()
        }

    }
}