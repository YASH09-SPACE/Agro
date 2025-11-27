package com.example.agro

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.agro.data.User
import com.example.agro.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class sign_in : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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
        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // If user already signed in, skip sign-in and go to MainActivity
        auth.currentUser?.let {
            // Optionally you can also load Firestore user here if needed
            goToMain()
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

        // Auth check is always via FirebaseAuth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        setLoading(false)
                        Toast.makeText(this, "Login succeeded but UID is null", Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }

                    // After auth success, load user from Firestore (NOT Realtime DB)
                    loadUserFromFirestore(uid)
                } else {
                    setLoading(false)
                    val err = task.exception?.message ?: "Authentication failed"
                    Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Load user profile from Firestore "Users" collection.
     * This replaces any logic you previously had with Realtime Database.
     */
    private fun loadUserFromFirestore(uid: String) {
        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                setLoading(false)
                if (snapshot.exists()) {
                    // Map to your User data class (optional, but useful)
                    val user = snapshot.toObject(User::class.java)
                    // You can keep user in a singleton / ViewModel if you want

                    Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    // User authenticated but no profile in Firestore
                    Toast.makeText(
                        this,
                        "User profile not found in Firestore",
                        Toast.LENGTH_LONG
                    ).show()
                    // Optionally create a minimal profile here if you want
                    goToMain() // or stay on sign-in if you prefer
                }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(
                    this,
                    "Failed to load user profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.Edtemail.isEnabled = !isLoading
        binding.Edtpassword.isEnabled = !isLoading
        binding.signInButton.isEnabled = !isLoading
        binding.createAccount.isEnabled = !isLoading
    }
}
