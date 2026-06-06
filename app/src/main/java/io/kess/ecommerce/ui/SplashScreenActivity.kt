package io.kess.ecommerce.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.R
import io.kess.ecommerce.databinding.ActivityLoginScreenBinding
import io.kess.ecommerce.databinding.ActivitySplashScreenBinding
import io.kess.ecommerce.util.UserSession
import io.kess.ecommerce.view_model.AuthViewModel

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate((layoutInflater))
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 3000)
    }

    private fun checkUserSession() {
        viewModel.authData.observe(this) { user ->
            if (user != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Log.d("User Status", "User = $user")
                finish()
            } else {
                val intent = Intent(this, Onboarding1Activity::class.java)
                startActivity(intent)
                Log.d("User Status", "No User")
                finish()
            }
        }
        viewModel.checkUserSession()
    }
}