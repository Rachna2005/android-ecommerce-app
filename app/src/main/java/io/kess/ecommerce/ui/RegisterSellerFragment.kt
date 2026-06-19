package io.kess.ecommerce.ui

import android.R.attr.type
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCreateShopBinding
import io.kess.ecommerce.databinding.FragmentRegisterCustomerBinding
import io.kess.ecommerce.databinding.FragmentRegisterSellerBinding
import io.kess.ecommerce.model.UserRole
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.AuthViewModel

class RegisterSellerFragment : Fragment() {


    private  var _binding: FragmentRegisterSellerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        setupOnClickListener()
    }
    private fun initViewModel(){
        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    private fun setupOnClickListener(){
        binding.login.setOnClickListener {

        }
        binding.createSeller.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()
            val confirmPass = binding.confirmPass.text.toString().trim()
            if (email.isBlank() || password.isBlank() || name.isBlank() || confirmPass.isBlank()) {
//                Toast.makeText(requireContext(), "All fields need to be fill", Toast.LENGTH_LONG)
//                    .show()
                showSnackBar(requireView(), "All fields need to be fill",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.yellow),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white))
                return@setOnClickListener
            }
            if (password != confirmPass) {
//                Toast.makeText(requireContext(), "Passwords Do not match", Toast.LENGTH_LONG).show()
                showSnackBar(
                    requireView(),
                    "Passwords do not match",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.red),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white)
                )
                return@setOnClickListener
            }
            viewModel.register(name, email, password, UserRole.SELLER)
        }
    }

    private fun observeData(){
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.createSeller.isEnabled = false
                    binding.createSeller.text = "Loading..."
                }

                is UiState.Success -> {
                    binding.createSeller.isEnabled = true
                    binding.createSeller.text = "Create Account"
                    showSnackBar(
                        requireView(),
                        "Register successfully",
                        ContextCompat.getColor(requireContext(), R.color.green),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                    (activity as RegisterActivity).navigate(CreateShopFragment())
                }

                is UiState.Error -> {
                    binding.createSeller.isEnabled = true
                    binding.createSeller.text = "Create Account"
                    showSnackBar(
                        requireView(),
                        state.message,
                        ContextCompat.getColor(requireContext(), R.color.red),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}