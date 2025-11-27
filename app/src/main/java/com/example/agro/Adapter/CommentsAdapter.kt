package com.example.agro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.Comment
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private var items: MutableList<Comment>,
    private val listener: CommentListener
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    interface CommentListener {
        fun onCommentLongClick(comment: Comment)
    }

    fun updateData(newList: List<Comment>) {
        items = newList.toMutableList()
        notifyDataSetChanged()
    }

    // 🔹 REQUIRED IMPLEMENTATION (FIX ERROR)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // 🔹 Inner ViewHolder
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUser: TextView = itemView.findViewById(R.id.tvCommentUser)
        private val tvText: TextView = itemView.findViewById(R.id.tvCommentText)
        private val tvDate: TextView = itemView.findViewById(R.id.tvCommentDate)

        private val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        fun bind(comment: Comment) {
            tvUser.text = comment.userName
            tvText.text = comment.text

            val time = comment.createdAt?.toDate()
            tvDate.text = if (time != null) formatter.format(time) else ""

            // 🔹 Allow delete only on long click
            itemView.setOnLongClickListener {
                listener.onCommentLongClick(comment)
                true
            }
        }
    }
}
