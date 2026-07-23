package io.kess.ecommerce.ui

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
import io.kess.ecommerce.databinding.FragmentRegisterCustomerBinding
import io.kess.ecommerce.model.UserRole
import io.kess.ecommerce.ui.activity.LoginActivity
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.AuthViewModel


class RegisterCustomerFragment : Fragment() {
    private var _binding: FragmentRegisterCustomerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        setupOnClickListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    private fun setupOnClickListener() {
        binding.login.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnCreateAccount.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()
            val confirmPass = binding.confirmPass.text.toString().trim()
            if (email.isBlank() || password.isBlank() || name.isBlank() || confirmPass.isBlank()) {
                showSnackBar(
                    requireView(), "All fields need to be fill",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white)
                )

                return@setOnClickListener
            }
            if (password != confirmPass) {
                showSnackBar(
                    requireView(),
                    "Passwords do not match",
                    backgroundColor = ContextCompat.getColor(requireContext(), R.color.red),
                    textColor = ContextCompat.getColor(requireContext(), R.color.white)
                )
                return@setOnClickListener
            }
            viewModel.register(name, email, password, UserRole.CUSTOMER)
        }
        binding.login.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeData() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }

                is UiState.Success -> {
                    showLoading(false)
                    showSnackBar(
                        requireView(),
                        "Register successfully",
                        ContextCompat.getColor(requireContext(), R.color.green),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                    val intent = Intent(requireContext(), ShopActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }

                is UiState.Error -> {
                    showLoading(false)
                    showSnackBar(
                        requireView(),
                        state.message,
                        ContextCompat.getColor(requireContext(), R.color.red),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }

                is UiState.Idle -> {}
            }
            viewModel.message.observe(viewLifecycleOwner) { message ->
                message.getContentIfNotHandled()?.let { txt ->

                                   showSnackBar(
                        requireView(),
                       txt,
                        ContextCompat.getColor(requireContext(), R.color.red),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}