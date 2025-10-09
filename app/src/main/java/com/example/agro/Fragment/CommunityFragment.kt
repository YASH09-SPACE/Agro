package com.example.agro.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agro.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import com.example.agro.PostAdapter
import com.example.agro.data.Post

class CommunityFragment : Fragment() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var etCategorySearch: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var fabAddPost: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        // --- Initialize Views ---
        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts)
        etCategorySearch = view.findViewById(R.id.etCategorySearch)
        btnBack = view.findViewById(R.id.btnBack)
        fabAddPost = view.findViewById(R.id.fabAddPost)

        // --- Prepare Dummy Data ---
        val postList = generateDummyPosts()

        // --- Setup Adapter ---
        postAdapter = PostAdapter(postList)
        recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            setHasFixedSize(false)
        }

        // --- Optional: Search functionality ---
        etCategorySearch.setOnEditorActionListener { v, actionId, event ->
            val query = v.text.toString()
            filterPostList(query)
            true
        }

        // --- Optional: Back Button ---
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // --- Optional: FAB (Add Post) ---
        fabAddPost.setOnClickListener {
            // TODO: Add logic for new post creation (e.g., open AddPostFragment)
        }

        return view
    }

    // Dummy Posts
    private fun generateDummyPosts(): MutableList<Post> {
        return mutableListOf(
            Post(
                postId = "p1",
                userName = "Vishwanath Mishra",
                postDate = "25 Sep 2020",
                description = "Diseased leaves of zucchini growing in the garden. High quality photo",
                imageUrl = "img_sample_disease"
            ),
            Post(
                postId = "p2",
                userName = "Amit Singh",
                postDate = "10 Oct 2020",
                description = "My new hydroponic setup for tomatoes is flourishing! Ask me anything.",
                imageUrl = "img_sample_disease"
            ),
            Post(
                postId = "p3",
                userName = "Priya Sharma",
                postDate = "01 Nov 2020",
                description = "I think my rose plant has a fungal infection. Any organic tonic recommendations?",
                imageUrl = "img_sample_disease"
            ),
            Post(
                postId = "p4",
                userName = "Vishwanath Mishra",
                postDate = "15 Nov 2020",
                description = "Another angle of the diseased leaves. Trying to confirm the specific pest. #gardening #pests",
                imageUrl = "img_sample_disease"
            )
        )
    }

    // Optional: Search filter (dummy)
    private fun filterPostList(query: String) {
        // You can later connect this to actual Firestore filtering or local filtering
        // Example:
        // val filteredList = originalList.filter { it.description.contains(query, ignoreCase = true) }
        // postAdapter.updateData(filteredList)
    }

    companion object {
        fun newInstance(): CommunityFragment = CommunityFragment()
    }
}
