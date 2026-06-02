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
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import io.kess.ecommerce.util.UserSession
import io.kess.ecommerce.view_model.FavoriteViewModel

class HomeFragment : Fragment() {
    private lateinit var runnable: Runnable
    private val handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var viewModel: ProductViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var recyclerViewDiscount: RecyclerView
    private lateinit var recyclerViewNewArrival: RecyclerView
    private lateinit var recyclerViewAll: RecyclerView
    private lateinit var imgSlide: ViewPager2
    private lateinit var discountAdapter: ProductAdapter
    private lateinit var newArrivalAdapter: ProductAdapter
    private lateinit var allAdapter: ProductAdapter
    private var productList: List<Product> = emptyList()
    private var favorite: Set<String> = emptySet()
    val user = UserSession.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        viewModel.loadAllProducts()
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]

        val userName = view.findViewById<TextView>(R.id.tvGreeting)
        if(user != null){
            userName.text = "Hi, ${user.name}"
        }else{
            userName.text = "Guest"
        }
        initView(view)
//        setUpBanner()
        setupRecyclerView()
        setupClickListeners(view)
    }

    private fun initView(view: View) {
        recyclerViewDiscount = view.findViewById(R.id.view_discount)
        recyclerViewNewArrival = view.findViewById(R.id.view_new_arrival)
        recyclerViewAll = view.findViewById(R.id.view_all_product)

        imgSlide = view.findViewById(R.id.viewPagerBanner)
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

    private fun setupRecyclerView() {
        Log.d("PRODUCT_DEBUG", "setupRecyclerView called")
        discountAdapter = ProductAdapter(emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, {product -> openProductDetail(product.id)})

        recyclerViewDiscount.adapter = discountAdapter
        recyclerViewDiscount.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        newArrivalAdapter = ProductAdapter(emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, {product -> openProductDetail(product.id)})

        recyclerViewNewArrival.adapter = newArrivalAdapter
        recyclerViewNewArrival.layoutManager =
            GridLayoutManager(requireContext(), 2)
        recyclerViewNewArrival.isNestedScrollingEnabled = false

        allAdapter = ProductAdapter(emptySet(), { product ->
            favoriteViewModel.toggleFavorite(product.id)
        }, {product -> openProductDetail(product.id)})

        recyclerViewAll.adapter = allAdapter
        recyclerViewAll.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        viewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("PRODUCT_DEBUG", "Observer triggered")
            val discountList = products.filter { (it.discountPercentage ?: 0.0) > 0 }.take(5)
            val newArrivalList =
                products
                    .sortedByDescending { it.createdAt?.seconds ?: 0 }
                    .take(4)
            discountAdapter.submitList(discountList)
            newArrivalAdapter.submitList(newArrivalList)
            allAdapter.submitList(products)
        }

        favoriteViewModel.favorite.observe(viewLifecycleOwner) {
            favorite = it
            discountAdapter.updateFavorites(favorite)
            newArrivalAdapter.updateFavorites(favorite)
            allAdapter.updateFavorites(favorite)
            Log.d("IN_HOME", favorite.count().toString())
        }
    }

    private fun setupClickListeners(view: View) {

        val search = view.findViewById<ImageView>(R.id.search)

        search.setOnClickListener {
            (activity as MainActivity).navigation(SearchFragment())
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
        (activity as MainActivity).navigation(fragment)
    }

    private fun openProductDetail(productId: String){
        val fragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", productId)
            }
        }
        (activity as MainActivity).navigation(fragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        handler.removeCallbacksAndMessages(null)
    }
}