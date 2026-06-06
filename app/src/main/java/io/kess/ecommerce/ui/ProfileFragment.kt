package io.kess.ecommerce.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.FragmentProfileBinding
import io.kess.ecommerce.util.UserSession
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.OrderViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var orderViewModel: OrderViewModel
    private var totalWishlist: Int = 0
    private var totalOrder: Int = 0
    private var totalCart: Int = 0
    private val binding get() = _binding!!
    val user = UserSession.currentUser

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
        setupUi()
        setupOnClickListener()
    }

    private fun initViewModel(){
        favoriteViewModel = ViewModelProvider(requireActivity())[FavoriteViewModel::class.java]
        cartViewModel = ViewModelProvider(requireActivity())[CartViewModel::class.java]

    }

    private fun observeData(){
        favoriteViewModel.favorite.observe(viewLifecycleOwner){favorite ->
            totalWishlist = favorite.count()
        }
        cartViewModel.cartItems.observe(viewLifecycleOwner){cart ->
            totalCart = cart.count()
        }
        updateUi()
    }

    private fun updateUi(){
        binding.orders.text = totalOrder.toString()
        binding.wishlists.text = totalWishlist.toString()
        binding.cart.text = totalCart.toString()
    }

    private fun setupUi(){
        if(user != null){
            binding.uName.text = user.name
        }else{
            binding.uName.text = "Guest"
        }

    }

    private fun setupOnClickListener(){
        binding.favorite.setOnClickListener {
            val fragment = ProductListFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", "FAVORITE")
                }
            }
            (activity as MainActivity).navigate(fragment)
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            UserSession.currentUser =  null
            startActivity(
                Intent(requireContext(), Onboarding1Activity::class.java)
            )
            requireActivity().finish()
        }
        binding.order.setOnClickListener {
            (activity as MainActivity).navigate(OrderHistoryFragment())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}