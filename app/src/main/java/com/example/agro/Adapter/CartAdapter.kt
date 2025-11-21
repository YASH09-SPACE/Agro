package com.example.agro.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.R
import com.example.agro.Model.CartItem

class CartAdapter(private val cartList: MutableList<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.product_image)
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val productCategory: TextView = itemView.findViewById(R.id.product_category)
        val productPrice: TextView = itemView.findViewById(R.id.product_price)
        val productOldPrice: TextView = itemView.findViewById(R.id.product_old_price)
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
        holder.productPrice.text = item.price
        holder.productOldPrice.text = item.oldPrice
        holder.quantityText.text = item.quantity.toString()

        holder.increaseBtn.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
        }

        holder.decreaseBtn.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = cartList.size
}
