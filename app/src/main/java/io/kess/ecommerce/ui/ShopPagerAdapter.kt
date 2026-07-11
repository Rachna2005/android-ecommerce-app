package io.kess.ecommerce.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ShopPagerAdapter(fragment: Fragment, private val shopId: String) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductShopFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", shopId)
                }
            }

            1 -> ShopInfoFragment()
            else -> ProductShopFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", shopId)
                }
            }
        }
    }
}