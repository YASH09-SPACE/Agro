// com/example/agro/Adapter/TopicAdapter.kt
package com.example.agro.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.R

class TopicAdapter(
    private val topics: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    inner class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTopicTitle)

        fun bind(title: String) {
            tvTitle.text = title
            itemView.setOnClickListener { onItemClick(title) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(topics[position])
    }

    override fun getItemCount(): Int = topics.size
}
