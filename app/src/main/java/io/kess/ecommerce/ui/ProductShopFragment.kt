package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProductShopBinding
import io.kess.ecommerce.databinding.FragmentShopDetailBinding
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.ProductAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class ProductShopFragment : Fragment() {

    private var _binding: FragmentProductShopBinding? = null
    private val binding get() = _binding!!
//    private var shopId: String? = null
private var shopId: String = "q96laKzLcb7x5zGXL9qY"
    private lateinit var productViewModel: ProductViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var productAdapter: ProductAdapter
    private var productList = listOf<Product>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        shopId = arguments?.getString("ID")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupAdapters()
        setupRecyclerViews()
        setupButtonClick()
        observeData()
    }

    private fun initViewModel() {
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        shopId?.let {
            productViewModel.getProductByShop(it)
        }
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
    }
    private fun setupAdapters() {
        productAdapter = ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, { product -> openProductDetail(product.id) })

        binding.recyclerView.apply {
//            adapter = productAdapter
//            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun openProductDetail(productId: String) {
        val fragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", productId)
            }
        }
        (activity as MainActivity).navigate(fragment)
    }

    private fun observeData(){
        productViewModel.products.observe(viewLifecycleOwner) { state ->
            when (state) {

                is UiState.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
//                    binding.contentLayout.visibility = View.GONE
                }

                is UiState.Success -> {
//                    binding.progressBar.visibility = View.GONE
//                    binding.contentLayout.visibility = View.VISIBLE

                    val products = state.data
                    productList = products

//                    productAdapter.submitList(productList)

//                    updateUi(productList, favoriteSet)
                }

                is UiState.Error -> {
//                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UiState.Idle -> {}
            }
        }
    }

    private fun setupRecyclerViews() {

    }

    private fun setupButtonClick() {

    }

    override fun onResume() {
        super.onResume()
//        (activity as MainActivity).showButtonNav(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        (activity as MainActivity).showButtonNav(true)
    }
}