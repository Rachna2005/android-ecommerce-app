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
import androidx.lifecycle.lifecycleScope
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
import io.kess.ecommerce.model.ProductFilter
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
    private var productList = listOf<Product>()
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
//        initViewModel()
//        setupClickListener()
//        setupRecyclerView()
//        observeProducts()
//        loadProduct()
//        setupSearch()

        val oneWeekAgo = Timestamp(Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000))
        var query: Query = FirebaseFirestore.getInstance().collection("products")
//        query = query.whereGreaterThan("discountPercentage", 0.0)

        query = query.whereGreaterThanOrEqualTo("createdAt", oneWeekAgo)

        query.orderBy("createdAt")
        query.get()
            .addOnSuccessListener { result ->
                val productList = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }
                Log.d("COUNT", productList.size.toString())
            }.addOnFailureListener { e ->
            }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        shopViewModel = ViewModelProvider(requireActivity())[ShopViewModel::class.java]
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        shopViewModel.getAllShops()
        categoryViewModel.loadCategories()
    }

    private fun setupSearch() {
        binding.search.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            if (query.isEmpty()) {
//                productAdapter.submitList(productList)
            } else {
                val filtered = productList.filter {
                    it.name.contains(query, ignoreCase = true)
                }
//                productAdapter.submitList(filtered)
            }
        }
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
                selectedShop = shop.id
            }
        )
        shopView.apply {
            layoutManager =
                LinearLayoutManager(requireContext())
            adapter = shopAdapter
        }

        val categoryView = view.findViewById<RecyclerView>(R.id.category)
        categoryAdapter = CategoryFilter(
            onClick = { category ->
                selectedCategory = category.id
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
            val newFilter = ProductQuery(
                ProductDisplayType.ALL,
                selectedCategory,
                minPrice,
                maxPrice,
                selectedShop
            )

            if (newFilter == currentFilter) {
                Log.d("FILTER", "SAME FILTER RETURN")
                sheet.dismiss()
                return@setOnClickListener
            }
            viewModel.loadProduct(newFilter)
            sheet.dismiss()
        }
        val isEmptyFilter =
            currentFilter.categoryId == null && currentFilter.shopId == null && currentFilter.minPrice == null && currentFilter.maxPrice == null
        clearFilter.setOnClickListener {
            if (isEmptyFilter) {
                sheet.dismiss()
            } else {
//                selectedShop = null
//                selectedCategory = null
                viewModel.loadProduct(ProductQuery(displayType = ProductDisplayType.NEW_ARRIVAL))
                sheet.dismiss()
            }

        }

        shopAdapter.submitList(shopList)
        shopAdapter.setSelected(selectedShop)
        categoryAdapter.submitList(categoryList)
        categoryAdapter.setSelected(selectedCategory)

        sheet.show()
    }

    private fun setupRecyclerView() {
//        productAdapter = when (type) {
//            "DISCOUNT" -> {
//                ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
//                    favoriteViewModel.toggleFavorite(product.id)
//                }, { product -> openProductDetail(product.id) })
//            }
//
//            else -> {
//                ProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
//                    favoriteViewModel.toggleFavorite(product.id)
//                }, { product -> openProductDetail(product.id) })
//            }
//        }
//        binding.listProduct.apply {
////            adapter = productAdapter
////            layoutManager = GridLayoutManager(requireContext(), 2)
//        }

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
        viewModel.loadProduct(ProductQuery(displayType = ProductDisplayType.NEW_ARRIVAL))

//        FirebaseFirestore.getInstance().collection("products")
//            .orderBy("createdAt", Query.Direction.ASCENDING)
//            .get()
//            .addOnSuccessListener { snapshot ->
//                Log.d("TEST", "Total = ${snapshot.size()}")
//
//                snapshot.documents.forEach {
//                    Log.d(
//                        "TEST",
//                        "${it.id} createdAt=${it.get("createdAt")}"
//                    )
//                }
//            }

//        when (type) {
//            "CATEGORY" -> {
//                categoryId?.let { viewModel.categoryProduct(it) }
//            }
//
//            else -> {
////                viewModel.loadAllProducts()
//            }
//        }

//        when (type) {
//            "CATEGORY" -> {
//                categoryId?.let {
//                    viewModel.loadAllProducts(
//                        limit = 6,
//                        displayType = ProductDisplayType.ALL,
//                        filter = ProductFilter(categoryId = categoryId)
//                    )
//                }
//            }
//
//            "DISCOUNT" -> {
//                viewModel.loadAllProducts(
//                    limit = 6,
//                    displayType = ProductDisplayType.DISCOUNT
//                )
//            }
//
//            "NEW_ARRIVAL" -> {
//                viewModel.loadAllProducts(
//                    limit = 6,
//                    displayType = ProductDisplayType.NEW_ARRIVAL
//                )
//            }
//
//            "ALL" -> {
//                viewModel.loadAllProducts(
//                    limit = 6,
//                    displayType = ProductDisplayType.ALL
//                )
//            }
//            else -> {
//                viewModel.loadAllProducts(
//                    limit = 6,
//                    displayType = ProductDisplayType.ALL
//                )
//            }
//        }
    }

    private fun observeProducts() {

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
//                    val products = state.data
//                    productList = products
//                }
//
//                is UiState.Error -> {
//                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(
//                        requireContext(),
//                        state.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                is UiState.Idle -> {}
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.product.collectLatest {
                productAdapter.submitData(it)
            }
        }

        shopViewModel.shops.observe(viewLifecycleOwner) { state ->
            when (state) {

                is UiState.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {
//                    binding.progressBar.visibility = View.GONE
//                    val products = state.data
//                    productList = products
                    shopList = state.data

//                    updateUi(productList, favoriteSet)
                }

                is UiState.Error -> {
//                    binding.progressBar.visibility = View.GONE
//                    Toast.makeText(
//                        requireContext(),
//                        state.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }

                is UiState.Idle -> {}
            }
        }
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryList = it
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
            binding.loadMoreContainer.visibility = if (loading) View.VISIBLE else View.GONE
        }

        favoriteViewModel.favorite.observe(viewLifecycleOwner) { favorites ->
            favoriteSet = favorites
            productAdapter.updateFavorites(favoriteSet)
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner) {
            productAdapter.updateLoadingFavorite(it)
        }
    }

