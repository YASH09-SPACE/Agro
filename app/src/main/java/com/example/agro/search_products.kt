//package com.example.agro
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.agro.Adapter.SearchProduct
//import com.example.agro.Adapter.SearchResultAdapter
//import com.example.agro.Model.CartItem
//import com.google.firebase.firestore.FirebaseFirestore
//
//class search_products : AppCompatActivity() {
//
//    private lateinit var etSearch: EditText
//    private lateinit var rvSearchResults: RecyclerView
//    private lateinit var tvRecentItem: TextView
//    private lateinit var tvClearAll: TextView
//    private lateinit var btnRemoveRecent: ImageView
//    private lateinit var btnClose: ImageView
//    private lateinit var btnFilter: ImageView
//
//    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
//
//    private val searchResults = mutableListOf<SearchProduct>()
//    private lateinit var searchAdapter: SearchResultAdapter
//
//    private val PREFS_NAME = "search_prefs"
//    private val KEY_RECENT = "recent_searches" // stored as "q1|q2|q3"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_search_products)
//
//        // --- Find Views ---
//        etSearch = findViewById(R.id.etSearch)
//        rvSearchResults = findViewById(R.id.rvSearchResults)
//        tvRecentItem = findViewById(R.id.tvRecentItem)
//        tvClearAll = findViewById(R.id.tvClearAll)
//        btnRemoveRecent = findViewById(R.id.btnRemoveRecent)
//        btnClose = findViewById(R.id.btnClose)
//        btnFilter = findViewById(R.id.btnFilter)
//
//        // --- Close button: just finish this activity ---
//        btnClose.setOnClickListener {
//            finish()
//        }
//
//        // --- Filter button: open filter screen (your existing activity) ---
//        btnFilter.setOnClickListener {
//            val i = Intent(this, filter::class.java)
//            startActivity(i)
//        }
//
//        // --- RecyclerView setup ---
//        rvSearchResults.layoutManager = LinearLayoutManager(this)
//        searchAdapter = SearchResultAdapter(searchResults) { product ->
//            // Add to CartManager when cart icon clicked
//            CartManager.addToCart(
//                CartItem(
//                    imageResId = R.drawable.ic_product,
//                    name = product.name,
//                    category = product.category,
//                    price = product.price,
//                    stockQuantity = product.stockQuantity
//                )
//            )
//            Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
//        }
//        rvSearchResults.adapter = searchAdapter
//
//        // --- Load recent search UI ---
//        updateRecentUI()
//
//        // --- Clear all recent searches ---
//        tvClearAll.setOnClickListener {
//            saveRecentSearches(emptyList())
//            updateRecentUI()
//        }
//
//        // --- Remove most recent search (first one) ---
//        btnRemoveRecent.setOnClickListener {
//            val list = getRecentSearches()
//            if (list.isNotEmpty()) {
//                list.removeAt(0)
//                saveRecentSearches(list)
//                updateRecentUI()
//            }
//        }
//
//        // --- Handle typing in search box ---
//        etSearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//
//            override fun afterTextChanged(s: Editable?) {
//                val query = s?.toString()?.trim() ?: ""
//                if (query.isEmpty()) {
//                    searchResults.clear()
//                    searchAdapter.notifyDataSetChanged()
//                } else {
//                    searchProducts(query)
//                }
//            }
//        })
//    }
//
//    // --------------------------
//    //  SEARCH PRODUCTS FROM FIRESTORE (CONTAINS, IGNORE CASE)
//    // --------------------------
//    private fun searchProducts(query: String) {
//        // Save query to recent
//        saveQueryToRecent(query)
//        updateRecentUI()
//
//        db.collection("product")
//            .get()
//            .addOnSuccessListener { snapshot ->
//                searchResults.clear()
//
//                if (snapshot.isEmpty) {
//                    Toast.makeText(this, "No products in Firestore", Toast.LENGTH_SHORT).show()
//                }
//
//                for (doc in snapshot.documents) {
//                    val name = doc.getString("name") ?: continue
//                    val priceDouble = doc.getDouble("price") ?: 0.0
//                    val category = doc.getString("category") ?: ""
//                    val stockQty = (doc.getLong("stockQuantity") ?: 0L).toInt()
//
//                    // 🔥 Local filter: if name contains query (any position), case-insensitive
//                    if (name.contains(query, ignoreCase = true)) {
//                        val product = SearchProduct(
//                            name = name,
//                            price = priceDouble,
//                            category = category,
//                            stockQuantity = stockQty
//                        )
//                        searchResults.add(product)
//                    }
//                }
//
//                // Optional: see how many matched
//                Toast.makeText(
//                    this,
//                    "Found ${searchResults.size} products",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                searchAdapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    // --------------------------
//    //  RECENT SEARCH STORAGE
//    // --------------------------
//    private fun getRecentSearches(): MutableList<String> {
//        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val data = prefs.getString(KEY_RECENT, "") ?: ""
//        if (data.isBlank()) return mutableListOf()
//        return data.split("|").filter { it.isNotBlank() }.toMutableList()
//    }
//
//    private fun saveRecentSearches(list: List<String>) {
//        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val joined = list.joinToString("|")
//        prefs.edit().putString(KEY_RECENT, joined).apply()
//    }
//
//    // Add query at front, max 3 terms
//    private fun saveQueryToRecent(query: String) {
//        val list = getRecentSearches()
//        list.remove(query)         // avoid duplicates
//        list.add(0, query)         // add at front
//        if (list.size > 3) {
//            while (list.size > 3) {
//                list.removeAt(list.size - 1)
//            }
//        }
//        saveRecentSearches(list)
//    }
//
//    private fun updateRecentUI() {
//        val list = getRecentSearches()
//        if (list.isEmpty()) {
//            tvRecentItem.text = "No recent searches"
//        } else {
//            tvRecentItem.text = list.joinToString(", ")
//        }
//    }
//}
package com.example.agro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.Adapter.SearchProduct
import com.example.agro.Adapter.SearchResultAdapter
import com.example.agro.Model.CartItem
import com.google.firebase.firestore.FirebaseFirestore

