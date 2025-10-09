package com.example.agro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class sign_in : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val createacc = findViewById<TextView>(R.id.createAccount)
        signInButton.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)

        }
        createacc.setOnClickListener {
            val i = Intent(this, sign_up::class.java)
            startActivity(i)

        }

    }

}