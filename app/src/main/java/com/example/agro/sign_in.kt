package com.example.agro

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.agro.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class sign_in : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signInButton.setOnClickListener {
            attemptSignIn()
        }

        binding.createAccount.setOnClickListener {
            startActivity(Intent(this, sign_up::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // If user already signed in, skip sign-in and go to MainActivity
        auth.currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun attemptSignIn() {
        val email = binding.Edtemail.text.toString().trim()
        val password = binding.Edtpassword.text.toString()

        // Validations
        if (email.isEmpty()) {
            binding.Edtemail.error = "Email required"
            binding.Edtemail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.Edtemail.error = "Enter a valid email"
            binding.Edtemail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.Edtpassword.error = "Password required"
            binding.Edtpassword.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.Edtpassword.error = "Password must be at least 6 characters"
            binding.Edtpassword.requestFocus()
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
                    // go to MainActivity and clear backstack
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    // show readable error
                    val err = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        // optionally disable inputs while loading
        binding.Edtemail.isEnabled = !isLoading
        binding.Edtpassword.isEnabled = !isLoading
        binding.signInButton.isEnabled = !isLoading
        binding.createAccount.isEnabled = !isLoading
    }
}
