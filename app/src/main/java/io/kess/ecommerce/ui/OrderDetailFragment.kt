package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentOrderHistoryDetailBinding
import io.kess.ecommerce.databinding.FragmentProductDetailBinding
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.ui.adapter.OrderItemAdapter
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailFragment : Fragment() {
    private var _binding: FragmentOrderHistoryDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderItemAdapter: OrderItemAdapter
    private var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = arguments?.getString("ID")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderHistoryDetailBinding.inflate(inflater, container, false)
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

    private fun initViewModel(){
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
    }
    private fun observeData(){
        orderViewModel.ordersDetail.observe(viewLifecycleOwner){state ->
//            setupUi(order)
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.content.visibility = View.GONE
                    binding.btnBuyAgain.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.content.visibility = View.VISIBLE
                    binding.btnBuyAgain.visibility = View.VISIBLE
                    setupUi(state.data)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.content.visibility = View.GONE
                    binding.btnBuyAgain.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        orderViewModel.orderItem.observe(viewLifecycleOwner){item ->
            orderItemAdapter.submitList(item)
        }
    }
    private fun setupAdapter(){
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

    private fun setupUi(order: Order){
        order.createdAt?.let {
            val date =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(it.toDate())

            binding.date.text = "Placed on ${date}"
        }
        binding.orderId.text =  "#ORD-${order.id.takeLast(6)}"
        binding.txtStatus.text = order.status
        binding.phoneNumber.text = order.phoneNumber
        binding.location.text = order.address
        binding.payment.text = order.paymentMethod
        binding.subTotal.text = order.totalPrice.toString()
        binding.total.text = order.totalPrice.toString()
    }
    private fun setupClickListener(){
        binding.backBtn.setOnClickListener { parentFragmentManager.popBackStack() }

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