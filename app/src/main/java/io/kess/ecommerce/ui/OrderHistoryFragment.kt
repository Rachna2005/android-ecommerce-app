package io.kess.ecommerce.ui

import OrderAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentHomeBinding
import io.kess.ecommerce.databinding.FragmentOrderHistoryBinding
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.ui.activity.MainActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.OrderViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class OrderHistoryFragment : Fragment() {
    private var _binding: FragmentOrderHistoryBinding?  = null
    private val binding get() = _binding!!
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderAdapter: OrderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupRecyclerView()
        observeData()
        setupClickListener()
    }

    private fun initViewModel(){
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        orderViewModel.loadOrder()
    }

    private fun setupRecyclerView(){
        orderAdapter = OrderAdapter(onOrderClick = {order ->
            openOrderDetail(order.id)
        })
        binding.recyclerView.apply {
            adapter = orderAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }

    private fun observeData(){
        orderViewModel.orders.observe(viewLifecycleOwner) { state ->
//            orderAdapter.submitList(result)
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility =
                        View.VISIBLE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val result = state.data
                    binding.progressBar.visibility = View.GONE
                    orderAdapter.submitList(result)
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                is UiState.Idle -> {}
            }
        }
    }
    private fun openOrderDetail(orderId: String){
        val fragment = OrderDetailFragment().apply {
            arguments = Bundle().apply {
                putString("ID", orderId)
            }
        }
        (activity as MainActivity).navigate(fragment)
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