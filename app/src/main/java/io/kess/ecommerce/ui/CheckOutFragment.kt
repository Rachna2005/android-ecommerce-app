package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton

import androidx.compose.remote.creation.dsl.log
import io.kess.ecommerce.R
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import io.kess.ecommerce.databinding.FragmentCheckoutScreenBinding
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.OrderItemAdapter
import io.kess.ecommerce.ui.bottomSheet.AddressBottomSheet
import io.kess.ecommerce.ui.bottomSheet.SuccessPaymentSheet
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.OrderViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import java.util.function.DoublePredicate


class CheckOutFragment : Fragment() {
    private var _binding: FragmentCheckoutScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderItemAdapter: OrderItemAdapter
    private var totalPrice: Double = 0.0
    private var quantity: Int = 1
    private var cartItem: List<CartItem> = emptyList()
    private val successBottomSheet = SuccessPaymentSheet()
    private var selectedPayment = "KHQR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        totalPrice = arguments?.getDouble("Total_price") ?: 0.0
        quantity = arguments?.getInt("Total_Quantity") ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCheckoutScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupBottomSheet()
        observeData(successBottomSheet)
        setupClickListener()
        setupUi()

    }

    private fun initViewModel() {
        cartViewModel =
            ViewModelProvider(requireActivity())[CartViewModel::class.java]
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
    }

    private fun setupBottomSheet() {
        parentFragmentManager.setFragmentResultListener(
            "ADDRESS_RESULT",
            viewLifecycleOwner
        ) { _, bundle ->
            val address = bundle.getString("ADDRESS", "")
            val phone = bundle.getString("PHONE", "")
            binding.address.text = address
            binding.phoneNumber.text = phone
        }

        successBottomSheet.onGoOrderHistory = {
            parentFragmentManager.popBackStack()
            (activity as MainActivity).navigate(OrderHistoryFragment())
        }
        successBottomSheet.onGoHome = {
            parentFragmentManager.popBackStack()
            (activity as MainActivity).apply {
                selectBottomNav(R.id.nav_home)
            }
        }
    }
//    private fun setupAdapter(){
//        orderItemAdapter = OrderItemAdapter()
//        binding.recyclerView.apply {
//            adapter = orderItemAdapter
//            layoutManager = LinearLayoutManager(requireContext())
//        }
//    }

    private fun observeData(successBottomSheet: SuccessPaymentSheet) {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cart ->
            cartItem = cart
//            orderItemAdapter.submitList(cartItem)
        }

        orderViewModel.message.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            successBottomSheet.show(parentFragmentManager, "Success_Payment")
        }
        orderViewModel.isOrder.observe(viewLifecycleOwner) { isOrder ->
            binding.btnCheckout.isEnabled = !isOrder
            binding.btnCheckout.alpha = if (isOrder) 0.5f else 1f
        }
    }

    private fun setupClickListener() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.change.setOnClickListener {
            val sheet = AddressBottomSheet()

            sheet.arguments = Bundle().apply {
                putString("ADDRESS", binding.address.text.toString())
                putString("PHONE", binding.phoneNumber.text.toString())
            }
            sheet.show(parentFragmentManager, "AddressSheet")
        }

        binding.optionKhqr.setOnClickListener {
            selectPayment("KHQR")
            Log.d("select_payment", selectedPayment)

        }

        binding.optionCard.setOnClickListener {
            selectPayment("CARD")
            Log.d("select_payment", selectedPayment)
        }

        binding.optionWallet.setOnClickListener {
            selectPayment("WALLET")
            Log.d("select_payment", selectedPayment)
        }
        binding.btnCheckout.setOnClickListener {
            val cart = cartItem

            val totalPrice = cart.sumOf {
                it.price * it.quantity
            }

            val totalItems = cart.sumOf {
                it.quantity
            }

            if (binding.phoneNumber.text.toString().trim().isEmpty() ||
                binding.address.text.toString().trim().isEmpty()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Address cannot be empty, please add or change address",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val order = Order(
                totalPrice = totalPrice,
                totalQuantity = totalItems,
                address = binding.address.text.toString(),
                phoneNumber = binding.phoneNumber.text.toString(),
                paymentMethod = selectedPayment,
                createdAt = Timestamp.now()
            )
            orderViewModel.placeOrder(order, cartItem)
        }
    }

    private fun setupUi() {
        binding.subTotal.text = "$${String.format("%.2f", totalPrice)}"
        binding.totalAmount.text = "$${String.format("%.2f", totalPrice)}"
        binding.total.text = "$${String.format("%.2f", totalPrice)}"
        binding.txtTotal.text = "$${String.format("%.2f", totalPrice)}"
        if (quantity == 1) {
            binding.totalAmount.text = "Subtotal (${quantity} item)"
        } else {
            binding.totalAmount.text = "Subtotal (${quantity} items)"
        }
    }

    private fun selectPayment(type: String) {
        selectedPayment = type
        binding.optionKhqr.setBackgroundResource(R.drawable.bg_card_white_rounded)
        binding.optionCard.setBackgroundResource(R.drawable.bg_card_white_rounded)
        binding.optionWallet.setBackgroundResource(R.drawable.bg_card_white_rounded)
        binding.radioKhqr.isChecked = false
        binding.radioCard.isChecked = false
        binding.radioWallet.isChecked = false
        when (type) {
            "KHQR" -> {
                binding.optionKhqr.setBackgroundResource(R.drawable.bg_card_selected_rounded)
                binding.radioKhqr.isChecked = true
            }

            "CARD" -> {
                binding.optionCard.setBackgroundResource(R.drawable.bg_card_selected_rounded)
                binding.radioCard.isChecked = true
            }

            "WALLET" -> {
                binding.optionWallet.setBackgroundResource(R.drawable.bg_card_selected_rounded)
                binding.radioWallet.isChecked = true
            }
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showButtonNav(show = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showButtonNav(show = true)
        _binding = null
    }
}