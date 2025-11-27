package com.example.agro

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agro.Model.CartItem
import com.example.agro.data.Product

class mainAdapter(private val itemList: List<Product>) :
    RecyclerView.Adapter<mainAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivProduct)
        val productName: TextView = view.findViewById(R.id.tvProductName)
        val price: TextView = view.findViewById(R.id.tvProductPrice)
        val addToCart: Button = view.findViewById(R.id.btnAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val product = itemList[position]

        // 🔹 Load Image Using Glide
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_product)
            .error(R.drawable.ic_product)
            .into(holder.image)

        // 🔹 Bind text
        holder.productName.text = product.name
        holder.price.text = "₹${product.price.toInt()}"
        holder.addToCart.text = "Add to Cart"

        // 🛒 Add to Cart Click
        holder.addToCart.setOnClickListener {
            CartManager.addToCart(
                CartItem(
                    productId = product.productId,
                    imageUrl = product.imageUrl,
                    name = product.name,
                    category = product.category,
                    price = product.price,
                    quantity = 1,
                    stockQuantity = product.stockQuantity
                )
            )
        }

        // 👉 Open Product Details on Card Click
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, product_detail::class.java).apply {
                putExtra("productId", product.productId)
                putExtra("name", product.name)
                putExtra("price", product.price)
                putExtra("imageUrl", product.imageUrl)
                putExtra("category", product.category)
                putExtra("description", product.description)
                putExtra("stock", product.stockQuantity)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
