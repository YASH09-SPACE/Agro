// com/example/agro/CropAdapter.kt
package com.example.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agro.data.Crop

class CropAdapter(
    private val onItemClick: (Crop) -> Unit
) : RecyclerView.Adapter<CropAdapter.CropViewHolder>() {

    private val fullList = mutableListOf<Crop>()
    private val displayList = mutableListOf<Crop>()
    private var lastQuery: String = ""

    inner class CropViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCropImage: ImageView = itemView.findViewById(R.id.ivCropImage)
        private val tvCropName: TextView = itemView.findViewById(R.id.tvCropName)

        fun bind(crop: Crop) {
            tvCropName.text = crop.name

            Glide.with(itemView.context)
                .load(crop.imageUrl)
                .placeholder(R.drawable.ic_date) // your placeholder
                .into(ivCropImage)

            itemView.setOnClickListener { onItemClick(crop) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crop, parent, false)
        return CropViewHolder(view)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        holder.bind(displayList[position])
    }

    override fun getItemCount(): Int = displayList.size

    fun setData(newList: List<Crop>) {
        fullList.clear()
        fullList.addAll(newList)
        applyFilter(lastQuery)
    }

    fun filterCrops(query: String) {
        lastQuery = query
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val q = query.trim().lowercase()
        displayList.clear()

        if (q.isEmpty()) {
            displayList.addAll(fullList)
        } else {
            val startsWith = fullList.filter {
                it.name.lowercase().startsWith(q)
            }
            val contains = fullList.filter {
                it.name.lowercase().contains(q) && !startsWith.contains(it)
            }
            val others = fullList.filter {
                !it.name.lowercase().contains(q)
            }
            displayList.addAll(startsWith + contains + others)
        }

        notifyDataSetChanged()
    }
}
