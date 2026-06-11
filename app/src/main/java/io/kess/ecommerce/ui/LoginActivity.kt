package io.kess.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityLoginScreenBinding
import io.kess.ecommerce.databinding.ActivityRegisterScreenBinding
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
    private fun initViewModel(){
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

    }
    private fun setupClick() {

        binding.btnLogIn.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPass.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields need to be fill", Toast.LENGTH_SHORT).show()
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
        viewModel.authData.observe(this) { user ->
            if (user != null) {
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login Fail", Toast.LENGTH_SHORT).show()
            }
        }
    }
}