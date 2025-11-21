package com.example.agro.Adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.databinding.ItemListTopicBinding

class TopicAdapter(
    private val topics: List<String>,
    private val itemClickListener: (String) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    inner class TopicViewHolder(val binding: ItemListTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: String) {
            binding.topicText.text = topic
            binding.root.setOnClickListener {
                itemClickListener(topic)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemListTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(topics[position])
    }

    override fun getItemCount(): Int = topics.size
}
