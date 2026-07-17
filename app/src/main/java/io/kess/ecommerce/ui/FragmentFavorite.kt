package io.kess.ecommerce.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.kess.ecommerce.databinding.FragmentFavoriteScreenBinding
import io.kess.ecommerce.databinding.FragmentProductShopBinding
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.HomeProductAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class FragmentFavorite : Fragment() {
    private var _binding: FragmentFavoriteScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var productViewModel: ProductViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var productAdapter: HomeProductAdapter
    private var favorite: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupAdapters()
        observeData()
        setupClickListener()
    }

    private fun initViewModel() {
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

    }

    private fun setupAdapters() {
        productAdapter = HomeProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, { product -> openProductDetail(product.id) })

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

    private fun setupClickListener() {
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeData() {
        productViewModel.favoriteProducts.observe(viewLifecycleOwner) { state ->
            when (state) {

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE

                    binding.listProduct.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE

                    binding.listProduct.visibility = View.VISIBLE
                    productAdapter.submitList(state.data)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE

                    binding.listProduct.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is UiState.Idle -> {}
            }
        }

        favoriteViewModel.favorite.observe(viewLifecycleOwner) {
            favorite = it
            productViewModel.getFavoriteProduct(favorite)
            productAdapter.updateFavorites(it)
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner) {
            productAdapter.updateLoadingFavorite(it)
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