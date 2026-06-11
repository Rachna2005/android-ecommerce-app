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
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductVariant
import io.kess.ecommerce.ui.adapter.ColorAdapter
import io.kess.ecommerce.ui.adapter.ReviewAdapter
import io.kess.ecommerce.ui.adapter.SizeAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import io.kess.ecommerce.view_model.ReviewViewModel

class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private var productId: String? = null
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var viewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var reviewViewModel: ReviewViewModel
    private var productDetail: ProductDetail = ProductDetail()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var sizeAdapter: SizeAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private var selectedColor: String? = null
    private var selectedSize: String? = null
    private var totalQuantity: Int = 1
    private var favoriteSet: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productId = arguments?.getString("ID")
//        productId = requireArguments().getString("ID")
//            ?: throw IllegalArgumentException("Product ID required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        favoriteViewModel =
            ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        cartViewModel =
            ViewModelProvider(requireActivity())[CartViewModel::class.java]
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

    private fun setupFavorite() {
        if (favoriteSet.contains(productId)) {
            binding.btnWishlist.setImageResource(R.drawable.ic_heart_fill)
        } else {
            binding.btnWishlist.setImageResource(R.drawable.ic_heart)
        }
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

        binding.increase.setOnClickListener {
            totalQuantity++
            updateQuantityUI()
        }

        binding.decrease.setOnClickListener {
            if (totalQuantity > 1) {
                totalQuantity--
                updateQuantityUI()
            }
        }
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.addToCart.setOnClickListener {
            val discount =
                productDetail.product.price *
                        ((productDetail.product.discountPercentage ?: 0.0) / 100)

            val finalPrice = productDetail.product.price - discount

            val selectedVariant = if (productDetail.variant.isNotEmpty()) {
                productDetail.variant.firstOrNull {
                    it.color == selectedColor && it.size == selectedSize
                }
            } else {
                null
            }
            if (selectedVariant == null && productDetail.variant.isNotEmpty()) {
                Toast.makeText(requireContext(), "Please Select color and size", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val cart = CartItem(
                productId = productDetail.product.id,
                variantId = selectedVariant?.id ?: "",
                name = productDetail.product.name,
                quantity = totalQuantity,
                image = productDetail.product.image,
                selectorColor = selectedVariant?.color ?: "",
                selectSize = selectedVariant?.size ?: "",
                price = finalPrice
            )
            cartViewModel.addToCart(cart)
        }

        binding.btnWishlist.setOnClickListener {
            productId?.let {
                favoriteViewModel.toggleFavorite(it)
            }
        }

        binding.btnCart.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (activity as MainActivity).selectBottomNav(R.id.nav_cart)
        }

        binding.writeReview.setOnClickListener {
            val fragment = ReviewFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", productId)
                    putString("Name", productDetail.product.name)
                    putString("Image", productDetail.product.image)
                }
            }
            (activity as MainActivity).navigate(fragment)
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
            }
        }
        cartViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null){
                showSuccessSnackBar( message)
                cartViewModel.clearMessage()
            }
        }
        favoriteViewModel.favorite.observe(viewLifecycleOwner) { favorite ->
            favoriteSet = favorite
            setupFavorite()
        }

        reviewViewModel.reviews.observe(viewLifecycleOwner) { review ->
            reviewAdapter.submitList(review)
        }
        cartViewModel.isAddingToCart.observe(viewLifecycleOwner){isLoading ->
            binding.addToCart.isEnabled = !isLoading
            binding.addToCart.alpha = if (isLoading) 0.5f else 1f
        }
        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner){favorite ->
            val favoriteLoading = favorite.contains(productId)
            binding.wishlistProgress.visibility = if(favoriteLoading) View.VISIBLE else View.GONE
            binding.btnWishlist.visibility =
                if (favoriteLoading) View.INVISIBLE else View.VISIBLE
        }
    }
    fun showSuccessSnackBar(message: String) {
        val snackbar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )

        snackbar.setBackgroundTint(
            ContextCompat.getColor(requireContext(), R.color.green)
        )

        snackbar.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )

        val snackbarView = snackbar.view

        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.setMargins(16, 100, 16, 0)

        snackbarView.layoutParams = params

        snackbar.show()
    }

    private fun updateQuantityUI() {
        binding.quantity.text = totalQuantity.toString()
        binding.decrease.isEnabled = totalQuantity > 1
        binding.decrease.alpha = if (totalQuantity > 1) 1f else 0.5f
    }

    private fun setupUi(product: ProductDetail) {

        binding.mainProductTitle.text = product.product.name
        binding.text.text = product.product.name
        binding.descriptionContent.text = product.product.description
        updateQuantityUI()
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
        (activity as MainActivity).showButtonNav(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).showButtonNav(true)
    }
}