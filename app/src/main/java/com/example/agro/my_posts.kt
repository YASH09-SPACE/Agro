package com.example.agro

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class my_posts : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoMyPosts: TextView
    private lateinit var btnBack: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var adapter: PostAdapter
    private val myPosts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_posts)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewMyPosts)
        tvNoMyPosts = findViewById(R.id.tvNoMyPosts)
        btnBack = findViewById(R.id.btnBackMyPosts)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = PostAdapter(myPosts, object : PostAdapter.PostListener {
            override fun onPostClicked(post: Post) {
                showPostOptions(post)
            }

            override fun onLikeClicked(post: Post) {
                // You may choose to not allow like here, or reuse same toggleLike logic
            }

            override fun onCommentClicked(post: Post) {
                val intent = Intent(this@my_posts, post_detail::class.java).apply {
                    putExtra("postId", post.postId)
                    putExtra("userName", post.userName)
                    putExtra("description", post.description)
                    putExtra("imageUrl", post.imageUrl)
                    putExtra("createdAt", post.createdAt?.seconds ?: 0L)
                }
                startActivity(intent)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadMyPosts()
    }

    private fun loadMyPosts() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("community_posts")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                myPosts.clear()
                for (doc in snapshot.documents) {
                    val post = doc.toObject(Post::class.java)?.copy(postId = doc.id)
                    if (post != null) myPosts.add(post)
                }

                adapter.updateData(myPosts)

                tvNoMyPosts.visibility =
                    if (myPosts.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun showPostOptions(post: Post) {
        val options = arrayOf("View", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Post options")
            .setItems(options) { _: DialogInterface, which: Int ->
                when (which) {
                    0 -> {
                        // View
                        val intent = Intent(this, post_detail::class.java).apply {
                            putExtra("postId", post.postId)
                            putExtra("userName", post.userName)
                            putExtra("description", post.description)
                            putExtra("imageUrl", post.imageUrl)
                            putExtra("createdAt", post.createdAt?.seconds ?: 0L)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        // Delete
                        confirmDeletePost(post)
                    }
                }
            }
            .show()
    }

    private fun confirmDeletePost(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Delete post?")
            .setMessage("This will also delete its comments.")
            .setPositiveButton("Delete") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(post: Post) {
        if (post.postId.isEmpty()) return

        val postRef = db.collection("community_posts").document(post.postId)
        val commentsRef = postRef.collection("comments")

        // First fetch all comments
        commentsRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()

                // Delete all comments
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Delete the post document itself
                batch.delete(postRef)

                // Commit batch
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load comments: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
