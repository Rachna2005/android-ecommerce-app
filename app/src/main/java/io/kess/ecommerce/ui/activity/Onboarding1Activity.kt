package io.kess.ecommerce.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.kess.ecommerce.databinding.ActivityOnbaodingBinding
import io.kess.ecommerce.ui.RegisterActivity

class Onboarding1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityOnbaodingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ONBOARDING", "onCreate called")
        binding = ActivityOnbaodingBinding.inflate((layoutInflater))
        setContentView(binding.root)
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