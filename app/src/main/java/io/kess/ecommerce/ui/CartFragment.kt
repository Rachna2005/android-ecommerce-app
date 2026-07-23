package io.kess.ecommerce.ui

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCartBinding
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ShopCartGroup
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.CartShopAdapter
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel


class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartViewModel: CartViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var cartShopAdapter: CartShopAdapter

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
        cartViewModel.cartGroup.observe(viewLifecycleOwner) { groups ->
            cartShopAdapter.submitList(groups)
            val allItems = groups.flatMap { it.items }
            updateUi(groups)
            updateCartUi(allItems)
        }
        cartViewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showSuccessSnackBar(message)
                cartViewModel.clearMessage()
            }
        }
        cartViewModel.loadingItems.observe(viewLifecycleOwner) {
            cartShopAdapter.updateLoading(it)
        }
    }

    private fun updateCartUi(cart: List<CartItem>) {
        if (cart.isEmpty()) {
            binding.layoutEmptyCart.visibility = View.VISIBLE
            binding.btnCheckoutAll.visibility = View.GONE
        } else {
            binding.layoutEmptyCart.visibility = View.GONE
            binding.btnCheckoutAll.visibility = View.VISIBLE
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

    private fun updateUi(items: List<ShopCartGroup>) {
        val totalPrice = items.sumOf { group ->
            group.items.sumOf { it.price * it.quantity }
        }
        binding.txtTotal.text = "$${String.format("%.2f", totalPrice)}"
    }

    private fun initViewModel() {
        cartViewModel =
            ViewModelProvider(requireActivity())[CartViewModel::class.java]
        favoriteViewModel =
            ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
    }

    private fun setupRecyclerView() {
        cartShopAdapter = CartShopAdapter(
            loadingItems = emptySet(),
            onProductClick = { cartItem ->
                openProductDetail(cartItem.productId)
            },

            onIncrease = { cartItem ->
                cartViewModel.increaseQuantity(cartItem.id)
            },

            onDecrease = { cartItem ->
                cartViewModel.decreaseQuantity(
                    cartItem.id,
                    cartItem.quantity
                )
            },

            onDelete = { cartItem ->
                cartViewModel.deleteCart(cartItem.id)
            },

            onCheckout = { shopGroup ->
                val fragment = CheckOutFragment().apply {
                    arguments = Bundle().apply {
                        putString("Shop_Id", shopGroup.shopId)
                    }
                }
                navigation(fragment)

            }
        )

        binding.recyclerView.apply {
            adapter = cartShopAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupClickListener() {
        binding.btnCheckout.setOnClickListener {
            navigation(CheckOutFragment())
        }
        binding.btnWishlist.setOnClickListener {
            val fragment = FragmentFavorite().apply {
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

    private fun navigation(fragment: Fragment) {
        (activity as MainActivity).navigate(fragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}