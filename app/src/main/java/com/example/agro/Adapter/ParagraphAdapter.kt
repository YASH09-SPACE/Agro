// com/example/agro/Adapter/ParagraphAdapter.kt
package com.example.agro.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.R

class ParagraphAdapter(
    private val items: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<ParagraphAdapter.ParagraphViewHolder>() {

    inner class ParagraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvParagraph: TextView = itemView.findViewById(R.id.tvParagraph)

        fun bind(text: String) {
            tvParagraph.text = text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParagraphViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paragraph, parent, false)
        return ParagraphViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParagraphViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
