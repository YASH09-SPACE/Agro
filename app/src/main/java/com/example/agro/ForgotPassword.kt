package com.example.agro

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class ForgotPassword : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtOtp: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnSendOtp: Button
    private lateinit var btnResetPassword: Button
    private lateinit var emailStepLayout: LinearLayout
    private lateinit var otpStepLayout: LinearLayout
    private lateinit var progressOverlay: View

    private val db = FirebaseFirestore.getInstance()

    private var userDocId: String? = null
    private var generatedOtp: String? = null

    // TODO: put safe values here
    private val GMAIL_EMAIL = "utsavsakariya05@gmail.com"
    private val GMAIL_APP_PASSWORD = "wxli ycwt svzo hwex"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initViews()

        btnSendOtp.setOnClickListener { sendOtpFlow() }
        btnResetPassword.setOnClickListener { resetPasswordFlow() }
        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener { finish() }
    }

    private fun initViews() {
        edtEmail = findViewById(R.id.EdtForgotEmail)
        edtOtp = findViewById(R.id.EdtOtp)
        edtNewPassword = findViewById(R.id.EdtNewPassword)
        edtConfirmPassword = findViewById(R.id.EdtConfirmPassword)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        emailStepLayout = findViewById(R.id.emailStepLayout)
        otpStepLayout = findViewById(R.id.otpStepLayout)
        progressOverlay = findViewById(R.id.progressOverlay)
    }

    private fun showLoading(show: Boolean) {
        progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    /* ---------------- STEP 1: SEND OTP ---------------- */

    private fun sendOtpFlow() {
        val email = edtEmail.text.toString().trim()

        if (email.isEmpty()) {
            edtEmail.error = "Email required"
            return
        }

        showLoading(true)

        // find user by email in Users collection
        db.collection("Users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    showLoading(false)
                    Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show()
                } else {
                    val doc = snapshot.documents[0]
                    userDocId = doc.id
                    generatedOtp = (100000..999999).random().toString()

                    val otpData = hashMapOf(
                        "email" to email,
                        "userId" to userDocId,
                        "otp" to generatedOtp,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("passwordOtps")
                        .document(userDocId!!)
                        .set(otpData)
                        .addOnSuccessListener {
                            sendOtpEmail(email, generatedOtp!!)
                        }
                        .addOnFailureListener {
                            showLoading(false)
                            Toast.makeText(this, "Failed to create OTP. Try again.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Error : ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendOtpEmail(receiverEmail: String, otp: String) {

        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.socketFactory.port", "465")
                    put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.port", "465")
                }

                val session = Session.getDefaultInstance(props,
                    object : javax.mail.Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(GMAIL_EMAIL, GMAIL_APP_PASSWORD)
                        }
                    })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(GMAIL_EMAIL, "Agro E-Commerce"))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail))
                    subject = "Your Agro Password Reset OTP"
                    setText(
                        "Your OTP for resetting password is: $otp\n\n" +
                                "This OTP is valid for 10 minutes.\n" +
                                "If you did not request this, please ignore this email."
                    )
                }

                Transport.send(message)

                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_LONG).show()
                    emailStepLayout.visibility = View.GONE
                    otpStepLayout.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Failed to send OTP: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    /* ------------- STEP 2: VERIFY OTP & RESET PASSWORD ------------- */

    private fun resetPasswordFlow() {
        val otpInput = edtOtp.text.toString().trim()
        val newPass = edtNewPassword.text.toString().trim()
        val confirmPass = edtConfirmPassword.text.toString().trim()

        if (otpInput.isEmpty()) {
            edtOtp.error = "Enter OTP"
            return
        }
        if (newPass.length < 6) {
            edtNewPassword.error = "Min 6 characters"
            return
        }
        if (newPass != confirmPass) {
            edtConfirmPassword.error = "Passwords do not match"
            return
        }

        val userId = userDocId
        if (userId == null) {
            Toast.makeText(this, "Something went wrong. Start again.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        db.collection("passwordOtps")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val otpFromDb = doc.getString("otp")

                if (otpFromDb == null || otpFromDb != otpInput) {
                    showLoading(false)
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                } else {
                    // NOTE: if you store hashed password, hash 'newPass' the same way here.
                    db.collection("Users")
                        .document(userId)
                        .update("password", newPass)
                        .addOnSuccessListener {
                            db.collection("passwordOtps").document(userId).delete() // optional

                            showLoading(false)
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_LONG).show()
                            finish() // back to sign in
                        }
                        .addOnFailureListener {
                            showLoading(false)
                            Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
