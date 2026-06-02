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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Timestamp
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.OrderViewModel
import io.kess.ecommerce.view_model.ProductViewModel
import java.util.function.DoublePredicate


class CheckOutFragment : Fragment() {
    private lateinit var optionKhqr: LinearLayout
    private lateinit var optionCard: LinearLayout
    private lateinit var optionWallet: LinearLayout
    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel

    private lateinit var radioKhqr: RadioButton
    private lateinit var radioCard: RadioButton
    private lateinit var radioWallet: RadioButton
    private lateinit var subTotal: TextView
    private lateinit var totalAmount: TextView
    private lateinit var total: TextView
    private lateinit var totalQuantity: TextView
    private lateinit var change: TextView
    private lateinit var location: TextView
    private lateinit var phoneNumber: TextView
    private lateinit var btn: LinearLayout

    private var totalPrice: Double = 0.0
    private var quantity: Int = 1
    private var cartItem: List<CartItem> = emptyList()

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_checkout_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cartViewModel =
            ViewModelProvider(requireActivity())[CartViewModel::class.java]
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        initView(view)
        parentFragmentManager.setFragmentResultListener(
            "ADDRESS_RESULT",
            viewLifecycleOwner
        ) { _, bundle ->
            val address = bundle.getString("ADDRESS", "")
            val phone = bundle.getString("PHONE", "")
            location.text = address
            phoneNumber.text = phone
        }
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cart ->
            cartItem = cart
        }
        orderViewModel.message.observe(viewLifecycleOwner){message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        setupClickListener()
        setupUi()

    }

    private fun initView(view: View) {
        optionKhqr = view.findViewById(R.id.option_khqr)
        optionCard = view.findViewById(R.id.option_card)
        optionWallet = view.findViewById(R.id.option_wallet)

        radioKhqr = view.findViewById(R.id.radioKhqr)
        radioCard = view.findViewById(R.id.radioCard)
        radioWallet = view.findViewById(R.id.radioWallet)
        subTotal = view.findViewById(R.id.subTotal)
        totalAmount = view.findViewById(R.id.totalAmount)
        total = view.findViewById(R.id.total)
        totalQuantity = view.findViewById(R.id.quantity)
        change = view.findViewById(R.id.change)
        location = view.findViewById(R.id.address)
        phoneNumber = view.findViewById(R.id.phone_number)
        btn = view.findViewById(R.id.btnCheckout)
    }

    private fun setupClickListener() {
        change.setOnClickListener {
            val sheet = AddressBottomSheet()

            sheet.arguments = Bundle().apply {
                putString("ADDRESS", location.text.toString())
                putString("PHONE", phoneNumber.text.toString())
            }
            sheet.show(parentFragmentManager, "AddressSheet")
        }

        optionKhqr.setOnClickListener {
            selectPayment("KHQR")
            Log.d("select_payment", selectedPayment)

        }

        optionCard.setOnClickListener {
            selectPayment("CARD")
            Log.d("select_payment", selectedPayment)
        }

        optionWallet.setOnClickListener {
            selectPayment("WALLET")
            Log.d("select_payment", selectedPayment)
        }
        btn.setOnClickListener {
            val cart = cartItem

            val totalPrice = cart.sumOf {
                it.price * it.quantity
            }

            val totalItems = cart.sumOf {
                it.quantity
            }

            if (phoneNumber.text.toString().trim().isEmpty() ||
                location.text.toString().trim().isEmpty()
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
                address = location.text.toString(),
                phoneNumber = phoneNumber.text.toString(),
                paymentMethod = selectedPayment,
                createdAt = Timestamp.now()
            )

            orderViewModel.placeOrder(order, cartItem)
        }
    }

    private fun setupUi() {
        subTotal.text = "$${String.format("%.2f", totalPrice)}"
        totalAmount.text = "$${String.format("%.2f", totalPrice)}"
        total.text = "$${String.format("%.2f", totalPrice)}"
        if (quantity == 1) {
            totalAmount.text = "Subtotal (${quantity} item)"
        } else {
            totalAmount.text = "Subtotal (${quantity} items)"
        }
    }

    private fun selectPayment(type: String) {
        selectedPayment = type
        optionKhqr.setBackgroundResource(R.drawable.bg_card_white_rounded)
        optionCard.setBackgroundResource(R.drawable.bg_card_white_rounded)
        optionWallet.setBackgroundResource(R.drawable.bg_card_white_rounded)
        radioKhqr.isChecked = false
        radioCard.isChecked = false
        radioWallet.isChecked = false
        when (type) {
            "KHQR" -> {
                optionKhqr.setBackgroundResource(R.drawable.bg_card_selected_rounded)
                radioKhqr.isChecked = true
            }

            "CARD" -> {
                optionCard.setBackgroundResource(R.drawable.bg_card_selected_rounded)
                radioCard.isChecked = true
            }

            "WALLET" -> {
                optionWallet.setBackgroundResource(R.drawable.bg_card_selected_rounded)
                radioWallet.isChecked = true
            }
        }

    }
}