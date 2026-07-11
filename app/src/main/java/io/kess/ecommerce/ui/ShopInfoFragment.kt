package io.kess.ecommerce.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.kess.ecommerce.databinding.FragmentShopInfoBinding
import io.kess.ecommerce.ui.activity.MainActivity

class ShopInfoFragment : Fragment() {
        private var _binding: FragmentShopInfoBinding? = null
        private val binding get() = _binding!!
        private var shopId: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            shopId = arguments?.getString("ID")

        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentShopInfoBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            initViewModel()

        }

        private fun initViewModel() {

        }

        override fun onResume() {
            super.onResume()
//            (activity as MainActivity).showButtonNav(false)
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
//            (activity as MainActivity).showButtonNav(true)
        }
}