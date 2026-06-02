package io.kess.ecommerce.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityMainBinding
import io.kess.ecommerce.view_model.FavoriteViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val homeFragment = HomeFragment()
    private val categoryFragment = CategoryFragment()
    private val cartFragment = CartFragment()
    private val profileFragment = ProfileFragment()
    private lateinit var favoriteViewModel: FavoriteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        favoriteViewModel.loadFavorite()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().apply {
            add(binding.container.id, homeFragment)
            add(binding.container.id, categoryFragment).hide(categoryFragment)
            add(binding.container.id, cartFragment).hide(cartFragment)
            add(binding.container.id, profileFragment).hide(profileFragment)
        }.commit()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> switchFragment(homeFragment)

                R.id.nav_category -> switchFragment(categoryFragment)

                R.id.nav_cart -> switchFragment(cartFragment)

                R.id.nav_profile -> switchFragment(profileFragment)
            }
            true
        }
    }

    fun switchFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        val fragments = listOfNotNull(
            homeFragment,
            categoryFragment,
            cartFragment,
            profileFragment
        )
        for (f in fragments){
            transaction.hide(f)
        }
        if(!fragment.isAdded){
            transaction.add(binding.container.id, fragment)
        }else{
            transaction.show(fragment)
        }
        transaction.commit()
    }

    fun navigation(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
            .addToBackStack(null).commit()
    }
    fun showButtonNav(show: Boolean) {
        binding.bottomNav.visibility = if (show) View.VISIBLE else View.GONE
    }
}