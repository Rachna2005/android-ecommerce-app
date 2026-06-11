package io.kess.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityOnbaodingBinding
import io.kess.ecommerce.databinding.ActivityOnboardingPaymentBinding

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
            val intent = Intent(this, LoginActivity:: class.java)
            startActivity(intent)
            finish()
        }
    }
}