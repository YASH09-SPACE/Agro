package com.example.agro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.agro.data.User
import com.example.agro.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class sign_up : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_GOOGLE_SIGN_IN = 9001
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Configure Google Sign In (request ID token)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // must be in strings.xml
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signUpButton.setOnClickListener {
            registerUser()
        }

        binding.googlesignUpButton.setOnClickListener {
            startGoogleSignIn()
        }

        binding.signin.setOnClickListener {
            startActivity(Intent(this, sign_in::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // If already signed in, go to MainActivity so sign_in isn't shown again
        auth.currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()

        }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // validations
        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name required"
            binding.etFullName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email required"
            binding.etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email"
            binding.etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password required"
            binding.etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // show progress overlay
        setLoading(true)
        binding.signUpButton.isEnabled = false
        binding.googlesignUpButton.isEnabled = false

        // create Firebase auth user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        setLoading(false)
                        binding.signUpButton.isEnabled = true
                        binding.googlesignUpButton.isEnabled = true
                        Toast.makeText(this, "Registration failed (missing UID)", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    val passwordHash = sha256(password)
                    val user = User(
                        fullName = fullName,
                        email = email,
                        password = passwordHash,
                        provider = "password",
                        createdAt = System.currentTimeMillis()
                    )

                    // Save to Firestore instead of Realtime Database
                    val db = FirebaseFirestore.getInstance()
                    db.collection("Users")
                        .document(uid)
                        .set(user)
                        .addOnCompleteListener { dbTask ->
                            setLoading(false)
                            binding.signUpButton.isEnabled = true
                            binding.googlesignUpButton.isEnabled = true
                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                clearFields()
                                // go to MainActivity
                                startActivity(Intent(this, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                                finish()
                            } else {
                                val err = dbTask.exception?.message ?: "Failed to save user info"
                                Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    setLoading(false)
                    binding.signUpButton.isEnabled = true
                    binding.googlesignUpButton.isEnabled = true
                    val err = authTask.exception?.message ?: "Registration failed"
                    Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                }
            }
    }

    // Start Google sign-in flow
    private fun startGoogleSignIn() {
        setLoading(true)
        binding.signUpButton.isEnabled = false
        binding.googlesignUpButton.isEnabled = false
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    // Handle Google result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                setLoading(false)
                binding.signUpButton.isEnabled = true
                binding.googlesignUpButton.isEnabled = true
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account == null) {
            setLoading(false)
            binding.signUpButton.isEnabled = true
            binding.googlesignUpButton.isEnabled = true
            Toast.makeText(this, "Google sign-in failed (no account)", Toast.LENGTH_LONG).show()
            return
        }

        val idToken = account.idToken
        if (idToken == null) {
            setLoading(false)
            binding.signUpButton.isEnabled = true
            binding.googlesignUpButton.isEnabled = true
            Toast.makeText(this, "Google ID token missing", Toast.LENGTH_LONG).show()
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                setLoading(false)
                binding.signUpButton.isEnabled = true
                binding.googlesignUpButton.isEnabled = true
                if (authTask.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser == null) {
                        Toast.makeText(this, "Sign-in succeeded but no user found", Toast.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }

                    val uid = firebaseUser.uid
                    val email = firebaseUser.email
                    val displayName = firebaseUser.displayName ?: account.displayName

                    val user = User(
                        fullName = displayName,
                        email = email,
                        password = null,
                        provider = "google",
                        createdAt = System.currentTimeMillis()
                    )

                    // Save to Firestore instead of Realtime Database
                    val db = FirebaseFirestore.getInstance()
                    db.collection("Users")
                        .document(uid)
                        .set(user)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "Signed in with Google", Toast.LENGTH_SHORT).show()
                                // go to MainActivity and don't show sign_in again
                                startActivity(Intent(this, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to save profile: ${dbTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${authTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.etFullName.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
        binding.signUpButton.isEnabled = !isLoading
        binding.googlesignUpButton.isEnabled = !isLoading
    }

    private fun clearFields() {
        binding.etFullName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
