package io.kess.ecommerce.ui.seller

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityMainBinding
import io.kess.ecommerce.databinding.ActivityShopBinding
import io.kess.ecommerce.ui.CartFragment
import io.kess.ecommerce.ui.CategoryFragment
import io.kess.ecommerce.ui.CreateShopFragment
import io.kess.ecommerce.ui.HomeFragment
import io.kess.ecommerce.ui.ProfileFragment
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.AuthViewModel
import io.kess.ecommerce.view_model.ShopViewModel

class ShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopBinding
    private val productFragment = ManageProductFragment()
    private lateinit var userViewModel: AuthViewModel
    private lateinit var shopViewModel: ShopViewModel
    private val manageProductFragment = ManageProductFragment()
    private val orderFragment = ManageOrderFragment()
    private val profileFragment = SellerProfileFragment()
    private var isProgrammaticNav = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInitialFragments()
        initViewModel()
        setupBottomNav()
        checkSellerShop()
        observeData()
    }

    private fun setupInitialFragments() {

        supportFragmentManager.beginTransaction()
            .add(R.id.container, manageProductFragment, "PRODUCT")
            .add(R.id.container, orderFragment, "ORDER")
            .hide(orderFragment)
            .add(R.id.container, profileFragment, "PROFILE")
            .hide(profileFragment)
            .commit()
    }
    private fun initViewModel(){
        userViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        shopViewModel = ViewModelProvider(this)[ShopViewModel::class.java]
        userViewModel.getUser()
    }

    private fun showFragment(fragmentToShow: Fragment) {

        val fragments = listOf(
            manageProductFragment,
            orderFragment,
            profileFragment
        )

        supportFragmentManager.beginTransaction().apply {

            fragments.forEach { fragment ->
                if (fragment == fragmentToShow) show(fragment)
                else hide(fragment)
            }
        }.commit()
    }

    private fun checkSellerShop(){
            userViewModel.authState.observe(this) { state ->
                if (state is UiState.Success) {
                    shopViewModel.getShopByOwner(state.data.id)
                }
            }
            return
    }
    private fun observeData(){
        shopViewModel.shopState.observe(this) { state ->
            when (state) {

                is UiState.Loading -> {
                    showLoading(true)
                }
                is UiState.Success -> {

                    showLoading(false)
                    showFragment(manageProductFragment)
                        showButtonNav(true)
                }
                is UiState.Error -> {

                    showLoading(false)
                    navigate(CreateShopFragment())
                    showButtonNav(false)
                    showSnackBar(
                        findViewById(android.R.id.content),
                        state.message,
                        ContextCompat.getColor(this, R.color.red),
                        ContextCompat.getColor(this, android.R.color.white)
                    )
                }
                is UiState.Idle -> {}
            }
        }
    }
    private fun setupBottomNav() {

        binding.bottomNav.setOnItemSelectedListener { item ->
            if (isProgrammaticNav) return@setOnItemSelectedListener true

            when (item.itemId) {
                R.id.nav_inventory -> goToBottomTab("PRODUCT")
                R.id.nav_order -> goToBottomTab("ORDER")
                R.id.nav_shop -> goToBottomTab("PROFILE")
            }
            true
        }
    }

    fun goToBottomTab(tab: String) {
        isProgrammaticNav = true
        supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        val target = when (tab) {
            "PRODUCT" -> manageProductFragment
            "ORDER" -> orderFragment
            "PROFILE" -> profileFragment
            else -> manageProductFragment
        }

        showFragment(target)

        // 3. Sync bottom nav UI
        val itemId = when (tab) {
            "PRODUCT" -> R.id.nav_inventory
            "ORDER" -> R.id.nav_order
            "PROFILE" -> R.id.nav_shop
            else -> R.id.nav_inventory
        }

        binding.bottomNav.selectedItemId = itemId
        isProgrammaticNav = false
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

    fun showLoading(isLoading: Boolean) {

        binding.loadingOverlay.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.bottomNav.isEnabled = !isLoading
    }

    //    fun navigation(fragment: Fragment) {
//        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
//            .addToBackStack(null).commit()
//    }
    fun showButtonNav(show: Boolean) {
        binding.bottomNav.visibility = if (show) View.VISIBLE else View.GONE
    }
}