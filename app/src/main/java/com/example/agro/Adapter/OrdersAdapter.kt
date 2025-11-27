package com.example.agro

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.OrderItem

class OrdersAdapter(
    private val orders: List<OrderItem>
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvTitle: TextView = itemView.findViewById(R.id.tvProductName)   // here it's order title
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvItems: TextView = itemView.findViewById(R.id.tvItemsCount)    // new TextView in XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = orders[position]

        holder.imgIcon.setImageResource(item.iconResId)
        holder.tvTitle.text = "Order #${item.id.takeLast(6)}"     // last 6 chars of doc id
        holder.tvOrderDate.text = item.orderDateText
        holder.tvPrice.text = "₹${item.totalAmount.toInt()}"
        holder.tvItems.text = "${item.itemCount} item${if (item.itemCount == 1) "" else "s"}"

        holder.tvStatus.text = item.status.replaceFirstChar { it.uppercase() }
        val statusLower = item.status.lowercase()

        val colorHex = when {
            "delivered" in statusLower -> "#4CAF50" // green
            "shipped" in statusLower -> "#2196F3"   // blue
            "pending" in statusLower -> "#FFC107"   // amber
            else -> "#9E9E9E"                       // grey
        }

        holder.tvStatus.background?.setTint(Color.parseColor(colorHex))
    }

    override fun getItemCount(): Int = orders.size
}
