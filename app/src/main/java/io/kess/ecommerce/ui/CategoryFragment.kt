package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCartBinding
import io.kess.ecommerce.databinding.FragmentCategoryBinding
import io.kess.ecommerce.model.Category
import io.kess.ecommerce.ui.adapter.CategoryAdapter
import io.kess.ecommerce.util.UserSession
import io.kess.ecommerce.view_model.CategoryViewModel
import io.kess.ecommerce.view_model.ProductViewModel

//import io.kess.ecommerce.adapter.ProductAdapter

class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryViewModel: CategoryViewModel
    private var categoryList: List<Category> = emptyList()
    private lateinit var categoryAdapter: CategoryAdapter
    val user = UserSession.currentUser

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
        setupUi()
        setupClickListener()
        setupRecyclerView()
        observeData()
    }
    private fun initViewModel(){
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
    }
    private fun setupUi(){
        if (user != null) {
            binding.tvGreeting.text = "Hi, ${user.name}"
        } else {
            binding.tvGreeting.text = "Guest"
        }
    }
    private fun setupClickListener(){
        binding.search.setOnClickListener {
            (activity as MainActivity).navigate(SearchFragment())
        }
    }
    private fun setupRecyclerView(){
        categoryAdapter = CategoryAdapter() { category -> openProductByCategory(category) }
        binding.recyclerCategory.apply {
            adapter = categoryAdapter
            layoutManager =  GridLayoutManager(requireContext(), 1)
        }
    }
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

    private fun observeData() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { result ->
            categoryList = result.sortedBy { it.createAt }
            categoryAdapter.submitList(categoryList)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}