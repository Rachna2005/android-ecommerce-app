package io.kess.ecommerce.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.databinding.FragmentDisplayProductBinding
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.ui.adapter.ProductAdapter
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import androidx.core.widget.addTextChangedListener
import com.google.firebase.Timestamp
import io.kess.ecommerce.ui.adapter.CategoryAdapter
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.R
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.util.UiState

class ProductListFragment : Fragment() {
    private var _binding: FragmentDisplayProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var favoriteViewModel: FavoriteViewModel
    private var type: String = "ALL"
    private var categoryId: String? = null
    private var productList = listOf<Product>()
    private var favoriteSet: Set<String> = emptySet()
    private lateinit var viewModel: ProductViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("TYPE") ?: "ALL"
        categoryId = arguments?.getString("CATEGORY_ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplayProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupClickListener()
        setupRecyclerView()
        loadProduct()
        observeProducts()
        setupSearch()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
    }

    private fun setupSearch() {
        binding.search.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            if (query.isEmpty()) {
                productAdapter.submitList(productList)
            } else {
                val filtered = productList.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                productAdapter.submitList(filtered)
            }
        }
    }

    private fun setupRecyclerView() {
        productAdapter = when (type) {
            "DISCOUNT" -> {
                ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
                    favoriteViewModel.toggleFavorite(product.id)
                }, { product -> openProductDetail(product.id) })
            }

            else -> {
                ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
                    favoriteViewModel.toggleFavorite(product.id)
                }, { product -> openProductDetail(product.id) })
            }
        }
        binding.listProduct.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
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

    private fun loadProduct() {
        when (type) {
            "CATEGORY" -> {
                categoryId?.let { viewModel.categoryProduct(it) }
            }

            else -> {
//                viewModel.loadAllProducts()
            }
        }
    }

    private fun observeProducts() {
        viewModel.products.observe(viewLifecycleOwner) { state ->
            when (state) {

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
//                    binding.contentLayout.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
//                    binding.contentLayout.visibility = View.VISIBLE

                    val products = state.data
                    productList = products

                    updateUi(productList, favoriteSet)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UiState.Idle -> {}
            }
        }

        favoriteViewModel.favorite.observe(viewLifecycleOwner) { favorites ->
            favoriteSet = favorites
            updateUi(productList, favoriteSet)
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner) {
            productAdapter.updateLoadingFavorite(it)
        }
    }

    private fun updateUi(products: List<Product>, favorite: Set<String>) {
        val result = when (type) {
            "CATEGORY" -> {
                binding.title.text = arguments?.getString("CATEGORY_NAME") ?: "Category"
                products
            }

            "DISCOUNT" -> {
                binding.title.text = "Discount Products"
                products.filter { (it.discountPercentage ?: 0.0) > 0 }
            }

            "NEW_ARRIVAL" -> {
                binding.title.text = "New Arrivals"
                products.sortedByDescending { it.createdAt?.seconds ?: 0 }
            }

            "ALL" -> {
                binding.title.text = "All Products"
                products
            }

            "FAVORITE" -> {
                binding.title.text = "My Wishlist"
                binding.filterContainer.visibility = View.GONE
                binding.searchContainer.visibility = View.GONE
                val favoriteProduct = products.filter { favorite.contains(it.id) }
                binding.layoutEmptyCart.visibility =
                    if (favoriteProduct.isEmpty()) View.VISIBLE else View.GONE
                favoriteProduct
            }

            else -> {
                binding.title.text = "Products"
                products
            }
        }
        productAdapter.submitList(result)
        productAdapter.updateFavorites(favorite)
    }

    private fun setupClickListener() {
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnCart.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (activity as MainActivity).selectBottomNav(R.id.nav_cart)
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