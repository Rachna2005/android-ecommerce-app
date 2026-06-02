package io.kess.ecommerce.ui

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.material3.Card
import androidx.compose.remote.creation.dsl.log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import io.kess.ecommerce.R
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductVariant
import io.kess.ecommerce.ui.adapter.ColorAdapter
import io.kess.ecommerce.ui.adapter.SizeAdapter
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class ProductDetailFragment : Fragment() {

    private var productId: String? = null

    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var viewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private var productDetail: ProductDetail = ProductDetail()

    private lateinit var colorAdapter: ColorAdapter
    private lateinit var sizeAdapter: SizeAdapter
    private var selectedColor: String? = null
    private var selectedSize: String? = null

    // views
    private lateinit var title: TextView
    private lateinit var name: TextView
    private lateinit var price: TextView
    private lateinit var oldPrice: TextView
    private lateinit var description: TextView
    private lateinit var image: ImageView
    private lateinit var recyclerColor: RecyclerView
    private lateinit var recyclerSize: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productId = arguments?.getString("ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ProductViewModel::class.java]
        favoriteViewModel =
            ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        cartViewModel =
            ViewModelProvider(requireActivity())[CartViewModel::class.java]
        cartViewModel.message.observe(viewLifecycleOwner){ message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

        }
        initViews(view)

        setupAdapters()

        setupRecyclerViews()

        setupButtonClick(view)

        productId?.let {
            viewModel.getProductDetail(it)
        }

        observeProduct()
    }
    private fun initViews(view: View) {

        title = view.findViewById(R.id.mainProductTitle)
        name = view.findViewById(R.id.text)
        price = view.findViewById(R.id.price)
        oldPrice = view.findViewById(R.id.txtOldPrice)
        description = view.findViewById(R.id.descriptionContent)
        image = view.findViewById(R.id.image)

        recyclerColor = view.findViewById(R.id.recyclerColor)
        recyclerSize = view.findViewById(R.id.recyclerSize)
    }

    private fun setupAdapters() {

        colorAdapter = ColorAdapter { variant ->

            selectedColor = variant.color

            Log.d("COLOR", "Selected Color: ${variant.color}")

            updateVariants()
        }
        sizeAdapter = SizeAdapter { variant ->

            selectedSize = variant.size

            Log.d("SIZE", "Selected Size: ${variant.size}")
        }
    }

    private fun setupRecyclerViews() {

        recyclerColor.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        recyclerColor.adapter = colorAdapter

        recyclerSize.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        recyclerSize.adapter = sizeAdapter
    }

    private fun setupButtonClick(view: View) {

        val back = view.findViewById<ImageView>(R.id.btnBack)

        back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val addToCart = view.findViewById<MaterialButton>(R.id.addToCart)
        addToCart.setOnClickListener {
            val discount =
                productDetail.product.price *
                        ((productDetail.product.discountPercentage ?: 0.0) / 100)

            val finalPrice = productDetail.product.price - discount

            val selectedVariant = productDetail.variant.firstOrNull {
                it.color == selectedColor && it.size == selectedSize
            }
            if(selectedVariant == null){
                Toast.makeText(requireContext(), "Please Select color and size", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cart = CartItem(
                productId = productDetail.product.id,
                variantId = selectedVariant.id,
                name = productDetail.product.name,
                quantity = 1,
                image = productDetail.product.image,
                selectorColor = selectedVariant.color,
                selectSize = selectedVariant.size,
                price = finalPrice
            )
            cartViewModel.addToCart(cart)
        }
    }

    private fun observeProduct() {

        viewModel.productDetail.observe(viewLifecycleOwner) { product ->

            productDetail = product
            setupUi(product)
            updateVariants()
        }
    }

    private fun setupUi(product: ProductDetail) {

        title.text = product.product.name
        name.text = product.product.name
        description.text = product.product.description

        Glide.with(requireContext())
            .load(product.product.image)
            .into(image)

        val discount =
            product.product.price *
                    ((product.product.discountPercentage ?: 0.0) / 100)

        val finalPrice = product.product.price - discount

        if (
            product.product.discountPercentage == null ||
            product.product.discountPercentage == 0.0
        ) {

            price.text =
                "$${String.format("%.2f", product.product.price)}"

            oldPrice.visibility = View.GONE

        } else {

            price.text =
                "$${String.format("%.2f", finalPrice)}"

            oldPrice.visibility = View.VISIBLE

            oldPrice.text =
                "$${String.format("%.2f", product.product.price)}"

            oldPrice.paintFlags =
                oldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        colorAdapter.submitList(product.variant)
    }

    private fun updateVariants() {

        val allVariants = productDetail.variant

        if(selectedColor == null){
            val allSizes = allVariants.distinctBy { it.size }
            sizeAdapter.submitList(allSizes)
            return
        }

        val availableSizes = allVariants.filter { it.color == selectedColor }.distinctBy { it.size }
        sizeAdapter.submitList(availableSizes)
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).showButtonNav(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as MainActivity).showButtonNav(true)
    }
}