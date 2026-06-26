package io.kess.ecommerce.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentCheckoutScreenBinding
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.ShopCartGroup
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.ShopOrderItemAdapter
import io.kess.ecommerce.ui.bottomSheet.AddressBottomSheet
import io.kess.ecommerce.ui.bottomSheet.SuccessPaymentSheet
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.OrderViewModel


class CheckOutFragment : Fragment() {
    private var _binding: FragmentCheckoutScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderItemAdapter: ShopOrderItemAdapter
    private var shopId: String? = null
    private var cartItem: List<CartItem> = emptyList()
    private var shopGroup: List<ShopCartGroup> = emptyList()
    private val successBottomSheet = SuccessPaymentSheet()
    private var selectedPayment = "KHQR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopId = arguments?.getString("Shop_Id")
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
        setupAdapter()
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

    private fun setupAdapter() {
        orderItemAdapter = ShopOrderItemAdapter()
        binding.recyclerView.apply {
            adapter = orderItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData(successBottomSheet: SuccessPaymentSheet) {
        cartViewModel.cartGroup.observe(viewLifecycleOwner) { cart ->
            if (shopId == null) {
                shopGroup = cart
                orderItemAdapter.submitList(cart)
                cartItem = cart.flatMap { it.items }
                setupUi(cart)
            } else {
                val selectedShop = cart.find { it.shopId == shopId }
                shopGroup = selectedShop?.let { listOf(it) } ?: emptyList()
                orderItemAdapter.submitList(shopGroup)
                setupUi(shopGroup)
            }
        }
        orderViewModel.message.observe(viewLifecycleOwner) { message ->
            message.getContentIfNotHandled()?.let { txt ->
                binding.btnCheckout.isEnabled = true
                binding.btnCheckout.alpha = 1f
//                Toast.makeText(requireContext(), txt, Toast.LENGTH_SHORT).show()
                successBottomSheet.show(parentFragmentManager, "Success_Payment")
            }
        }
        orderViewModel.actionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
//                    binding.btnCheckout.isEnabled = false
//                    binding.btnCheckout.alpha = 0.5f
                    showLoading(true)
                }
                is UiState.Success -> {
//                    binding.btnCheckout.isEnabled = true
//                    binding.btnCheckout.alpha = 1f
                    showLoading(false)
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
//                    binding.btnCheckout.isEnabled = true
//                    binding.btnCheckout.alpha = 1f
                    showLoading(false)
                }
                is UiState.Idle -> {}
            }
        }
    }
    private fun showAddressDialog() {
        val view = layoutInflater.inflate(R.layout.address_dialog, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        view.findViewById<Button>(R.id.btnAddAddress).setOnClickListener {
            dialog.dismiss()
            val sheet = AddressBottomSheet()
            sheet.arguments = Bundle().apply {
                putString("ADDRESS", binding.address.text.toString())
                putString("PHONE", binding.phoneNumber.text.toString())
            }
            sheet.show(parentFragmentManager, "AddressSheet")
        }
        dialog.show()
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


            if (binding.phoneNumber.text.toString().trim().isEmpty() ||
                binding.address.text.toString().trim().isEmpty()
            ) {
//                Toast.makeText(
//                    requireContext(),
//                    "Address cannot be empty, please add or change address",
//                    Toast.LENGTH_SHORT
//                ).show()
                showAddressDialog()
                return@setOnClickListener
            }
            val orders = shopGroup.map { group ->

                val totalPrice = group.items.sumOf { it.price * it.quantity }
                val totalQuantity = group.items.sumOf { it.quantity }

                val order = Order(
                    shopId = group.shopId,
                    shopName = group.shopName,
                    totalPrice = totalPrice,
                    totalQuantity = totalQuantity,
                    address = binding.address.text.toString(),
                    phoneNumber = binding.phoneNumber.text.toString(),
                    paymentMethod = selectedPayment,
                    createdAt = Timestamp.now()
                )

                order to group.items
            }

            orderViewModel.placeOrder(orders)
        }
    }

    private fun setupUi(items: List<ShopCartGroup>) {
        val totalQuantity = items.sumOf { group ->
            group.items.sumOf { it.quantity }
        }
        val totalPrice = items.sumOf { group ->
            group.items.sumOf { it.price * it.quantity }

        }
        binding.subTotal.text = "$${String.format("%.2f", totalPrice)}"
//        binding.totalAmount.text = "$${String.format("%.2f", totalPrice)}"
        binding.total.text = "$${String.format("%.2f", totalPrice)}"
        binding.txtTotal.text = "$${String.format("%.2f", totalPrice)}"

        binding.totalAmount.text =
            if (totalQuantity == 1) {
                "Subtotal ($totalQuantity item)"
            } else {
                "Subtotal ($totalQuantity items)"
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
    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isEnabled = !show
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