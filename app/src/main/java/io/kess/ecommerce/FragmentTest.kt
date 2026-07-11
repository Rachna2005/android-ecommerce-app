//package io.kess.ecommerce.ui
//
//import android.os.Bundle
//
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.util.Log
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.benchmark.traceprocessor.Insight
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.fragment.app.FragmentManager
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import io.kess.ecommerce.databinding.FragmentDisplayProductBinding
//import io.kess.ecommerce.model.Product
//import io.kess.ecommerce.ui.adapter.ProductAdapter
//import io.kess.ecommerce.view_model.AuthViewModel
//import io.kess.ecommerce.view_model.ProductViewModel
//import androidx.core.widget.addTextChangedListener
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.slider.RangeSlider
//import com.google.firebase.Timestamp
//import io.kess.ecommerce.ui.adapter.CategoryAdapter
//import io.kess.ecommerce.view_model.FavoriteViewModel
//import io.kess.ecommerce.R
//import io.kess.ecommerce.databinding.FragmentTestBinding
//import io.kess.ecommerce.model.Category
//import io.kess.ecommerce.model.ProductFilter
//import io.kess.ecommerce.model.Shop
//import io.kess.ecommerce.repository.ProductRepository
//import io.kess.ecommerce.ui.activity.MainActivity
//import io.kess.ecommerce.ui.adapter.CategoryFilter
//import io.kess.ecommerce.ui.adapter.ShopFilter
//import io.kess.ecommerce.util.UiState
//import io.kess.ecommerce.view_model.CategoryViewModel
//import io.kess.ecommerce.view_model.ShopViewModel
//
//class FragmentTest : Fragment() {
//    private var _binding: FragmentTestBinding? = null
//    private val binding get() = _binding!!
//    private var selectedShop: String? = null
//    private var selectedCategory: String? = null
//
//    private lateinit var productAdapter: ProductAdapter
//    private lateinit var shopAdapter: ShopFilter
//    private lateinit var categoryAdapter: CategoryFilter
//    private lateinit var favoriteViewModel: FavoriteViewModel
//    private val repository = ProductRepository()
//    private var productList = listOf<Product>()
//    private var shopList: List<Shop> = emptyList()
//    private var categoryList: List<Category> = emptyList()
//    private var favoriteSet: Set<String> = emptySet()
//    private lateinit var viewModel: ProductViewModel
//    private lateinit var shopViewModel: ShopViewModel
//    private lateinit var categoryViewModel: CategoryViewModel
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTestBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        initViewModel()
//        setupClickListener()
//        setupRecyclerView()
//        loadProduct()
//        observeProducts()
//        setupSearch()
////        repository.debugBrokenProducts()
//    }
//
//    private fun initViewModel() {
//        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
//        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
//        shopViewModel = ViewModelProvider(this)[ShopViewModel::class.java]
//        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
//        shopViewModel.getAllShops()
////        categoryViewModel.loadCategories()
//    }
//
//    private fun setupSearch() {
//        binding.search.addTextChangedListener { editable ->
//            val query = editable.toString().trim()
//            if (query.isEmpty()) {
//                productAdapter.submitList(productList)
//            } else {
//                val filtered = productList.filter {
//                    it.name.contains(query, ignoreCase = true)
//                }
//                productAdapter.submitList(filtered)
//            }
//        }
//    }
//
//    private fun showFilterBottomSheet() {
////        selectedShop = null
////        selectedCategory = null
//        val view = layoutInflater.inflate(R.layout.filter_bottom_sheet, null)
//        val sheet = BottomSheetDialog(requireContext())
//        sheet.setContentView(view)
//
//        val shopView = view.findViewById<RecyclerView>(R.id.shop)
//        shopAdapter = ShopFilter(
//            onClick = { shop ->
//                selectedShop = shop.id
//            }
//        )
//        shopView.apply {
//            layoutManager =
//                LinearLayoutManager(requireContext())
//            adapter = shopAdapter
//        }
//
//        val categoryView = view.findViewById<RecyclerView>(R.id.category)
//        categoryAdapter = CategoryFilter(
//            onClick = { category ->
//                selectedCategory = category.id
//            }
//        )
//        categoryView.apply {
//            layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            adapter = categoryAdapter
//        }
//
//        val slider = view.findViewById<RangeSlider>(R.id.sliderPrice)
//        slider.valueFrom = 0f
//        slider.valueTo = 1000f
//        slider.values = listOf(0f, 1000f)
//
//        val tvPriceRange = view.findViewById<TextView>(R.id.tvPriceRange)
//        slider.addOnChangeListener { s, _, _ ->
//            val min = s.values[0].toInt()
//            val max = s.values[1].toInt()
//            tvPriceRange.text = "$$min - $$max"
//        }
//
//        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
//        val btnApply = view.findViewById<Button>(R.id.btnApply)
//        val clearFilter = view.findViewById<TextView>(R.id.clear)
//        btnClose.setOnClickListener {
//            sheet.dismiss()
//        }
//
//        btnApply.setOnClickListener {
//            val minPrice: Int?
//            val maxPrice: Int?
//            if (slider.values[0] == slider.valueFrom && slider.values[1] == slider.valueTo) {
//                minPrice = null
//                maxPrice = null
//            } else {
//                minPrice = slider.values[0].toInt()
//                maxPrice = slider.values[1].toInt()
//            }
//            val newFilter = ProductFilter(selectedCategory, minPrice, maxPrice, selectedShop)
//
//            if (newFilter == viewModel.getCurrentFilter()) {
//                sheet.dismiss()
//                return@setOnClickListener
//            }
//            viewModel.loadAllProducts(
//                true,
//                6,
//                newFilter
//            )
//            sheet.dismiss()
//        }
//        clearFilter.setOnClickListener {
////            selectedShop = null
////            selectedCategory = null
////            viewModel.loadAllProducts(true, 6)
//            if (selectedShop == null && selectedCategory == null) {
//                sheet.dismiss()
//            } else {
//                selectedShop = null
//                selectedCategory = null
//                viewModel.loadAllProducts(true, 6)
//                sheet.dismiss()
//            }
//
//        }
//
//        sheet.setOnDismissListener {
//
//        }
//        shopAdapter.submitList(shopList)
//        shopAdapter.setSelected(selectedShop)
//        categoryAdapter.submitList(categoryList)
//        categoryAdapter.setSelected(selectedCategory)
//
//        sheet.show()
//    }
//
//    private fun setupRecyclerView() {
//        productAdapter = ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
//            favoriteViewModel.toggleFavorite(product.id)
//        }, { product -> openProductDetail(product.id) })
//
//        binding.listProduct.apply {
//            adapter = productAdapter
//            layoutManager = GridLayoutManager(requireContext(), 2)
//        }
//    }
//
//    private fun openProductDetail(productId: String) {
//        val fragment = ProductDetailFragment().apply {
//            arguments = Bundle().apply {
//                putString("ID", productId)
//            }
//        }
//        (activity as MainActivity).navigate(fragment)
//    }
//
//    private fun loadProduct() {
//        viewModel.loadAllProducts(limit = 6)
//    }
//
//    private fun observeProducts() {
//        viewModel.products.observe(viewLifecycleOwner) { state ->
//            when (state) {
//
//                is UiState.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
//                }
//
//                is UiState.Success -> {
//                    binding.progressBar.visibility = View.GONE
//
//
//                    val products = state.data
//                    productList = products
//
//                    updateUi(productList, favoriteSet)
//                }
//
//                is UiState.Error -> {
//                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(
//                        requireContext(),
//                        state.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.d("FILTER_DEBUG", state.message)
//                }
//
//                is UiState.Idle -> {}
//            }
//        }
//
//        shopViewModel.shops.observe(viewLifecycleOwner) { state ->
//            when (state) {
//
//                is UiState.Loading -> {
////                    binding.progressBar.visibility = View.VISIBLE
//                }
//
//                is UiState.Success -> {
////                    binding.progressBar.visibility = View.GONE
////                    val products = state.data
////                    productList = products
//                    shopList = state.data
//
////                    updateUi(productList, favoriteSet)
//                }
//
//                is UiState.Error -> {
////                    binding.progressBar.visibility = View.GONE
////                    Toast.makeText(
////                        requireContext(),
////                        state.message,
////                        Toast.LENGTH_SHORT
////                    ).show()
//                }
//
//                is UiState.Idle -> {}
//            }
//        }
//        categoryViewModel.categories.observe(viewLifecycleOwner) {
//            categoryList = it
//        }
//
//        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
//            binding.loadMoreContainer.visibility = if (loading) View.VISIBLE else View.GONE
//        }
//
//        favoriteViewModel.favorite.observe(viewLifecycleOwner) { favorites ->
//            favoriteSet = favorites
//            updateUi(productList, favoriteSet)
//        }
//        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner) {
//            productAdapter.updateLoadingFavorite(it)
//        }
//    }
//
//    private fun updateUi(products: List<Product>, favorite: Set<String>) {
//        binding.title.text = "All Products"
//        productAdapter.submitList(products)
//        productAdapter.updateFavorites(favorite)
//    }
//
//    private fun setupClickListener() {
//        binding.backBtn.setOnClickListener {
//
//        }
//        binding.btnCart.setOnClickListener {
//
//        }
//        binding.filterContainer.setOnClickListener {
//            showFilterBottomSheet()
//        }
//
//        binding.listProduct.addOnScrollListener(
//            object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    if (dy <= 0) return
//                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
//                    val totalItemCount = layoutManager.itemCount
//                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
//                    val isAtBottom = !recyclerView.canScrollVertically(1)
//
//                    if (
//                        isAtBottom &&
////                        !viewModel.isLoading &&
//                        !viewModel.isLastPage
//                    ) {
//                        viewModel.loadAllProducts(limit = 6)
//                    }
//                }
//            }
//        )
//    }
//
//    override fun onResume() {
//        super.onResume()
////        (activity as MainActivity).showButtonNav(show = false)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
////        (activity as MainActivity).showButtonNav(show = true)
//    }
//}