package com.example.agro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.agro.data.Category
import com.google.firebase.firestore.FirebaseFirestore

class filter : AppCompatActivity() {

    private lateinit var btnClose: ImageView
    private lateinit var btnReset: TextView
    private lateinit var btnApplyFilter: Button
    private lateinit var etMinPrice: EditText
    private lateinit var etMaxPrice: EditText
    private lateinit var categoryContainer: LinearLayout

    private val PREFS_FILTER = "filter_prefs"
    private val KEY_MIN = "min_price_text"
    private val KEY_MAX = "max_price_text"

    // Firestore
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Category list (if you need later)
    private val categoryList = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_filter)

        // Find views
        btnClose = findViewById(R.id.btnClose)
        btnReset = findViewById(R.id.btnReset)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)
        etMinPrice = findViewById(R.id.etMinPrice)
        etMaxPrice = findViewById(R.id.etMaxPrice)
        categoryContainer = findViewById(R.id.categoryContainer)

        val prefs = getSharedPreferences(PREFS_FILTER, Context.MODE_PRIVATE)

        // Load last saved filter values
        etMinPrice.setText(prefs.getString(KEY_MIN, ""))
        etMaxPrice.setText(prefs.getString(KEY_MAX, ""))

        // Load categories from Firestore products
        loadCategoriesFromProducts()

        // Close button = go back
        btnClose.setOnClickListener {
            finish()
        }

        // Reset: clear fields and saved values
        btnReset.setOnClickListener {
            etMinPrice.text.clear()
            etMaxPrice.text.clear()

            prefs.edit()
                .remove(KEY_MIN)
                .remove(KEY_MAX)
                .apply()
        }

        // Apply filter
        btnApplyFilter.setOnClickListener {
            val minText = etMinPrice.text.toString().trim()
            val maxText = etMaxPrice.text.toString().trim()

            // Save raw text so it shows next time
            prefs.edit()
                .putString(KEY_MIN, minText)
                .putString(KEY_MAX, maxText)
                .apply()

            // Convert to Double, with safe defaults
            val minPrice = minText.toDoubleOrNull() ?: 0.0
            val maxPrice = maxText.toDoubleOrNull() ?: Double.MAX_VALUE

            val intent = Intent(this, search_products::class.java).apply {
                putExtra("min_price", minPrice)
                putExtra("max_price", maxPrice)
            }
            startActivity(intent)
            finish()
        }
    }

    /**
     * Load unique categories from Firestore "product" collection
     * and inflate item_category_filter for each.
     * Each category shows: name + total stockQuantity (like HomeFragment).
     */
    private fun loadCategoriesFromProducts() {
        db.collection("product")
            .get()
            .addOnSuccessListener { snapshot ->
                categoryList.clear()
                categoryContainer.removeAllViews()

                // category -> total stock
                val categoryStockMap = mutableMapOf<String, Int>()

                for (doc in snapshot.documents) {
                    val category = doc.getString("category") ?: "General"
                    val stockQty = (doc.getLong("stockQuantity") ?: 0L).toInt()

                    val current = categoryStockMap[category] ?: 0
                    categoryStockMap[category] = current + stockQty
                }

                // Build list of Category objects and add views
                for ((name, totalStock) in categoryStockMap) {
                    val category = Category(
                        title = name,
                        itemCount = totalStock,
                        iconResId = R.drawable.ic_crop_tonics
                    )
                    categoryList.add(category)

                    // Inflate one row from item_category_filter.xml
                    val itemView = layoutInflater.inflate(
                        R.layout.item_category_filter,
                        categoryContainer,
                        false
                    )

                    val tvName = itemView.findViewById<TextView>(R.id.tvCategoryName)
                    val tvCount = itemView.findViewById<TextView>(R.id.tvItemCount)
                    val ivIcon = itemView.findViewById<ImageView>(R.id.ivCategoryIcon)

                    tvName.text = category.title
                    tvCount.text = "${category.itemCount} Items"
                    ivIcon.setImageResource(category.iconResId)

                    // If you want click later, you can setOnClickListener here

                    categoryContainer.addView(itemView)
                }
            }
            .addOnFailureListener {
                // Optionally show a toast/log
            }
    }
}
