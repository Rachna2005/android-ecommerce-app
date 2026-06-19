package io.kess.ecommerce.test

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import androidx.recyclerview.widget.RecyclerView
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityMainBinding
import io.kess.ecommerce.ui.CartFragment
import io.kess.ecommerce.ui.CategoryFragment
import io.kess.ecommerce.ui.CheckOutFragment
import io.kess.ecommerce.ui.HomeFragment
import io.kess.ecommerce.ui.ProfileFragment
import io.kess.ecommerce.ui.adapter.ProductAdapter
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.CartViewModel
import io.kess.ecommerce.view_model.CategoryViewModel

import io.kess.ecommerce.view_model.FavoriteViewModel
import io.kess.ecommerce.view_model.ProductViewModel

class HomeTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val homeFragment = HomeFragment()
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var cartViewModel: CartViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var userViewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        loadData()

        if (savedInstanceState == null) {
            replaceFragment(homeFragment)
        }

        setupBottomNav()
        observeData()
    }
    private fun initViewModel(){
        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        userViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }
    private fun loadData(){
        favoriteViewModel.loadFavorite()
        cartViewModel.loadCart()
        productViewModel.loadAllProducts()
        categoryViewModel.loadCategories()
        userViewModel.getUser()
    }
    private fun observeData(){
        cartViewModel.cartItems.observe(this) { cart ->
            val totalCount = cart.sumOf { it.quantity }
            updateCartBadge(totalCount)
        }
    }
    private fun setupBottomNav() {

        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> replaceFragment(HomeFragment())

                R.id.nav_category -> replaceFragment(CategoryFragment())

                R.id.nav_cart -> replaceFragment(CartFragment())

                R.id.nav_profile -> replaceFragment(ProfileFragment())
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

    fun updateCartBadge(count: Int){
        val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart)
        badge.backgroundColor = ContextCompat.getColor(this, R.color.primary)
        if(count > 0 ){
            badge.isVisible = true
            badge.number = count
        }
        else{
            binding.bottomNav.removeBadge(
                R.id.nav_cart
            )
        }
    }
    //    private fun showFragment(fragmentToShow: Fragment) {
//
//        val fragments = listOf(homeFragment, categoryFragment, cartFragment, profileFragment)
//
//        supportFragmentManager.beginTransaction().apply {
//
//            fragments.forEach { hide(it) }
//
//            show(fragmentToShow)
//
//        }.commit()
//    }
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