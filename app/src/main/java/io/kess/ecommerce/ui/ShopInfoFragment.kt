package io.kess.ecommerce.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.databinding.FragmentShopInfoBinding
import io.kess.ecommerce.model.Shop
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.ShopViewModel

class ShopInfoFragment : Fragment() {
    private var _binding: FragmentShopInfoBinding? = null
    private val binding get() = _binding!!
    private var shopId: String? = null
    private lateinit var shopViewModel: ShopViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopId = arguments?.getString("ID")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
    }

    private fun initViewModel() {
        shopViewModel = ViewModelProvider(this)[ShopViewModel::class.java]
        shopId?.let {
            shopViewModel.getShopDetail(it)

        }
    }

    private fun setupUi(shop: Shop) {
        binding.inputName.setText(shop.shopName)
        binding.inputShopDescription.setText(shop.description)
        binding.inputPhone.setText(shop.phone)
        binding.inputLocation.setText(shop.address)
    }

    private fun observeData() {
        shopViewModel.shopState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    setupUi(state.data)
                }

                is UiState.Error -> {

                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showButtonNav(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).showButtonNav(true)
    }
}