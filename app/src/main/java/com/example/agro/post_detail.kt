package com.example.agro

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agro.data.Comment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class post_detail : AppCompatActivity() {

    private lateinit var postImage: ImageView
    private lateinit var userNameTv: TextView
    private lateinit var postDateTv: TextView
    private lateinit var postDescriptionTv: TextView
    private lateinit var noCommentsText: TextView
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var progressBarComments: ProgressBar
    private lateinit var backButton: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>()

    private var postId: String = ""

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        postImage = findViewById(R.id.post_image)
        userNameTv = findViewById(R.id.user_name)
        postDateTv = findViewById(R.id.post_date)
        postDescriptionTv = findViewById(R.id.post_description)
        noCommentsText = findViewById(R.id.no_comments_text)
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        commentInput = findViewById(R.id.comment_input)
        sendButton = findViewById(R.id.send_button)
        progressBarComments = findViewById(R.id.progressBarComments)
        backButton = findViewById(R.id.back_button)

        recyclerViewComments.layoutManager = LinearLayoutManager(this)

        // 🔹 Pass listener for long-press delete
        commentsAdapter = CommentsAdapter(commentsList, object : CommentsAdapter.CommentListener {
            override fun onCommentLongClick(comment: Comment) {
                confirmDeleteComment(comment)
            }
        })
        recyclerViewComments.adapter = commentsAdapter

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        getPostFromIntent()
        listenForComments()

        sendButton.setOnClickListener {
            addComment()
        }
    }

    private fun getPostFromIntent() {
        postId = intent.getStringExtra("postId") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl")
        val createdAtSeconds = intent.getLongExtra("createdAt", 0L)

        userNameTv.text = userName
        postDescriptionTv.text = description

        if (createdAtSeconds != 0L) {
            val date = Date(createdAtSeconds * 1000)
            postDateTv.text = dateFormatter.format(date)
        }

        if (!imageUrl.isNullOrEmpty()) {
            postImage.visibility = View.VISIBLE
            Glide.with(this).load(imageUrl).into(postImage)
        } else {
            postImage.visibility = View.GONE
        }
    }

    private fun listenForComments() {
        if (postId.isEmpty()) return

        progressBarComments.visibility = View.VISIBLE

        db.collection("community_posts")
            .document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBarComments.visibility = View.GONE
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                commentsList.clear()
                for (doc in snapshot.documents) {
                    val comment = doc.toObject(Comment::class.java)?.copy(commentId = doc.id)
                    if (comment != null) commentsList.add(comment)
                }
                commentsAdapter.updateData(commentsList)

                noCommentsText.visibility =
                    if (commentsList.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun addComment() {
        val text = commentInput.text.toString().trim()
        if (text.isEmpty() || postId.isEmpty()) return

        val currentUser = auth.currentUser ?: return

        val newCommentRef = db.collection("community_posts")
            .document(postId)
            .collection("comments")
            .document()

        val commentData = hashMapOf(
            "commentId" to newCommentRef.id,
            "userId" to currentUser.uid,
            "userName" to (currentUser.displayName ?: "Farmer"),
            "text" to text,
            "createdAt" to Timestamp.now()
        )

        commentInput.setText("")

        newCommentRef.set(commentData)
            .addOnSuccessListener {
                db.collection("community_posts")
                    .document(postId)
                    .update("commentCount", FieldValue.increment(1))
            }
    }

    // 🔹 Show dialog & only allow owner to delete
    private fun confirmDeleteComment(comment: Comment) {
        val currentUser = auth.currentUser ?: return

        // Only allow delete if user owns the comment
        if (comment.userId != currentUser.uid) {
            Toast.makeText(this, "You can delete only your comment", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete comment?")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteComment(comment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteComment(comment: Comment) {
        if (postId.isEmpty() || comment.commentId.isEmpty()) return

        val commentRef = db.collection("community_posts")
            .document(postId)
            .collection("comments")
            .document(comment.commentId)

        commentRef.delete()
            .addOnSuccessListener {
                // Decrement commentCount on post
                db.collection("community_posts")
                    .document(postId)
                    .update(
                        "commentCount",
                        FieldValue.increment(-1)
                    )
            }
    }
}
