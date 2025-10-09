package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.agro.databinding.FragmentProfileBinding
import com.example.agro.sign_in

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
        // Example logic for setting up the user profile fragment
        // You can modify this according to your app's user data or database logic

        // Example: display hardcoded user info (replace with actual user data later)
//        binding.tvUserName.text = "John Doe"
//        binding.tvUserEmail.text = "john.doe@example.com"

        // Example: button click actions
        binding.btnEditProfile.setOnClickListener {
            // Handle Edit Profile action here
            // e.g., navigate to EditProfileFragment or open a dialog
        }

        binding.btnLogout.setOnClickListener {
            // Handle Logout logic here
           val intent = Intent(requireContext(), sign_in::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
