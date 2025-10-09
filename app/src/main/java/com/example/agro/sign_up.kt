package com.example.agro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class sign_up : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        val signupButton = findViewById<Button>(R.id.signUpButton)
        val signin = findViewById<TextView>(R.id.signin)

        signupButton.setOnClickListener {
            val i = Intent(this, sign_up::class.java)
            startActivity(i)
        }
        signin.setOnClickListener {
            val i = Intent(this, sign_in::class.java)
            startActivity(i)
        }
    }
}