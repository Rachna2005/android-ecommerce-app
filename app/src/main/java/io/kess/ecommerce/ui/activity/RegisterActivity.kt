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


}