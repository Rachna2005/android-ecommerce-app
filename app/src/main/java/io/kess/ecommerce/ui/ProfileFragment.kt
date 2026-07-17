package io.kess.ecommerce.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProfileBinding
import io.kess.ecommerce.model.User
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.activity.Onboarding1Activity
import io.kess.ecommerce.ui.bottomSheet.AddressBottomSheet
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.OrderViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var userViewModel: AuthViewModel
    private var totalWishlist: Int = 0
    private var totalOrder: Int = 0
    private var totalCart: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        observeData()
        setupOnClickListener()
    }

    private fun initViewModel() {
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        orderViewModel.loadOrder()
    }

    private fun observeData() {
        favoriteViewModel.favorite.observe(viewLifecycleOwner) { favorite ->
            totalWishlist = favorite.count()
            updateUi()
        }
        cartViewModel.cartItems.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    totalCart = state.data.count()
                    updateUi()
                }

                is UiState.Error -> {
                    // Handle error
                }

                is UiState.Loading -> {
                    // Show loading
                }

                is UiState.Idle -> {}
            }

        }

        orderViewModel.orders.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                }

                is UiState.Success -> {
                    totalOrder = state.data.count()
                    updateUi()
                }

                is UiState.Error -> {

                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
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

    private fun updateUi() {
        binding.orders.text = totalOrder.toString()
        binding.wishlists.text = totalWishlist.toString()
        binding.cart.text = totalCart.toString()
    }

    private fun setupUi(user: User?) {
        if (user != null) {
            binding.uName.text = user.name
        } else {
            binding.uName.text = "Guest"
        }
    }

    private fun showDialog() {
        val view = layoutInflater.inflate(R.layout.logout_dialog, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            userViewModel.logout()
            startActivity(
                Intent(requireContext(), Onboarding1Activity::class.java)
            )
            requireActivity().finish()
        }
        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.75).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun setupOnClickListener() {
        binding.favorite.setOnClickListener {
            (activity as MainActivity).navigate(FragmentFavorite())
        }

        binding.btnLogout.setOnClickListener {
            showDialog()
        }
        binding.order.setOnClickListener {
            (activity as MainActivity).navigate(OrderHistoryFragment())
        }
        binding.setting.setOnClickListener {
            (activity as MainActivity).navigate(SettingFragment())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}