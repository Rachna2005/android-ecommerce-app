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
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.FavoriteViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var runnable: Runnable
    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var viewModel: ProductViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var userViewModel: AuthViewModel
    private lateinit var imgSlide: ViewPager2
    private lateinit var discountAdapter: ProductAdapter
    private lateinit var newArrivalAdapter: ProductAdapter
    private lateinit var allAdapter: ProductAdapter
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
    }

    private fun setUpBanner() {
        val imageList = listOf(
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1779176445/banner3_splvfi.png",
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1779176445/banner3_splvfi.png",
            "https://res.cloudinary.com/dcao8vmuc/image/upload/v1779176444/banner2_kwjqie.png"
        )

        imgSlide.adapter = BannerAdapter(imageList)

        handler.postDelayed(object : Runnable {
            override fun run() {

                val current = imgSlide.currentItem
                val next = if (current == imageList.size - 1) 0 else current + 1

                imgSlide.currentItem = next

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
        discountAdapter = ProductAdapter(emptySet(), loadingFavorite = emptySet(),  { product ->
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

        newArrivalAdapter = ProductAdapter(emptySet(),loadingFavorite = emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, { product -> openProductDetail(product.id) })

        binding.viewNewArrival.apply {
            adapter = newArrivalAdapter
            layoutManager =
                GridLayoutManager(requireContext(), 2)
        }
        binding.viewNewArrival.isNestedScrollingEnabled = false

        allAdapter = ProductAdapter(emptySet(),loadingFavorite = emptySet(), { product ->
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

    }

    private fun observeData() {
        viewModel.products.observe(viewLifecycleOwner) { state ->

//            Log.d("PRODUCT_DEBUG", "Observer triggered")
//            val discountList = products.filter { (it.discountPercentage ?: 0.0) > 0 }.take(5)
//            val newArrivalList =
//                products
//                    .sortedByDescending { it.createdAt?.seconds ?: 0 }
//                    .take(4)
//            discountAdapter.submitList(discountList)
//            newArrivalAdapter.submitList(newArrivalList)
//            allAdapter.submitList(products)

            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility =
                        View.VISIBLE
                    binding.content.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
//                    binding.contentLayout.visibility = View.VISIBLE
                    binding.content.visibility = View.VISIBLE
                    val products = state.data

                    val discountList =
                        products.filter { (it.discountPercentage ?: 0.0) > 0 }.take(5)
                    val newArrivalList =
                        products
                            .sortedByDescending { it.createdAt?.seconds ?: 0 }
                            .take(4)
                    discountAdapter.submitList(discountList)
                    newArrivalAdapter.submitList(newArrivalList)
                    allAdapter.submitList(products)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE

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
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner){
            discountAdapter.updateLoadingFavorite(it)
            newArrivalAdapter.updateLoadingFavorite(it)
            allAdapter.updateLoadingFavorite(it)
        }
//        userViewModel.authData.observe(viewLifecycleOwner) {
//            setupUi(it)
//        }
    }

    private fun setupClickListeners(view: View) {

        val search = view.findViewById<ImageView>(R.id.search)

        search.setOnClickListener {
            navigation(SearchFragment())
        }

        val discountMore = view.findViewById<TextView>(R.id.discount_all)
        discountMore.setOnClickListener {
            openProductList("DISCOUNT")
        }
        val newMore = view.findViewById<TextView>(R.id.new_more)
        newMore.setOnClickListener {
            openProductList("NEW_ARRIVAL")
        }
        val allMore = view.findViewById<TextView>(R.id.all_seeMore)
        allMore.setOnClickListener {
            openProductList("ALL")
        }
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