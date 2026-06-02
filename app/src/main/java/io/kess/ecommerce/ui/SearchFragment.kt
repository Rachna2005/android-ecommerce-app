package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import io.kess.ecommerce.databinding.FragmentSearchBinding
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.ui.adapter.ProductAdapter
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
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_search, container, false)
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        viewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

        searchAdapter = ProductAdapter(emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, {product -> openProductDetail(product.id)})
        binding.recyclerView.adapter = searchAdapter
        binding.recyclerView.layoutManager =  GridLayoutManager(requireContext(), 2)
        viewModel.products.observe(viewLifecycleOwner)
        { products ->
            productList = products
            val query = binding.search.text.toString().trim()
            setupSearch(query)
        }
        favoriteViewModel.favorite.observe(viewLifecycleOwner) {
            favorite = it
            searchAdapter.updateFavorites(favorite)
        }
        binding.search.addTextChangedListener { editable ->

            setupSearch(editable.toString().trim())
        }
    }

    private fun setupSearch(query: String) {
            if (query.isEmpty()) {
                searchAdapter.submitList(emptyList())
            } else {
                val filteredList = productList.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                searchAdapter.submitList(filteredList)
            }
    }
    private fun openProductDetail(productId: String){
        val fragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", productId)
            }
        }
        (activity as MainActivity).navigation(fragment)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showButtonNav(show = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showButtonNav(show = true)
    }

}