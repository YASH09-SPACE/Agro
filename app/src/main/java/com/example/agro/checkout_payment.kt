package com.example.agro

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class checkout_payment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout_payment)
        val cardCash = findViewById<LinearLayout>(R.id.cardCash)
        val cardOnline = findViewById<LinearLayout>(R.id.cardOnline)

        cardCash.setOnClickListener {
            cardCash.setBackgroundResource(R.drawable.bg_card_selected)
            cardOnline.setBackgroundResource(R.drawable.bg_card_unselected)
        }

        cardOnline.setOnClickListener {
            cardOnline.setBackgroundResource(R.drawable.bg_card_selected)
            cardCash.setBackgroundResource(R.drawable.bg_card_unselected)
        }

    }
}