//    private fun updateUi(products: List<Product>, favorite: Set<String>) {
//        val result = when (type) {
//            "CATEGORY" -> {
//                binding.title.text = arguments?.getString("CATEGORY_NAME") ?: "Category"
//                products
//            }
//
//            "DISCOUNT" -> {
//                binding.title.text = "Discount Products"
//                products.filter { (it.discountPercentage ?: 0.0) > 0 }
//            }
//
//            "NEW_ARRIVAL" -> {
//                binding.title.text = "New Arrivals"
//                products.sortedByDescending { it.createdAt?.seconds ?: 0 }
//            }
//
//            "ALL" -> {
//                binding.title.text = "All Products"
//                products
//            }
//            else -> {
//                binding.title.text = "Products"
//                products
//            }
//        }
//        productAdapter.submitList(result)
//        productAdapter.updateFavorites(favorite)
//    }

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
////                        viewModel.loadAllProducts(limit = 6)
//                        val result = when (type) {
//                            "CATEGORY" -> {
//                                viewModel.loadAllProducts(
//                                    limit = 6,
//                                    displayType = ProductDisplayType.ALL
//                                )
//                            }
//
//                            "DISCOUNT" -> {
//                                viewModel.loadAllProducts(
//                                    limit = 6,
//                                    displayType = ProductDisplayType.DISCOUNT
//                                )
//                            }
//
//                            "NEW_ARRIVAL" -> {
//                                viewModel.loadAllProducts(
//                                    limit = 6,
//                                    displayType = ProductDisplayType.NEW_ARRIVAL
//                                )
//                            }
//
//                            else -> {
//                                viewModel.loadAllProducts(
//                                    limit = 6,
//                                    displayType = ProductDisplayType.ALL
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        )
    }

    override fun onResume() {
        super.onResume()
//        (activity as MainActivity).showButtonNav(show = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        (activity as MainActivity).showButtonNav(show = true)
    }
}