package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.agro.MyOrdersActivity
import com.example.agro.databinding.FragmentProfileBinding
import com.example.agro.sign_in
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        setupUserProfile()
        return view
    }

    private fun setupUserProfile() {
        // Email should not be editable
        binding.etEmail.isEnabled = false
        binding.etEmail.isFocusable = false
        binding.etEmail.isFocusableInTouchMode = false

        // Load user data from Firestore "users" collection
        loadUserDetails()

        // Save / update profile (except email)
        binding.btnEditProfile.setOnClickListener {
            saveProfileChanges()
        }

        // My Orders button → open MyOrdersActivity
        binding.btnMyorders.setOnClickListener {
            val intent = Intent(requireContext(), MyOrdersActivity::class.java)
            startActivity(intent)
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            // 1. Logout from FirebaseAuth
            FirebaseAuth.getInstance().signOut()

            // 2. Also logout from Google if used
            val googleSignInClient = GoogleSignIn.getClient(
                requireContext(),
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            )
            googleSignInClient.signOut()

            // 3. Redirect to sign_in and clear back stack
            val intent = Intent(requireContext(), sign_in::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserDetails() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        // users collection → document = uid
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    // No profile yet – at least show email from auth
                    binding.etEmail.setText(user.email ?: "")
                    return@addOnSuccessListener
                }

                val fullName = doc.getString("fullName") ?: ""
                val email = doc.getString("email") ?: user.email ?: ""
                val phone = doc.getString("phone") ?: ""
                val address = doc.getString("address") ?: ""
                val zipCode = doc.getString("zipCode") ?: ""
                val city = doc.getString("city") ?: ""

                binding.etFullName.setText(fullName)
                binding.etEmail.setText(email)
                binding.etPhone.setText(phone)
                binding.etAddress.setText(address)
                binding.etZipCode.setText(zipCode)
                binding.etCity.setText(city)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveProfileChanges() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val zipCode = binding.etZipCode.text.toString().trim()
        val city = binding.etCity.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name required"
            binding.etFullName.requestFocus()
            return
        }

        // Only update allowed fields (email stays as it is)
        val updates = mapOf(
            "fullName" to fullName,
            "phone" to phone,
            "address" to address,
            "zipCode" to zipCode,
            "city" to city
        )

        // Merge so we don't overwrite other fields like createdAt, provider, etc.
        db.collection("users")
            .document(user.uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to update profile: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
