package io.kess.ecommerce.ui

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.Card
import androidx.compose.remote.creation.dsl.log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProductDetailBinding
import io.kess.ecommerce.databinding.FragmentSellerProductDetailBinding
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductVariant
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.ColorAdapter
import io.kess.ecommerce.ui.adapter.ReviewAdapter
import io.kess.ecommerce.ui.adapter.SizeAdapter
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import io.kess.ecommerce.view_model.ReviewViewModel

class SellerProductDetailFragment : Fragment() {
    private var _binding: FragmentSellerProductDetailBinding? = null
    private val binding get() = _binding!!
    private var productId: String? = null
    private lateinit var viewModel: ProductViewModel
    private lateinit var reviewViewModel: ReviewViewModel
    private var productDetail: ProductDetail = ProductDetail()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var sizeAdapter: SizeAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private var selectedColor: String? = null
    private var selectedSize: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productId = arguments?.getString("ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupAdapters()
        setupRecyclerViews()
        setupButtonClick()
        openProductDetail()
        observeData()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        productId?.let {
            reviewViewModel.loadReviews(it)
        }
    }
    private fun setupAdapters() {

        colorAdapter = ColorAdapter { variant ->

            selectedColor = variant.color

            updateVariants()
        }
        sizeAdapter = SizeAdapter { variant ->

            selectedSize = variant.size

        }

        reviewAdapter = ReviewAdapter()

    }

    private fun openProductDetail() {
        productId?.let {
            viewModel.getProductDetail(it)
        }
    }

    private fun setupRecyclerViews() {
        binding.recyclerColor.apply {
            adapter = colorAdapter
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }

        binding.recyclerSize.apply {
            adapter = sizeAdapter
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }

        binding.review.apply {
            adapter = reviewAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }

    private fun setupButtonClick() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnCart.setOnClickListener {
            (requireActivity() as ShopActivity).goToBottomTab("PROFILE")
        }
    }

    private fun observeData() {

        viewModel.productDetail.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.mainContent.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.mainContent.visibility = View.VISIBLE
                    val product = state.data
                    productDetail = product
                    setupUi(product)
                    updateVariants()
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.mainContent.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is UiState.Idle -> {}
            }
        }
        reviewViewModel.reviews.observe(viewLifecycleOwner) { review ->
//            reviewAdapter.submitList(review)
        }
    }
    private fun setupUi(product: ProductDetail) {

        binding.mainProductTitle.text = product.product.name
        binding.text.text = product.product.name
        binding.descriptionContent.text = product.product.description

        if (product.variant.isEmpty()) {
            binding.color.visibility = View.GONE
            binding.caseSize.visibility = View.GONE
        } else {
            binding.color.visibility = View.VISIBLE
            binding.caseSize.visibility = View.VISIBLE
        }

        Glide.with(requireContext())
            .load(product.product.image)
            .into(binding.image)

        val discount =
            product.product.price *
                    ((product.product.discountPercentage ?: 0.0) / 100)

        val finalPrice = product.product.price - discount

        if (
            product.product.discountPercentage == null ||
            product.product.discountPercentage == 0.0
        ) {

            binding.price.text =
                "$${String.format("%.2f", product.product.price)}"

            binding.txtOldPrice.visibility = View.GONE

        } else {

            binding.price.text =
                "$${String.format("%.2f", finalPrice)}"

            binding.txtOldPrice.visibility = View.VISIBLE

            binding.txtOldPrice.text =
                "$${String.format("%.2f", product.product.price)}"

            binding.txtOldPrice.paintFlags =
                binding.txtOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        colorAdapter.submitList(product.variant)
    }

    private fun updateVariants() {
        val allVariants = productDetail.variant
        sizeAdapter.setVariant(allVariants)
        sizeAdapter.setSelectedColor(selectedColor)
    }

    override fun onResume() {
        super.onResume()
        (activity as ShopActivity).showButtonNav(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as ShopActivity).showButtonNav(true)
    }
}