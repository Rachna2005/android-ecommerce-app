package io.kess.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityLoginScreenBinding
import io.kess.ecommerce.databinding.ActivityOnbaodingBinding

class Onboarding1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityOnbaodingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnbaodingBinding.inflate((layoutInflater))
        binding.button.setOnClickListener {
            val intent = Intent(this, Onboarding2Activity::class.java)
            startActivity(intent)
            finish()
        }
        binding.skip.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}