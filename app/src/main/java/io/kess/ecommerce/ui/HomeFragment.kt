package io.kess.ecommerce.ui

import BannerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.kess.ecommerce.R
import io.kess.ecommerce.model.Category
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.ui.adapter.ProductAdapter
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import java.util.logging.Handler
import com.google.firebase.Timestamp
import io.kess.ecommerce.databinding.FragmentHomeBinding
import io.kess.ecommerce.model.User
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.CategoryAdapter
import io.kess.ecommerce.ui.adapter.HomeProductAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CategoryViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var runnable: Runnable
    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var viewModel: ProductViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var userViewModel: AuthViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var imgSlide: ViewPager2
    private lateinit var discountAdapter: HomeProductAdapter
    private lateinit var newArrivalAdapter: HomeProductAdapter
    private lateinit var allAdapter: HomeProductAdapter
    private var favorite: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
//        setUpBanner()
        observeData()
        setupRecyclerView()
        setupClickListeners(view)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
    }

    private fun setUpBanner() {
        val imageList = listOf(
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1784169538/poster4_mitlde.jpg",
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1784169523/poster3_vpccwn.jpg",
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1784169509/poster2_hw30lw.jpg",
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1784168852/poster1_r6kgnf.jpg"
        )

        binding.viewPagerBanner.adapter = BannerAdapter(imageList)

        handler.postDelayed(object : Runnable {
            override fun run() {

                val current =  binding.viewPagerBanner.currentItem
                val next = if (current == imageList.size - 1) 0 else current + 1

                binding.viewPagerBanner.setCurrentItem(next, true)
                handler.postDelayed(this, 3000)
            }
        }, 3000)

    }

    private fun setupUi(user: User?) {
        if (user != null) {
            binding.tvGreeting.text = "Hi, ${user.name}"
        } else {
            binding.tvGreeting.text = "Hi, Guest"
        }
    }

    private fun setupRecyclerView() {
        Log.d("PRODUCT_DEBUG", "setupRecyclerView called")
        discountAdapter = HomeProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, { product -> openProductDetail(product.id) })

        binding.viewDiscount.apply {
            adapter = discountAdapter
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }

        newArrivalAdapter =
            HomeProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
                favoriteViewModel.toggleFavorite(product.id)
            }, { product -> openProductDetail(product.id) })

        binding.viewNewArrival.apply {
            adapter = newArrivalAdapter
            layoutManager =
                GridLayoutManager(requireContext(), 2)
        }
        binding.viewNewArrival.isNestedScrollingEnabled = false

        allAdapter = HomeProductAdapter(emptySet(), loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, { product -> openProductDetail(product.id) })

        binding.viewAllProduct.apply {
            adapter = allAdapter
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }
        categoryAdapter = CategoryAdapter { category ->
            openProductByCategory(category)
        }
        binding.categoryRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

    }

    private fun observeData() {
        viewModel.products.observe(viewLifecycleOwner) { state ->

            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    allAdapter.submitList(state.data)
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
        }

        viewModel.discountProducts.observe(viewLifecycleOwner) { state ->

            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    discountAdapter.submitList(state.data)
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
        }
        viewModel.newArrivalProducts.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    newArrivalAdapter.submitList(state.data)
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
        }

        favoriteViewModel.favorite.observe(viewLifecycleOwner) {
            favorite = it
            discountAdapter.updateFavorites(favorite)
            newArrivalAdapter.updateFavorites(favorite)
            allAdapter.updateFavorites(favorite)
            Log.d("IN_HOME", favorite.count().toString())
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner) {
            discountAdapter.updateLoadingFavorite(it)
            newArrivalAdapter.updateLoadingFavorite(it)
            allAdapter.updateLoadingFavorite(it)
        }
        categoryViewModel.categories.observe(viewLifecycleOwner) {
            categoryAdapter.submitList(it)
            Log.d("Category", it.count().toString())
        }
        userViewModel.authState.observe(viewLifecycleOwner) { state ->

            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    setupUi(state.data)
                }

                is UiState.Error -> {

                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
        }
    }

    private fun setupClickListeners(view: View) {

        val search = view.findViewById<ImageView>(R.id.search)

        search.setOnClickListener {
            navigation(SearchFragment())
        }
        binding.discountAll.setOnClickListener {
            openProductList("DISCOUNT")
        }
        binding.newMore.setOnClickListener {
            openProductList("NEW_ARRIVAL")
        }
        binding.allSeeMore.setOnClickListener {
            openProductList("ALL")
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

    private fun openProductList(type: String) {
        val fragment = ProductListFragment().apply {
            arguments = Bundle().apply {
                putString("TYPE", type)
            }
        }
        navigation(fragment)
    }

    private fun openProductDetail(productId: String) {
        val fragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", productId)
            }
        }
        navigation(fragment)
    }

    private fun navigation(fragment: Fragment) {
        (activity as MainActivity).navigate(fragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}