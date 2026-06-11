package io.kess.ecommerce.ui

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCartBinding
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.ui.adapter.CartAdapter
import io.kess.ecommerce.ui.adapter.ColorAdapter
import io.kess.ecommerce.ui.adapter.ProductAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartViewModel: CartViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var cartAdapter: CartAdapter
    private var favorite: Set<String> = emptySet()
    private var sumPrice: Double = 0.0
    private var sumQuantity: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupRecyclerView()
        observeData()
        setupClickListener()
    }

    private fun observeData() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cart ->
            cartAdapter.submitList(cart)
            updateUi(cart)
        }
        cartViewModel.message.observe(viewLifecycleOwner){message ->
            if (message != null){
                showSuccessSnackBar( message)
                cartViewModel.clearMessage()
            }
        }
        favoriteViewModel.favorite.observe(viewLifecycleOwner) {
            favorite = it
            cartAdapter.updateFavorites(favorite)
        }
        cartViewModel.loadingItems.observe(viewLifecycleOwner){
            cartAdapter.updateLoading(it)
        }

        favoriteViewModel.loadingFavorites.observe(viewLifecycleOwner){
            cartAdapter.updateLoadingFavorite(it)
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

    private fun updateUi(cart: List<CartItem>) {
        val totalPrice =
            cart.sumOf {
                it.price * it.quantity
            }
        sumPrice = totalPrice
        val totalItems =
            cart.sumOf {
                it.quantity
            }
        sumQuantity = totalItems
        binding.num.text = "$totalItems"

        binding.subTotal.text =
            "$${String.format("%.2f", totalPrice)}"

        binding.total.text =
            "$${String.format("%.2f", totalPrice)}"

        binding.totalCheckout.text =
            "$${String.format("%.2f", totalPrice)}"
    }

    private fun initViewModel() {
        cartViewModel =
            ViewModelProvider(requireActivity())[CartViewModel::class.java]
        favoriteViewModel =
            ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(favorite, loadingItems = emptySet(), loadingFavorite = emptySet(), onFavoriteClick = { cartItem ->
            favoriteViewModel.toggleFavorite(cartItem.productId)
        }, onProductClick = { cartItem ->
            openProductDetail(cartItem.productId)
        }, onIncrease = { cartItem ->
            cartViewModel.increaseQuantity(cartItem.id)
        }, onDecrease = { cartItem ->
            cartViewModel.decreaseQuantity(cartItem.id, cartItem.quantity)
        }, onDelete = { cartItem ->
            cartViewModel.deleteCart(cartItem.id)
        }
        )
        binding.recyclerView.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListener() {
        binding.btnCheckout.setOnClickListener {
            val fragment = CheckOutFragment().apply {
                arguments = Bundle().apply {
                    putDouble("Total_price", sumPrice)
                    putInt("Total_Quantity", sumQuantity)
                }
            }
            navigation(fragment)
        }
        binding.btnWishlist.setOnClickListener {
            val fragment = ProductListFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", "FAVORITE")
                }
            }
            (activity as MainActivity).navigate(fragment)
        }
    }

    private fun openProductDetail(productId: String) {
        val fragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", productId)
            }
        }
        navigation(fragment)
    }

    private fun navigation(fragment: Fragment){
        (activity as MainActivity).navigate(fragment)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}