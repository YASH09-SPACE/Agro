package com.example.agro

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class checkout : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etZip: EditText
    private lateinit var etCity: EditText
    private lateinit var cbSaveAddress: CheckBox
    private lateinit var btnNext: Button
    private lateinit var btnBack: ImageView

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)

        // Find views
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        etZip = findViewById(R.id.etZip)
        etCity = findViewById(R.id.etCity)
        cbSaveAddress = findViewById(R.id.cbSaveAddress)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // Prefill from Users collection
        loadUserAddress()

        btnNext.setOnClickListener {
            goToPaymentScreen()
        }
    }

    private fun loadUserAddress() {
        val user = auth.currentUser ?: return

        // 🔹 Use "Users" collection (same as ProfileFragment)
        db.collection("Users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    etFullName.setText(doc.getString("fullName") ?: "")
                    // email from doc or from auth as fallback
                    etEmail.setText(doc.getString("email") ?: user.email ?: "")
                    etPhone.setText(doc.getString("phone") ?: "")
                    etAddress.setText(doc.getString("address") ?: "")
                    etZip.setText(doc.getString("zipCode") ?: "")
                    etCity.setText(doc.getString("city") ?: "")
                } else {
                    // no doc yet → at least set email from auth if present
                    etEmail.setText(user.email ?: "")
                }
            }
    }

    private fun goToPaymentScreen() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val zip = etZip.text.toString().trim()
        val city = etCity.text.toString().trim()

        if (fullName.isEmpty()) {
            etFullName.error = "Required"
            etFullName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Required"
            etEmail.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            etPhone.error = "Required"
            etPhone.requestFocus()
            return
        }
        if (address.isEmpty()) {
            etAddress.error = "Required"
            etAddress.requestFocus()
            return
        }

        val user = auth.currentUser

        // ✅ Save address back to Users collection (but NOT email)
        if (user != null && cbSaveAddress.isChecked) {
            val updates = mapOf(
                "fullName" to fullName,
                "phone" to phone,
                "address" to address,
                "zipCode" to zip,
                "city" to city
                // email stays as it is, not overwritten here
            )

            db.collection("Users")
                .document(user.uid)
                .set(updates, SetOptions.merge())
        }

        // Cart totals
        val subtotal = CartManager.getTotal()
        val tax = subtotal * 0.02
        val total = subtotal + tax
        val itemsCount = CartManager.cartItems.sumOf { it.quantity }

        // Go to payment activity and pass address + totals
        val intent = Intent(this, checkout_payment::class.java).apply {
            putExtra("fullName", fullName)
            putExtra("email", email)
            putExtra("phone", phone)
            putExtra("address", address)
            putExtra("zip", zip)
            putExtra("city", city)
            putExtra("totalAmount", total)
            putExtra("itemsCount", itemsCount)
        }
        startActivity(intent)
    }
}
