package com.example.agro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.Post
import com.example.agro.databinding.ItemCommunityPostBinding

// Adapter for displaying community posts in a RecyclerView
class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // ViewHolder class to hold and bind views for each post item
    inner class PostViewHolder(private val binding: ItemCommunityPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            // Bind text data
            binding.tvUserName.text = post.userName
            binding.tvPostDate.text = post.postDate
            binding.tvDescription.text = post.description

            // Load image (if you plan to use Glide or similar)
            // Glide.with(binding.ivPostImage.context)
            //     .load(post.imageUrl)
            //     .placeholder(R.drawable.img_placeholder)
            //     .into(binding.ivPostImage)

            // Set initial like button state
            updateLikeButton(post.isLiked)

            // Handle Like button click
            binding.btnLike.setOnClickListener {
                val newIsLiked = !post.isLiked
                posts[adapterPosition].isLiked = newIsLiked
                updateLikeButton(newIsLiked)

                // TODO: Add API call or database update here
            }

            // Handle Comment button click
            binding.btnComment.setOnClickListener {
                // TODO: Add navigation or comment dialog here
            }

            // Handle Bookmark button click (if added)
            // binding.btnBookmark.setOnClickListener {
            //     // TODO: Add bookmark functionality here
            // }
        }

        // Updates the like button UI based on current state
        private fun updateLikeButton(isLiked: Boolean) {
            if (isLiked) {
                binding.btnLike.setImageResource(R.drawable.ic_heart_filled)
                binding.btnLike.setColorFilter(
                    binding.root.context.getColor(android.R.color.holo_red_dark)
                )
            } else {
                binding.btnLike.setImageResource(R.drawable.ic_heart_outline)
                binding.btnLike.setColorFilter(
                    binding.root.context.getColor(android.R.color.darker_gray)
                )
            }
        }
    }

    // Inflates layout using View Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemCommunityPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    // Binds data to ViewHolder for a specific position
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    // Returns total number of items
    override fun getItemCount(): Int = posts.size
}