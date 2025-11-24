package com.example.agro.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.R

// Model used in search results
data class SearchProduct(
    val productId: String,      // 👈 Firestore doc id
    val name: String,
    val price: Double,
    val category: String,
    val stockQuantity: Int
)

class SearchResultAdapter(
    private val items: MutableList<SearchProduct>,
    private val onAddToCart: (SearchProduct) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvProductDiscount: TextView = itemView.findViewById(R.id.tvProductDiscount)
        val btnCart: ImageView = itemView.findViewById(R.id.btnCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_product, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = items[position]

        holder.imgProduct.setImageResource(R.drawable.ic_product) // placeholder image
        holder.tvProductName.text = item.name
        holder.tvProductPrice.text = "₹${item.price.toInt()}"
        holder.tvProductDiscount.visibility = View.GONE // no discount yet

        holder.btnCart.setOnClickListener {
            onAddToCart(item)
            Toast.makeText(
                holder.itemView.context,
                "${item.name} added to cart",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int = items.size
}
