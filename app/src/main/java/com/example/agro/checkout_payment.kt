package com.example.agro

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.agro.Model.CartItem
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import kotlin.math.roundToInt

class checkout_payment : AppCompatActivity(), PaymentResultListener {

    private lateinit var cardCash: LinearLayout
    private lateinit var cardOnline: LinearLayout
    private lateinit var btnProceedPayment: Button
    private lateinit var btnBack: ImageView

    private var selectedPaymentMethod: String = "COD"  // "COD" or "ONLINE"

    // Data from checkout activity
    private var fullName: String = ""
    private var email: String = ""
    private var phone: String = ""
    private var address: String = ""
    private var zip: String = ""
    private var city: String = ""
    private var totalAmount: Double = 0.0
    private var itemsCount: Int = 0

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout_payment)

        // 🔹 Preload Razorpay (recommended)
        Checkout.preload(applicationContext)

        // Read extras from checkout activity
        fullName = intent.getStringExtra("fullName") ?: ""
        email = intent.getStringExtra("email") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        address = intent.getStringExtra("address") ?: ""
        zip = intent.getStringExtra("zip") ?: ""
        city = intent.getStringExtra("city") ?: ""
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        itemsCount = intent.getIntExtra("itemsCount", 0)

        cardCash = findViewById(R.id.cardCash)
        cardOnline = findViewById(R.id.cardOnline)
        btnProceedPayment = findViewById(R.id.btnProceedPayment)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // Default: COD selected
        selectCOD()

        cardCash.setOnClickListener { selectCOD() }
        cardOnline.setOnClickListener { selectOnline() }

        btnProceedPayment.setOnClickListener {
            if (selectedPaymentMethod == "COD") {
                // Cash on delivery: directly place order without Razorpay
                placeOrder(paymentMethod = "cash_on_delivery", razorpayPaymentId = null)
            } else {
                // Online payment: start Razorpay checkout
                startRazorpayPayment()
            }
        }
    }

    // -----------------------------
    // UI selection helpers
    // -----------------------------
    private fun selectCOD() {
        selectedPaymentMethod = "COD"
        cardCash.setBackgroundResource(R.drawable.bg_card_selected)
        cardOnline.setBackgroundResource(R.drawable.bg_card_unselected)
    }

    private fun selectOnline() {
        selectedPaymentMethod = "ONLINE"
        cardOnline.setBackgroundResource(R.drawable.bg_card_selected)
        cardCash.setBackgroundResource(R.drawable.bg_card_unselected)
    }

    // -----------------------------
    // RAZORPAY PAYMENT
    // -----------------------------
    private fun startRazorpayPayment() {
        if (totalAmount <= 0.0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val checkout = Checkout()

        // 🔹 Replace with your Razorpay Key ID
        //    rzp_test_xxx for test, rzp_live_xxx for production
        checkout.setKeyID("rzp_test_RjfdCf4GPTx0oo")

        // (Optional) Set your logo/icon
        checkout.setImage(R.drawable.ic_product)

        try {
            val options = JSONObject()

            options.put("name", "Agro App")            // Your app / company name
            options.put("description", "Order Payment")
            options.put("currency", "INR")

            // Razorpay amount is in paise (₹1 = 100)
            val amountInPaise = (totalAmount * 100).roundToInt()
            options.put("amount", amountInPaise)

            // Prefill customer info
            val prefill = JSONObject()
            prefill.put("email", email)
            prefill.put("contact", phone)
            options.put("prefill", prefill)

            // Optional notes
            val notes = JSONObject()
            notes.put("address", "$address, $city - $zip")
            options.put("notes", notes)

            checkout.open(this, options)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Called when Razorpay payment is successful
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Toast.makeText(this, "Payment Success: $razorpayPaymentId", Toast.LENGTH_SHORT).show()
        // Save order with status = "paid" or "pending" as you like
        placeOrder(paymentMethod = "online_payment", razorpayPaymentId = razorpayPaymentId)
    }

    // Called when Razorpay payment fails/cancelled
    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
        // Don't place order here
    }

    // -----------------------------
    // PLACE ORDER IN FIRESTORE
    // -----------------------------
    /**
     * Save order to Firestore "orders" collection.
     *
     * Schema:
     *  - userId: String
     *  - orderDate: Timestamp
     *  - totalAmount: Double
     *  - status: "pending" / "shipped" / "delivered"
     *  - paymentMethod: "cash_on_delivery" / "online_payment"
     *  - paymentId: Razorpay payment id or null for COD
     *  - shippingAddress: { fullName, email, phone, address, zip, city }
     *  - items: [ { productId, name, quantity, priceAtTimeOfOrder } ]
     */
    private fun placeOrder(paymentMethod: String, razorpayPaymentId: String?) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        if (CartManager.cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Build items array from CartManager
        val itemsList = CartManager.cartItems.map { cartItem: CartItem ->
            mapOf(
                "productId" to cartItem.productId,
                "name" to cartItem.name,
                "quantity" to cartItem.quantity,
                "priceAtTimeOfOrder" to cartItem.price
            )
        }

        val orderData = hashMapOf(
            "userId" to user.uid,
            "orderDate" to FieldValue.serverTimestamp(),
            "totalAmount" to totalAmount,
            "status" to if (paymentMethod == "online_payment") "paid" else "pending",
            "paymentMethod" to paymentMethod,
            "paymentId" to (razorpayPaymentId ?: ""),
            "itemsCount" to itemsCount,
            "shippingAddress" to mapOf(
                "fullName" to fullName,
                "email" to email,
                "phone" to phone,
                "address" to address,
                "zip" to zip,
                "city" to city
            ),
            "items" to itemsList
        )

        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener { docRef ->
                val orderId = docRef.id

                // Clear cart after successful order
                CartManager.cartItems.clear()

                showOrderSuccessDialog(orderId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to place order: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showOrderSuccessDialog(orderId: String) {
        AlertDialog.Builder(this)
            .setTitle("Order Placed")
            .setMessage("Your order has been placed successfully.\nOrder ID: $orderId")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish() // close payment activity
            }
            .setCancelable(false)
            .show()
    }
}
