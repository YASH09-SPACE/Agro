// com/example/agro/CropDetailsActivity.kt
package com.example.agro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.agro.Adapter.TopicAdapter
import com.example.agro.databinding.ActivityCropDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore

class CropDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropDetailsBinding
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var cropId: String = ""
    private var cropName: String = ""
    private var cropImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cropId = intent.getStringExtra("crop_id") ?: ""
        cropName = intent.getStringExtra("crop_name") ?: "Crop Details"
        cropImageUrl = intent.getStringExtra("crop_image") ?: ""

        // set titles
        binding.headerTitle.text = cropName

        // load image in header
        Glide.with(this)
            .load(cropImageUrl)
            .placeholder(R.drawable.ic_date)
            .into(binding.ivCropHeader)

        binding.backButton.setOnClickListener { finish() }

        loadTopicsFromFirestore()
    }

    private fun loadTopicsFromFirestore() {
        if (cropId.isEmpty()) return

        db.collection("crops")
            .document(cropId)
            .get()
            .addOnSuccessListener { doc ->
                // topics is a Map<String, List<String>>
                val topicsMap = doc.get("topics") as? Map<String, List<String>> ?: emptyMap()

                if (topicsMap.isEmpty()) {
                    // Optional: log or toast to debug
                    // Toast.makeText(this, "No topics found in Firestore", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 🔹 Make a stable order:
                // If keys start with "1. ", "2. " etc → sort by number
                // Otherwise → fallback to A–Z alphabetical
                val topicList = topicsMap.keys.sortedWith(compareBy { key ->
                    key.substringBefore(".").trim().toIntOrNull() ?: Int.MAX_VALUE
                })

                val adapter = TopicAdapter(topicList) { topic ->
                    val index = topicList.indexOf(topic)

                    val intent = Intent(this, crop_detail::class.java)
                    intent.putExtra("crop_id", cropId)
                    intent.putExtra("crop_name", cropName)
                    intent.putExtra("topic_index", index)
                    intent.putStringArrayListExtra("topic_list", ArrayList(topicList))
                    startActivity(intent)
                }

                binding.topicRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.topicRecyclerView.adapter = adapter
            }
            .addOnFailureListener {
                it.printStackTrace()
                // Optional: Toast.makeText(this, "Failed to load topics", Toast.LENGTH_SHORT).show()
            }
    }


}
