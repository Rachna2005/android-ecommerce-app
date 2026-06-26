package io.kess.ecommerce.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityRegisterScreenBinding
import io.kess.ecommerce.ui.MainRegisterFragment
import io.kess.ecommerce.view_model.AuthViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterScreenBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterScreenBinding.inflate((layoutInflater))
        setContentView(binding.root)
        initViewModel()
        replaceFragment(MainRegisterFragment())
//        setupClick()
//        observeViewModel()
    }
    private fun initViewModel(){
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
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

    private fun setupClick() {

//        binding.signIn.setOnClickListener {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//        binding.btnCreateAccount.setOnClickListener {
//            val name = binding.inputName.text.toString().trim()
//            val email = binding.inputEmail.text.toString().trim()
//            val password = binding.inputPass.text.toString().trim()
//            val confirmPass = binding.confirmPass.text.toString().trim()
//            if (email.isBlank() || password.isBlank() || name.isBlank() || confirmPass.isBlank()) {
//                Toast.makeText(this, "All fields need to be fill", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }
//            if (password != confirmPass) {
//                Toast.makeText(this, "Passwords Do not match", Toast.LENGTH_LONG).show()
//                return@setOnClickListener
//            }
//            viewModel.register(name, email, password)
//        }
    }
//    private fun observeViewModel() {
//        viewModel.authData.observe(this) { user ->
//            if (user != null) {
//                Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                Toast.makeText(this, "Register Fail", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}