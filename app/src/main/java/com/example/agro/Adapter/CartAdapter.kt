package com.example.agro.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.Model.CartItem
import com.example.agro.R

class CartAdapter(
    private val cartList: MutableList<CartItem>,
    private val updateTotal: () -> Unit      // 🔥 Callback to update totals
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productCategory: TextView = itemView.findViewById(R.id.product_category)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val quantityText: TextView = itemView.findViewById(R.id.quantity_text)
        val increaseBtn: ImageButton = itemView.findViewById(R.id.increase_button)
        val decreaseBtn: ImageButton = itemView.findViewById(R.id.decrease_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartList[position]

        holder.productImage.setImageResource(item.imageResId)
        holder.productName.text = item.name
        holder.productCategory.text = item.category
        holder.productPrice.text = "₹${item.price}"
        holder.quantityText.text = item.quantity.toString()

        // 🔥 Increase Quantity
        holder.increaseBtn.setOnClickListener {
            if (item.quantity < item.stockQuantity) {
                item.quantity++
                notifyItemChanged(position)
                updateTotal()  // refresh totals
            } else {
                Toast.makeText(holder.itemView.context,
                    "Only ${item.stockQuantity} in stock!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // 🔥 Decrease Quantity
        holder.decreaseBtn.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                updateTotal()
            } else {
                // Optional: remove if quantity goes below 1
                cartList.removeAt(position)
                notifyItemRemoved(position)
                updateTotal()
            }
        }
    }


    override fun getItemCount(): Int = cartList.size
}
