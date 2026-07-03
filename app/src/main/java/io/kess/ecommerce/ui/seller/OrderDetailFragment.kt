package io.kess.ecommerce.ui.seller

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentOrderDetailBinding
import io.kess.ecommerce.databinding.FragmentOrderHistoryDetailBinding
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.OrderItemAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderItemAdapter: OrderItemAdapter
    private var currentStatus: String? = null
    private var selectedStatus: String? = null
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = arguments?.getString("ID")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        openOrderDetail()
        openOrderItem()
        setupAdapter()
        setupClickListener()
        observeData()
    }

    private fun initViewModel() {
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        orderId?.let { orderViewModel.getOrderDetail(it) }
    }

    private fun observeData() {
        orderViewModel.ordersDetail.observe(viewLifecycleOwner) { state ->
//            setupUi(order)
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.content.visibility = View.GONE
                    binding.btnChangeStatus.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.content.visibility = View.VISIBLE
                    binding.btnChangeStatus.visibility = View.VISIBLE
                    setupUi(state.data)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.content.visibility = View.GONE
                    binding.btnChangeStatus.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is UiState.Idle -> {}
            }
        }
        orderViewModel.orderItem.observe(viewLifecycleOwner){state ->
            when (state) {
                is UiState.Loading -> {

                }

                is UiState.Success -> {
                    orderItemAdapter.submitList(state.data)
                }

                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Idle -> {}
            }
//            orderItemAdapter.submitList(item)
        }

        orderViewModel.actionState.observe(viewLifecycleOwner){ state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }

                is UiState.Success -> {
                    showLoading(false)
                    showSnackBar(
                        requireView(),
                        "Status Updated successfully",
                        ContextCompat.getColor(requireContext(), R.color.green),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    ){
                        val intent = Intent(requireContext(), ShopActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }

                is UiState.Error -> {
                    showLoading(false)
                    showSnackBar(
                        requireView(),
                        state.message,
                        ContextCompat.getColor(requireContext(), R.color.red),
                        ContextCompat.getColor(requireContext(), android.R.color.white)
                    )
                }
                is UiState.Idle -> {}
            }
        }
    }
    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isEnabled = !show
    }

    private fun orderStatusDialog(onSelected: (String) -> Unit) {
        val status = arrayOf("PROCESSING", "CONFIRMED", "DELIVERED")
        AlertDialog.Builder(requireContext()).setTitle("Change Status")
            .setItems(status) { _, which ->
                val selectStatus = status[which]
                onSelected(selectStatus)
            }.setNegativeButton("Cancel", null).show()
    }

    private fun setupAdapter() {
        orderItemAdapter = OrderItemAdapter()
        binding.recyclerView.apply {
            adapter = orderItemAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }

    private fun openOrderDetail() {
        orderId?.let {
            orderViewModel.getOrderDetail(it)
        }
    }

    private fun openOrderItem() {
        orderId?.let {
            orderViewModel.getOrderItem(it)
        }
    }

    private fun setupUi(order: Order) {
        currentStatus = order.status
        selectedStatus = order.status
        order.createdAt?.let {
            val date =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(it.toDate())

            binding.date.text = "Placed on ${date}"
        }
        binding.orderId.text = "#ORD-${order.id.takeLast(6)}"
        binding.txtStatus.text = order.status
        binding.phoneNumber.text = order.phoneNumber
        binding.location.text = order.address
        binding.payment.text = order.paymentMethod
        binding.subTotal.text = order.totalPrice.toString()
        binding.total.text = order.totalPrice.toString()
        binding.shop.text = order.shopName
        binding.totalItem.text =
            if (order.totalQuantity == 1) {
                "(${order.totalQuantity} item)"
            } else {
                "(${order.totalQuantity} items)"
            }
    }

    private fun setupClickListener() {
        binding.backBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.txtStatus.setOnClickListener {
            orderStatusDialog { status ->
                selectedStatus = status
                binding.txtStatus.text = status
            }
        }
        binding.btnChangeStatus.setOnClickListener {
            if (selectedStatus == null || currentStatus == null) return@setOnClickListener
            if(selectedStatus == currentStatus){
                showSnackBar(
                    requireView(),
                    "Status not changed",
                    ContextCompat.getColor(requireContext(), R.color.primary),
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
                return@setOnClickListener
            }
            orderId?.let { id ->
                orderViewModel.updateStatus(id, selectedStatus!!)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showButtonNav(show = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as MainActivity).showButtonNav(show = true)
    }

}