package com.example.agro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class walkthrough2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_walkthrough2)
        val but = findViewById<Button>(R.id.start)

        but.setOnClickListener {
            val i = Intent(this, sign_in::class.java)
            startActivity(i)

        }

    }
}