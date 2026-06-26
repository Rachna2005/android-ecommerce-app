package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCartBinding
import io.kess.ecommerce.databinding.FragmentCategoryBinding
import io.kess.ecommerce.model.Category
import io.kess.ecommerce.model.User
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.CategoryAdapter
import io.kess.ecommerce.ui.adapter.ShopAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.CategoryViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import io.kess.ecommerce.view_model.ShopViewModel


class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var shopViewModel: ShopViewModel
    private lateinit var shopAdapter: ShopAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupClickListener()
//        setupRecyclerView()
//        observeData()
    }
    private fun initViewModel(){
        shopViewModel = ViewModelProvider(requireActivity())[ShopViewModel::class.java]

    }
    private fun setupClickListener(){
        binding.search.setOnClickListener {
//            (activity as MainActivity).navigate(SearchFragment())
        }
    }
//    private fun setupRecyclerView(){
//        shopAdapter = ShopAdapter() {}
//        binding.recyclerShop.apply {
//            adapter = shopAdapter
//            layoutManager =  GridLayoutManager(requireContext(), 3)
//        }
//    }
    private fun openProductByCategory(category: Category) {
        val fragment = ProductListFragment().apply {
            arguments = Bundle().apply {
                putString("TYPE", "CATEGORY")
                putString("CATEGORY_ID", category.id)
                putString("CATEGORY_NAME", category.name)
            }
        }
        (activity as MainActivity).navigate(fragment)
    }

//    private fun observeData() {
//        shopViewModel.shops.observe(viewLifecycleOwner) { state ->
//            when (state) {
//                is UiState.Loading -> {
//                    binding.progressBar.visibility =
//                        View.VISIBLE
//                    binding.recyclerShop.visibility = View.GONE
//                }
//                is UiState.Success -> {
//                    binding.progressBar.visibility = View.GONE
//                    binding.recyclerShop.visibility = View.VISIBLE
//                    shopAdapter.submitList(state.data)
//                }
//                is UiState.Error -> {
//                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}