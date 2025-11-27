package com.example.agro.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agro.Model.CartItem
import com.example.agro.R

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val onCartChanged: () -> Unit    // callback to update totals
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productCategory: TextView = itemView.findViewById(R.id.product_category)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val quantityText: TextView = itemView.findViewById(R.id.quantity_text)
        val increaseBtn: ImageButton = itemView.findViewById(R.id.increase_button)
        val decreaseBtn: ImageButton = itemView.findViewById(R.id.decrease_button)
        val removeBtn: ImageButton = itemView.findViewById(R.id.remove_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        // 🔹 Load image (URL first, then drawable)
        if (!item.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.productImage)
        } else {
            val resId = item.imageResId ?: R.drawable.ic_product
            holder.productImage.setImageResource(resId)
        }

        holder.productName.text = item.name
        holder.productCategory.text = item.category
        holder.productPrice.text = "₹${item.price.toInt()}"
        holder.quantityText.text = item.quantity.toString()

        // ➕ Increase quantity
        holder.increaseBtn.setOnClickListener {
            item.quantity++
            notifyItemChanged(holder.bindingAdapterPosition)
            onCartChanged()
        }

        // ➖ Decrease quantity
        holder.decreaseBtn.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(holder.bindingAdapterPosition)
                onCartChanged()
            }
        }

        // ❌ Remove item
        holder.removeBtn.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                cartList.removeAt(pos)
                notifyItemRemoved(pos)
                onCartChanged()
            }
        }
    }

    override fun getItemCount(): Int = cartList.size
}
