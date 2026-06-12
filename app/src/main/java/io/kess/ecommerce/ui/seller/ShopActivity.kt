package io.kess.ecommerce.ui.seller

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityMainBinding
import io.kess.ecommerce.databinding.ActivityShopBinding
import io.kess.ecommerce.ui.CartFragment
import io.kess.ecommerce.ui.CategoryFragment
import io.kess.ecommerce.ui.HomeFragment
import io.kess.ecommerce.ui.ProfileFragment

class ShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopBinding
    private val productFragment = ManageProductFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNav()
        if (savedInstanceState == null) {
            replaceFragment(productFragment)
        }

    }

    private fun setupBottomNav() {

        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_inventory -> replaceFragment(ManageProductFragment())

                R.id.nav_order -> replaceFragment(ManageOrderFragment())

                R.id.nav_shop -> replaceFragment(SellerProfileFragment())
            }

            true
        }
    }
    fun replaceFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
    fun navigate(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun selectBottomNav(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    //    fun navigation(fragment: Fragment) {
//        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
//            .addToBackStack(null).commit()
//    }
    fun showButtonNav(show: Boolean) {
        binding.bottomNav.visibility = if (show) View.VISIBLE else View.GONE
    }
}