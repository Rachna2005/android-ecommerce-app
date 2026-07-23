package io.kess.ecommerce.ui.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.databinding.ActivityLoginScreenBinding
import io.kess.ecommerce.model.UserRole
import io.kess.ecommerce.ui.seller.ShopActivity
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.util.showSnackBar
import io.kess.ecommerce.view_model.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate((layoutInflater))
        setContentView(binding.root)
        initViewModel()
        setupClick()
        observeViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }

    private fun setupClick() {

        binding.btnLogIn.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showSnackBar(
                    findViewById(R.id.content), "All fields need to be fill",
                    backgroundColor = ContextCompat.getColor(
                        this,
                        io.kess.ecommerce.R.color.primary
                    ),
                    textColor = ContextCompat.getColor(this, io.kess.ecommerce.R.color.white)
                )
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }

                is UiState.Success -> {
                    showLoading(false)
                    showSnackBar(
                        findViewById(R.id.content),
                        "Login successfully",
                        ContextCompat.getColor(this, io.kess.ecommerce.R.color.green),
                        ContextCompat.getColor(this, android.R.color.white)
                    ) {
                        if (state.data.role == UserRole.SELLER.name) {
                            startActivity(Intent(this, ShopActivity::class.java))
                        } else {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }

                    finish()
                }

                is UiState.Error -> {
                    showLoading(false)
                    showSnackBar(
                        findViewById(R.id.content),
                        state.message,
                        ContextCompat.getColor(this, io.kess.ecommerce.R.color.red),
                        ContextCompat.getColor(this, R.color.white)
                    )
                }

                is UiState.Idle -> {}
            }
        }

        viewModel.message.observe(this) { message ->
            message.getContentIfNotHandled()?.let { txt ->
                showSnackBar(
                    findViewById(R.id.content),
                    txt,
                    ContextCompat.getColor(this, io.kess.ecommerce.R.color.red),
                    ContextCompat.getColor(this, R.color.white)
                )
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        binding.root.isEnabled = !show
    }
}