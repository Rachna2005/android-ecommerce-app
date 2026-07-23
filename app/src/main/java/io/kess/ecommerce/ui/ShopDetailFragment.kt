package io.kess.ecommerce.ui

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProductDetailBinding
import io.kess.ecommerce.databinding.FragmentShopDetailBinding
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.Shop
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.ColorAdapter
import io.kess.ecommerce.ui.adapter.ReviewAdapter
import io.kess.ecommerce.ui.adapter.SizeAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import io.kess.ecommerce.view_model.ReviewViewModel
import io.kess.ecommerce.view_model.ShopViewModel
import kotlin.collections.find

class ShopDetailFragment : Fragment() {
    private var _binding: FragmentShopDetailBinding? = null
    private val binding get() = _binding!!
    private var shopId: String? = null
    private lateinit var shopViewModel: ShopViewModel

    private lateinit var productFragment: ProductShopFragment
    private lateinit var shopInfoFragment: ShopInfoFragment
    private var currentFragment: Fragment? = null
//    private var shopId: String = "9gaVbJ6yScvedfuqiBTM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopId = arguments?.getString("ID")
//            shopId = "9gaVbJ6yScvedfuqiBTM"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ProductShop", "create view")
        initViewModel()
        if(childFragmentManager.findFragmentByTag("Product") == null){
            setupFragments()
        }
        setupButtonClick()
        observeData()
    }

    private fun initViewModel() {
        shopViewModel = ViewModelProvider(this)[ShopViewModel::class.java]
        shopId?.let {
            shopViewModel.getShopDetail(it)
        }
    }

    private fun setupUi(shop: Shop) {
        binding.shopName.text = shop.shopName
        Glide.with(requireContext())
            .load(shop.logoUrl)
            .into(binding.imgBanner)
    }
    private fun setupLoading(isLoading: Boolean){
        if(isLoading){
            binding.toolbar.visibility = View.GONE
            binding.imageContainer.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
            binding.fragmentContainer.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.toolbar.visibility = View.VISIBLE
            binding.imageContainer.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun observeData(){
        shopViewModel.shopState.observe(viewLifecycleOwner){state ->
            when (state) {
                is UiState.Loading -> {
                    setupLoading(true)
                }

                is UiState.Success -> {
                    setupLoading(false)
                   setupUi(state.data)
                }

                is UiState.Error -> {
                    setupLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
        }

    }

    private fun setupFragments() {
        productFragment = ProductShopFragment().apply {
            arguments = Bundle().apply {
                putString("ID", shopId)
            }
        }
        shopInfoFragment = ShopInfoFragment().apply {
            arguments = Bundle().apply {
                putString("ID", shopId)
            }
        }
        childFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, productFragment, "Product")
            .add(R.id.fragmentContainer, shopInfoFragment, "Shop_Info")
            .hide(shopInfoFragment)
            .commit()
        currentFragment = productFragment
        selectAllProductsTab()
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment == currentFragment) return
        childFragmentManager.beginTransaction().hide(currentFragment!!).show(fragment).commit()
        currentFragment = fragment
    }

    private fun setupButtonClick() {
        binding.allProduct.setOnClickListener {
            switchFragment(productFragment)
            selectAllProductsTab()
        }
        binding.shopInfo.setOnClickListener {
            switchFragment(shopInfoFragment)
            selectShopInfoTab()
        }
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    private fun selectAllProductsTab() {

        binding.tvAllProduct.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )

        binding.tvShopInfo.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )

        binding.lineAllProduct.visibility = View.VISIBLE
        binding.lineShopInfo.visibility = View.GONE
    }

    private fun selectShopInfoTab() {

        binding.tvAllProduct.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )

        binding.tvShopInfo.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )
        binding.lineAllProduct.visibility = View.GONE
        binding.lineShopInfo.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
            (activity as MainActivity).showButtonNav(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ProductShop", "destroy view")
        _binding = null
        (activity as MainActivity).showButtonNav(show = true)
    }
}