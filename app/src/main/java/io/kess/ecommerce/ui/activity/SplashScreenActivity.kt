package io.kess.ecommerce.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.databinding.ActivitySplashScreenBinding
import io.kess.ecommerce.model.UserRole
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.AuthViewModel

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate((layoutInflater))
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

//        Handler(Looper.getMainLooper()).postDelayed({
//            checkUserSession()
//        }, 3000)

        observeData()
        viewModel.getUser()
    }

    private fun observeData() {

        viewModel.authState.observe(this) { state ->

            when (state) {

                is UiState.Loading -> {
                    //Loading
                }
                is UiState.Success -> {

                    when (state.data.role) {

                        UserRole.CUSTOMER.name -> {
                            startActivity(
                                Intent(this, MainActivity::class.java)
                            )
                            finish()
                        }

                        UserRole.SELLER.name -> {
                            startActivity(
                                Intent(this, ShopActivity::class.java)
                            )
                            finish()
                        }

                        else -> {
                            startActivity(
                                Intent(this, Onboarding1Activity::class.java)
                            )
                            finish()
                        }
                    }
                }
                is UiState.Error -> {
                    startActivity(
                        Intent(this, Onboarding1Activity::class.java)
                    )
                    finish()
                }
            }
        }
    }
}