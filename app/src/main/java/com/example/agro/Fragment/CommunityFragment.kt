package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.PostAdapter
import com.example.agro.R
import com.example.agro.create_post
import com.example.agro.data.Post
import com.example.agro.post_detail
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue

class CommunityFragment : Fragment() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var etCategorySearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var fabAddPost: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val allPosts = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts)
        etCategorySearch = view.findViewById(R.id.etCategorySearch)
        btnBack = view.findViewById(R.id.btnBack)
        fabAddPost = view.findViewById(R.id.fabAddPost)
        progressBar = view.findViewById(R.id.progressBar)

        postAdapter = PostAdapter(allPosts, object : PostAdapter.PostListener {
            override fun onPostClicked(post: Post) {
                openPostDetail(post)
            }

            override fun onLikeClicked(post: Post) {
                toggleLike(post)
            }

            override fun onCommentClicked(post: Post) {
                openPostDetail(post)
            }
        })

        recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            setHasFixedSize(false)
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        fabAddPost.setOnClickListener {
            startActivity(Intent(requireContext(), create_post::class.java))
        }

        etCategorySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterPostList(s?.toString().orEmpty())
            }
        })

        loadPostsFromFirestore()

        return view
    }

    private fun loadPostsFromFirestore() {
        progressBar.visibility = View.VISIBLE

        db.collection("community_posts")
            .orderBy("likeCount", Query.Direction.DESCENDING)
            .orderBy("commentCount", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE

                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val currentUid = auth.currentUser?.uid
                allPosts.clear()

                for (doc in snapshot.documents) {
                    val post = doc.toObject(Post::class.java)?.copy(postId = doc.id)
                    if (post != null) {
                        if (currentUid != null) {
                            post.isLikedByCurrentUser = post.likedBy.contains(currentUid)
                        }
                        allPosts.add(post)
                    }
                }

                postAdapter.updateData(allPosts)
            }
    }

    private fun filterPostList(query: String) {
        if (query.isBlank()) {
            postAdapter.updateData(allPosts)
            return
        }
        val q = query.trim().lowercase()
        val filtered = allPosts.filter { post ->
            post.description.lowercase().contains(q) ||
                    post.userName.lowercase().contains(q)
        }
        postAdapter.updateData(filtered)
    }

    private fun toggleLike(post: Post) {
        val uid = auth.currentUser?.uid ?: return      // 🔹 UID, not token
        if (post.postId.isEmpty()) return

        val postRef = db.collection("community_posts").document(post.postId)

        // optimistic UI
        post.isLikedByCurrentUser = !post.isLikedByCurrentUser
        postAdapter.notifyDataSetChanged()

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
            var likeCount = snapshot.getLong("likeCount") ?: 0L

            val updates = mutableMapOf<String, Any>()

            if (likedBy.contains(uid)) {
                likeCount = (likeCount - 1).coerceAtLeast(0)
                updates["likeCount"] = likeCount
                updates["likedBy"] = com.google.firebase.firestore.FieldValue.arrayRemove(uid)
            } else {
                likeCount += 1
                updates["likeCount"] = likeCount
                updates["likedBy"] = com.google.firebase.firestore.FieldValue.arrayUnion(uid)
            }

            transaction.update(postRef, updates)
        }
    }


    private fun openPostDetail(post: Post) {
        val intent = Intent(requireContext(), post_detail::class.java).apply {
            putExtra("postId", post.postId)
            putExtra("userName", post.userName)
            putExtra("description", post.description)
            putExtra("imageUrl", post.imageUrl)
            putExtra("createdAt", post.createdAt?.seconds ?: 0L)
        }
        startActivity(intent)
    }

    companion object {
        fun newInstance(): CommunityFragment = CommunityFragment()
    }
}
