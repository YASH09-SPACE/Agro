package com.example.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.Post
import com.bumptech.glide.Glide

class PostAdapter(
    private var items: MutableList<Post>,
    private val listener: PostListener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface PostListener {
        fun onPostClicked(post: Post)
        fun onLikeClicked(post: Post)
        fun onCommentClicked(post: Post)
    }

    fun updateData(newList: List<Post>) {
        items = newList.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_post, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvPostDate: TextView = itemView.findViewById(R.id.tvPostDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        private val tvComments: TextView = itemView.findViewById(R.id.tvComments)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        private val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)
        private val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)

        fun bind(post: Post) {
            tvUserName.text = post.userName
            tvDescription.text = post.description
            tvLikes.text = "${post.likeCount} Likes"
            tvComments.text = "${post.commentCount} Comments"

            // CreatedAt display (optional)
            post.createdAt?.toDate()?.let {
                val text = android.text.format.DateFormat.format("dd MMM yyyy", it)
                tvPostDate.text = text
            }

            if (!post.imageUrl.isNullOrEmpty()) {
                ivPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .into(ivPostImage)
            } else {
                ivPostImage.visibility = View.GONE
            }

            btnLike.setImageResource(
                if (post.isLikedByCurrentUser) R.drawable.ic_heart_filled
                else R.drawable.ic_heart_outline
            )

            itemView.setOnClickListener { listener.onPostClicked(post) }
            btnLike.setOnClickListener { listener.onLikeClicked(post) }
            btnComment.setOnClickListener { listener.onCommentClicked(post) }
        }
    }
}
