package io.kess.ecommerce.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.RangeSlider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.kess.ecommerce.ui.adapter.CategoryAdapter
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Category

import io.kess.ecommerce.model.ProductQuery
import io.kess.ecommerce.model.Shop
import io.kess.ecommerce.repository.ProductDisplayType
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.CategoryFilter
import io.kess.ecommerce.ui.adapter.ShopFilter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CategoryViewModel
import io.kess.ecommerce.view_model.ShopViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class ProductListFragment : Fragment() {
    private var _binding: FragmentDisplayProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var favoriteViewModel: FavoriteViewModel
    private var selectedShop: String? = null
    private var selectedCategory: String? = null
    private lateinit var shopAdapter: ShopFilter
    private lateinit var categoryAdapter: CategoryFilter
    private var type: String = "ALL"
    private var shopList: List<Shop> = emptyList()
    private var categoryList: List<Category> = emptyList()
    private var categoryId: String? = null
    private var favoriteSet: Set<String> = emptySet()
    private lateinit var viewModel: ProductViewModel
    private lateinit var shopViewModel: ShopViewModel
    private lateinit var categoryViewModel: CategoryViewModel
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
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        shopViewModel = ViewModelProvider(requireActivity())[ShopViewModel::class.java]
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        shopViewModel.getAllShops()
        categoryViewModel.loadCategories()
    }

    private fun showFilterBottomSheet() {
        val currentFilter = viewModel.productQuery.value
        selectedShop = currentFilter.shopId
        selectedCategory = currentFilter.categoryId

        val view = layoutInflater.inflate(R.layout.filter_bottom_sheet, null)
        val sheet = BottomSheetDialog(requireContext())
        sheet.setContentView(view)


        val shopView = view.findViewById<RecyclerView>(R.id.shop)
        shopAdapter = ShopFilter(
            onClick = { shop ->
                if (shop == null) {
                    selectedShop = null
                } else {
                    selectedShop = shop.id
                }
            }
        )
        shopView.apply {
            layoutManager =
                LinearLayoutManager(requireContext())
            adapter = shopAdapter
        }

        val categoryView = view.findViewById<RecyclerView>(R.id.category)
        val categoryTitle = view.findViewById<TextView>(R.id.categoryTitle)
        if (type == "CATEGORY") {
            categoryView.visibility = View.GONE
            categoryTitle.visibility = View.GONE
        }
        categoryAdapter = CategoryFilter(
            onClick = { category ->
                if (category == null) {
                    selectedCategory = null
                } else {
                    selectedCategory = category.id
                }
            }
        )
        categoryView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        val slider = view.findViewById<RangeSlider>(R.id.sliderPrice)
        slider.valueFrom = 0f
        slider.valueTo = 1000f
        if (currentFilter.minPrice != null && currentFilter.maxPrice != null) {
            slider.values = listOf(
                currentFilter.minPrice.toFloat(),
                currentFilter.maxPrice.toFloat()
            )
        } else {
            slider.values = listOf(
                slider.valueFrom,
                slider.valueTo
            )
        }
        val tvPriceRange = view.findViewById<TextView>(R.id.tvPriceRange)
        tvPriceRange.text = "$${slider.values[0].toInt()} - $${slider.values[1].toInt()}"
        slider.addOnChangeListener { s, _, _ ->

            tvPriceRange.text = "$${slider.values[0].toInt()} - $${slider.values[1].toInt()}"
        }

        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val btnApply = view.findViewById<Button>(R.id.btnApply)
        val clearFilter = view.findViewById<TextView>(R.id.clear)
        btnClose.setOnClickListener {
            sheet.dismiss()
        }

        btnApply.setOnClickListener {
            val minPrice: Int?
            val maxPrice: Int?
            if (slider.values[0] == slider.valueFrom && slider.values[1] == slider.valueTo) {
                minPrice = null
                maxPrice = null
            } else {
                minPrice = slider.values[0].toInt()
                maxPrice = slider.values[1].toInt()
            }
            val newFilter = when (type) {
                "CATEGORY" -> {
                    ProductQuery(
                        ProductDisplayType.ALL,
                        categoryId,
                        minPrice,
                        maxPrice,
                        selectedShop, isSearch = false
                    )

                }

                "DISCOUNT" -> {
                    ProductQuery(
                        ProductDisplayType.DISCOUNT,
                        selectedCategory,
                        minPrice,
                        maxPrice,
                        selectedShop, isSearch = false
                    )
                }

                "NEW_ARRIVAL" -> {
                    ProductQuery(
                        ProductDisplayType.NEW_ARRIVAL,
                        selectedCategory,
                        minPrice,
                        maxPrice,
                        selectedShop, isSearch = false
                    )
                }

                "ALL" -> {
                    ProductQuery(
                        ProductDisplayType.ALL,
                        selectedCategory,
                        minPrice,
                        maxPrice,
                        selectedShop, isSearch = false
                    )
                }

                else -> {
                    ProductQuery(
                        ProductDisplayType.ALL,
                        selectedCategory,
                        minPrice,
                        maxPrice,
                        selectedShop, isSearch = false
                    )
                }
            }
            if (newFilter == currentFilter) {
                Log.d("FILTER", "SAME FILTER RETURN")
                sheet.dismiss()
                return@setOnClickListener
            }
            viewModel.loadProduct(newFilter)
            updateFilterUi(newFilter)
            sheet.dismiss()
        }
        val isEmptyFilter =
            currentFilter.categoryId == null && currentFilter.shopId == null && currentFilter.minPrice == null && currentFilter.maxPrice == null
        clearFilter.setOnClickListener {
            if (isEmptyFilter) {
                sheet.dismiss()
            } else {
                val clearedFilter = when (type) {
                    "CATEGORY" -> ProductQuery(
                        displayType = ProductDisplayType.ALL,
                        categoryId = categoryId,
                        isSearch = false
                    )

                    "DISCOUNT" -> ProductQuery(
                        displayType = ProductDisplayType.DISCOUNT,
                        isSearch = false
                    )

                    "NEW_ARRIVAL" -> ProductQuery(
                        displayType = ProductDisplayType.NEW_ARRIVAL,
                        isSearch = false
                    )

                    else -> ProductQuery(
                        displayType = ProductDisplayType.ALL,
                        isSearch = false
                    )
                }
                viewModel.loadProduct(clearedFilter)
                updateFilterUi(clearedFilter)
                sheet.dismiss()
            }
        }
        shopAdapter.submitList(shopList)
        shopAdapter.setSelected(selectedShop)
        categoryAdapter.submitList(categoryList)
        categoryAdapter.setSelected(selectedCategory)
        sheet.show()
    }

    private fun updateFilterUi(query: ProductQuery) {
        var filterCount = 0
        if (!query.categoryId.isNullOrEmpty()) {
            filterCount++
        }
        if (!query.shopId.isNullOrEmpty()) {
            filterCount++
        }
        if (query.minPrice != null || query.maxPrice != null) {
            filterCount++
        }
        if (filterCount > 0) {
            binding.filterContainer.setBackgroundResource(R.drawable.bg_filter_active)
            binding.tvFilterCount.visibility = View.VISIBLE
            binding.tvFilterCount.text = filterCount.toString()
        } else {
            binding.filterContainer.setBackgroundResource(R.drawable.bg_filter_normal)
            binding.tvFilterCount.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {

        productAdapter = ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, { product -> openProductDetail(product.id) })

        binding.listProduct.adapter = productAdapter
        binding.listProduct.layoutManager = GridLayoutManager(requireContext(), 2)

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
//        viewModel.loadProduct(ProductQuery(displayType = ProductDisplayType.ALL))

        when (type) {
            "CATEGORY" -> {
                categoryId?.let {
                    viewModel.loadProduct(
                        ProductQuery(
                            displayType = ProductDisplayType.ALL,
                            categoryId = categoryId, isSearch = false
                        )
                    )
                }
                binding.title.text = arguments?.getString("CATEGORY_NAME") ?: "Category"
            }

            "DISCOUNT" -> {
                viewModel.loadProduct(
                    ProductQuery(
                        displayType = ProductDisplayType.DISCOUNT,
                        isSearch = false
                    )
                )
                binding.title.text = "Discount Products"
            }

            "NEW_ARRIVAL" -> {
                viewModel.loadProduct(
                    ProductQuery(
                        displayType = ProductDisplayType.NEW_ARRIVAL,
                        isSearch = false
                    )
                )
                binding.title.text = "New Arrivals"
            }

            "ALL" -> {
                viewModel.loadProduct(
                    ProductQuery(
                        displayType = ProductDisplayType.ALL,
                        isSearch = false
                    )
                )
                binding.title.text = "All Products"
            }

            else -> {
                viewModel.loadProduct(ProductQuery(displayType = ProductDisplayType.ALL))
                binding.title.text = "Products"
            }
        }
    }

    private fun observeProducts() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.product.collectLatest {
                productAdapter.submitData(it)
//                productAdapter.updateFavorites()
            }
        }

        productAdapter.addLoadStateListener { loadStates ->
            when (val state = loadStates.refresh) {
                is LoadState.Loading -> {
                    binding.listProduct.visibility = View.GONE
                    binding.searchSection.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }

                is LoadState.NotLoading -> {
                    binding.listProduct.visibility = View.VISIBLE
                    binding.searchSection.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    if (productAdapter.itemCount == 0) {
                        binding.layoutEmptyCart.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmptyCart.visibility = View.GONE
                    }
                }

                is LoadState.Error -> {
                    binding.listProduct.visibility = View.VISIBLE
                    binding.searchSection.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        shopViewModel.shops.observe(viewLifecycleOwner) { state ->
            when (state) {

                is UiState.Loading -> {

                }

                is UiState.Success -> {
                    shopList = state.data
                }

                is UiState.Error -> {

                }

                is UiState.Idle -> {}
            }
        }
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryList = it
        }
        favoriteViewModel.favorite.observe(viewLifecycleOwner) { favorites ->
            favoriteSet = favorites
            productAdapter.updateFavorites(favoriteSet)
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner) {
            productAdapter.updateLoadingFavorite(it)
        }
    }

    private fun setupClickListener() {
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.filterContainer.setOnClickListener {
            showFilterBottomSheet()
        }
        binding.btnCart.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (activity as MainActivity).selectBottomNav(R.id.nav_cart)
        }
        binding.searchContainer.setOnClickListener {
            (activity as MainActivity).navigate(SearchFragment())
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