package io.kess.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentMainRegisterBinding
import io.kess.ecommerce.databinding.FragmentRegisterCustomerBinding
import io.kess.ecommerce.ui.activity.LoginActivity
import io.kess.ecommerce.ui.activity.RegisterActivity

class MainRegisterFragment : Fragment() {

    private  var _binding: FragmentMainRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener(){
        binding.reSeller.setOnClickListener {
            (activity as RegisterActivity).replaceFragment(RegisterSellerFragment())
        }
        binding.reCustomer.setOnClickListener {
            (activity as RegisterActivity).replaceFragment(RegisterCustomerFragment())
        }
        binding.login.setOnClickListener {
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
            )
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}