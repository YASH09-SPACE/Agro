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
import com.google.firebase.firestore.Query

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

        db.collection("orders")
            .whereEqualTo("userId", user.uid)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                ordersList.clear()

                if (snapshot.isEmpty) {
                    tvEmptyOrders.visibility = View.VISIBLE
                    tvEmptyOrders.text = "You have no orders yet"
                } else {
                    for (doc in snapshot.documents) {
                        val id = doc.id
                        val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                        val status = doc.getString("status") ?: "pending"

                        val ts = doc.getTimestamp("orderDate")?.toDate()
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

                        ordersList.add(orderItem)
                    }
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
