package com.example.agro

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.OrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmptyOrders: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView

    private val ordersList = mutableListOf<OrderItem>()
    private lateinit var ordersAdapter: OrdersAdapter

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.my_orders)

        rvOrders = findViewById(R.id.rvOrders)
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBackOrders)

        btnBack.setOnClickListener { finish() }

        rvOrders.layoutManager = LinearLayoutManager(this)
        ordersAdapter = OrdersAdapter(ordersList)
        rvOrders.adapter = ordersAdapter

        loadUserOrders()
    }

    private fun loadUserOrders() {
        val user = auth.currentUser
        if (user == null) {
            tvEmptyOrders.visibility = View.VISIBLE
            tvEmptyOrders.text = "Please login to see your orders"
            progressBar.visibility = View.GONE
            return
        }

        progressBar.visibility = View.VISIBLE
        tvEmptyOrders.visibility = View.GONE

        // ⬇️ Removed orderBy() to avoid composite index requirement
        db.collection("orders")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                ordersList.clear()

                if (snapshot.isEmpty) {
                    tvEmptyOrders.visibility = View.VISIBLE
                    tvEmptyOrders.text = "You have no orders yet"
                } else {
                    // temporary list to keep timestamp for sorting
                    val tempList = mutableListOf<Pair<Long, OrderItem>>()

                    for (doc in snapshot.documents) {
                        val id = doc.id
                        val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                        val status = doc.getString("status") ?: "pending"

                        val ts = doc.getTimestamp("orderDate")?.toDate()
                        val orderDateMillis = ts?.time ?: 0L
                        val orderDateText = if (ts != null) {
                            android.text.format.DateFormat
                                .format("dd MMM yyyy", ts)
                                .toString()
                        } else {
                            ""
                        }

                        val itemsArray = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val itemCount = itemsArray.size

                        val orderItem = OrderItem(
                            id = id,
                            orderDateText = orderDateText,
                            totalAmount = totalAmount,
                            status = status,
                            itemCount = itemCount
                        )

                        tempList.add(orderDateMillis to orderItem)
                    }

                    // 🔽 sort by date (latest first)
                    tempList.sortByDescending { it.first }

                    // move into ordersList for adapter
                    ordersList.addAll(tempList.map { it.second })
                }

                progressBar.visibility = View.GONE
                ordersAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                tvEmptyOrders.visibility = View.VISIBLE
                tvEmptyOrders.text = "Failed to load orders: ${e.message}"
            }
    }
}
