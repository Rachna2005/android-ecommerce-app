package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import io.kess.ecommerce.databinding.FragmentSearchBinding
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.ProductAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProductViewModel
    private lateinit var searchAdapter: ProductAdapter
    private var favorite: Set<String> = emptySet()
    private lateinit var favoriteViewModel: FavoriteViewModel
    private var productList = listOf<Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        initViewModel()
        setupAdapter()
//        observeData()
        setupSearch()
    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
    }

    private fun setupAdapter(){
        searchAdapter = ProductAdapter(emptySet(),loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, {product -> openProductDetail(product.id)})
//        binding.recyclerView.adapter = searchAdapter
        binding.recyclerView.layoutManager =  GridLayoutManager(requireContext(), 2)
    }

    private fun observeData(){
        viewModel.products.observe(viewLifecycleOwner)
        { state ->
            when (state) {

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE

                    productList = state.data

                    val query = binding.search.text.toString().trim()
                    setupSearch(query)
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
        favoriteViewModel.favorite.observe(viewLifecycleOwner) {
            favorite = it
            searchAdapter.updateFavorites(favorite)
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner){
            searchAdapter.updateLoadingFavorite(it)
        }
    }

    private fun setupSearch(){
        binding.search.addTextChangedListener { editable ->
            setupSearch(editable.toString().trim())
        }
    }


    private fun setupSearch(query: String) {
            if (query.isBlank()) {
//                searchAdapter.submitList(emptyList())
            } else {
                val filteredList = productList.filter {
                    it.name.contains(query, ignoreCase = true)
                }
//                searchAdapter.submitList(filteredList)
            }
    }
    private fun openProductDetail(productId: String){
        val fragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", productId)
            }
        }
        (activity as MainActivity).navigate(fragment)
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