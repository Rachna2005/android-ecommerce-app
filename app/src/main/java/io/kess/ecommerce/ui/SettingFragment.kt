package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProfileBinding
import io.kess.ecommerce.databinding.FragmentSettingBinding
import io.kess.ecommerce.model.User
import io.kess.ecommerce.view_model.AuthViewModel

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupBottomSheet()
        setupClickListener()

        observeData()
    }

    private fun initViewModel() {
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    private fun observeData() {
        authViewModel.authData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                setupUi(data)
            }
        }
    }

    private fun setupUi(user: User) {
        binding.name.text = user.name
        binding.phoneNumber.text = user.phoneNumber
        binding.address.text = user.address
    }
    private fun setupBottomSheet() {
        parentFragmentManager.setFragmentResultListener(
            "ADDRESS_RESULT",
            viewLifecycleOwner
        ) { _, bundle ->
            val address = bundle.getString("ADDRESS", "")
            val phone = bundle.getString("PHONE", "")
            binding.address.text = address
            binding.phoneNumber.text = phone

            // 2. update Firebase via ViewModel
            authViewModel.updateUser(
                address = address,
                phoneNumber = phone
            )
        }
    }

    private fun setupClickListener() {
        binding.backBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnEdit.setOnClickListener {
            val sheet = AddressBottomSheet()

            sheet.arguments = Bundle().apply {
                putString("ADDRESS", binding.address.text.toString())
                putString("PHONE", binding.phoneNumber.text.toString())
            }
            sheet.show(parentFragmentManager, "AddressSheet")
        }
        binding.btnAddAddress.setOnClickListener {
            val sheet = AddressBottomSheet()

            sheet.arguments = Bundle().apply {
                putString("ADDRESS", "")
                putString("PHONE", "")
            }
            sheet.show(parentFragmentManager, "AddressSheet")
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showButtonNav(show = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).showButtonNav(show = true)
    }

}