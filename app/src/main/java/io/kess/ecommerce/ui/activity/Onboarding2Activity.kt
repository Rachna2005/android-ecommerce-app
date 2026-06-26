package io.kess.ecommerce.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.kess.ecommerce.databinding.ActivityOnboardingPaymentBinding
import io.kess.ecommerce.ui.activity.LoginActivity
import io.kess.ecommerce.ui.activity.RegisterActivity

class Onboarding2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingPaymentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingPaymentBinding.inflate((layoutInflater))
        setContentView(binding.root)
        val listener = View.OnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.skip.setOnClickListener (listener)
        binding.button.setOnClickListener (listener)
        binding.signUp.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}