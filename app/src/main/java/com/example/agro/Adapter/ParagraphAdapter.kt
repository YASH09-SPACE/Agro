package com.example.agro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.R
import com.example.agro.model.Paragraph

class ParagraphAdapter(private val paragraphs: List<Paragraph>) :
    RecyclerView.Adapter<ParagraphAdapter.ParagraphViewHolder>() {

    inner class ParagraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paragraphText: TextView = itemView.findViewById(R.id.paragraphText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParagraphViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paragraph, parent, false)
        return ParagraphViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParagraphViewHolder, position: Int) {
        val paragraph = paragraphs[position]
        holder.paragraphText.text = paragraph.text
    }

    override fun getItemCount(): Int = paragraphs.size
}