class search_products : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var tvRecentItem: TextView
    private lateinit var tvClearAll: TextView
    private lateinit var btnRemoveRecent: ImageView
    private lateinit var btnClose: ImageView
    private lateinit var btnFilter: ImageView

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val searchResults = mutableListOf<SearchProduct>()
    private lateinit var searchAdapter: SearchResultAdapter

    private val PREFS_NAME = "search_prefs"
    private val KEY_RECENT = "recent_searches" // stored as "q1|q2|q3"

    // price filter values from filter screen (null = no limit)
    private var filterMinPrice: Double? = null
    private var filterMaxPrice: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_products)

        // --- Find Views ---
        etSearch = findViewById(R.id.etSearch)
        rvSearchResults = findViewById(R.id.rvSearchResults)
        tvRecentItem = findViewById(R.id.tvRecentItem)
        tvClearAll = findViewById(R.id.tvClearAll)
        btnRemoveRecent = findViewById(R.id.btnRemoveRecent)
        btnClose = findViewById(R.id.btnClose)
        btnFilter = findViewById(R.id.btnFilter)

        // --- Close button: just finish this activity ---
        btnClose.setOnClickListener {
            finish()
        }

        // --- Filter button: open filter screen ---
        btnFilter.setOnClickListener {
            val i = Intent(this, filter::class.java)
            startActivity(i)
        }

        // --- RecyclerView setup ---
        rvSearchResults.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchResultAdapter(searchResults) { product ->
            CartManager.addToCart(
                CartItem(
                    productId = product.productId,
                    imageResId = R.drawable.ic_product,
                    name = product.name,
                    category = product.category,
                    price = product.price,
                    quantity = 1,                           // default
                    stockQuantity = product.stockQuantity
                )
            )
            Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        rvSearchResults.adapter = searchAdapter

        // --- Load recent search UI ---
        updateRecentUI()

        // --- Clear all recent searches ---
        tvClearAll.setOnClickListener {
            saveRecentSearches(emptyList())
            updateRecentUI()
        }

        // --- Remove most recent search (first one) ---
        btnRemoveRecent.setOnClickListener {
            val list = getRecentSearches()
            if (list.isNotEmpty()) {
                list.removeAt(0)
                saveRecentSearches(list)
                updateRecentUI()
            }
        }

        // 🔥 Read price filter from Intent extras (coming from filter activity)
        if (intent.hasExtra("min_price")) {
            filterMinPrice = intent.getDoubleExtra("min_price", 0.0)
        }
        if (intent.hasExtra("max_price")) {
            filterMaxPrice = intent.getDoubleExtra("max_price", Double.MAX_VALUE)
        }

        // If we have any price filter, load products immediately
        if (filterMinPrice != null || filterMaxPrice != null) {
            loadProducts(null)   // no name query yet
        }

        // --- Handle typing in search box ---
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                if (query.isNullOrEmpty()) {
                    // Only price filter (or nothing)
                    loadProducts(null)
                } else {
                    loadProducts(query)
                }
            }
        })
    }

    /**
     * Load products from Firestore and filter:
     * - by name contains query (if query != null/empty)
     * - by price >= min & <= max (if filterMinPrice / filterMaxPrice not null)
     */
    private fun loadProducts(query: String?) {
        val q = query?.trim().orEmpty()

        val min = filterMinPrice ?: 0.0
        val max = filterMaxPrice ?: Double.MAX_VALUE

        // Save recent search only when user actually typed something
        if (q.isNotEmpty()) {
            saveQueryToRecent(q)
            updateRecentUI()
        }

        db.collection("product")
            .get()
            .addOnSuccessListener { snapshot ->
                searchResults.clear()

                for (doc in snapshot.documents) {
                    val name = doc.getString("name") ?: continue
                    val priceDouble = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: ""
                    val stockQty = (doc.getLong("stockQuantity") ?: 0L).toInt()

                    // Filter by name (only if query present)
                    if (q.isNotEmpty() && !name.contains(q, ignoreCase = true)) {
                        continue
                    }

                    // Filter by price range
                    if (priceDouble < min || priceDouble > max) {
                        continue
                    }

                    searchResults.add(
                        SearchProduct(
                            productId = doc.id,          // 👈 Firestore document id
                            name = name,
                            price = priceDouble,
                            category = category,
                            stockQuantity = stockQty
                        )
                    )

                }

                searchAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --------------------------
    //  RECENT SEARCH STORAGE
    // --------------------------
    private fun getRecentSearches(): MutableList<String> {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_RECENT, "") ?: ""
        if (data.isBlank()) return mutableListOf()
        return data.split("|").filter { it.isNotBlank() }.toMutableList()
    }

    private fun saveRecentSearches(list: List<String>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val joined = list.joinToString("|")
        prefs.edit().putString(KEY_RECENT, joined).apply()
    }

    private fun saveQueryToRecent(query: String) {
        val list = getRecentSearches()
        list.remove(query)
        list.add(0, query)
        if (list.size > 3) {
            while (list.size > 3) {
                list.removeAt(list.size - 1)
            }
        }
        saveRecentSearches(list)
    }

    private fun updateRecentUI() {
        val list = getRecentSearches()
        if (list.isEmpty()) {
            tvRecentItem.text = "No recent searches"
        } else {
            tvRecentItem.text = list.joinToString(", ")
        }
    }
}
