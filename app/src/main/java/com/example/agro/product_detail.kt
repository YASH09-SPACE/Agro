package com.example.agro

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.agro.Model.CartItem

class product_detail : AppCompatActivity() {

    private lateinit var imgProduct: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnAddToCart: Button

    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        imgProduct = findViewById(R.id.product_image)
        tvName = findViewById(R.id.product_name)
        tvCategory = findViewById(R.id.product_category)
        tvPrice = findViewById(R.id.current_price)
        tvDescription = findViewById(R.id.product_description)
        btnBack = findViewById(R.id.back_arrow)
        btnAddToCart = findViewById(R.id.add_to_cart_button)

        // 🔥 Get intent data
        val name = intent.getStringExtra("name") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val price = intent.getDoubleExtra("price", 0.0)
        val category = intent.getStringExtra("category") ?: ""
        val productId = intent.getStringExtra("productId") ?: ""
        val stockQuantity = intent.getIntExtra("stockQuantity", 0)
        val desc = intent.getStringExtra("description") ?: ""

        tvName.text = name
        tvCategory.text = category
        tvPrice.text = "₹${price.toInt()}"
        tvDescription.text = desc

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_product)
            .into(imgProduct)

        btnBack.setOnClickListener { finish() }

        // 🛒 Add to Cart
        btnAddToCart.setOnClickListener {
            CartManager.addToCart(
                CartItem(
                    productId = productId,
                    imageResId = 0,
                    imageUrl = imageUrl,  // 🔥 store url
                    name = name,
                    category = category,
                    price = price,
                    quantity = 1,
                    stockQuantity = stockQuantity
                )
            )
            Toast.makeText(this, "$name added to cart", Toast.LENGTH_SHORT).show()
        }
    }
}
