// com/example/agro/CropDetailsActivity.kt
package com.example.agro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agro.Adapter.TopicAdapter
import com.example.agro.databinding.ActivityCropDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore

class CropDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropDetailsBinding
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var cropId: String = ""
    private var cropName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cropName = intent.getStringExtra("crop_name") ?: "Crop Details"
        cropId = intent.getStringExtra("crop_id") ?: ""

        binding.contentTitle.text = cropName
        binding.backButton.setOnClickListener { finish() }

        loadTopicsFromFirestore()
    }

    private fun loadTopicsFromFirestore() {
        if (cropId.isEmpty()) return

        db.collection("crops")
            .document(cropId)
            .get()
            .addOnSuccessListener { doc ->
                val topicsMap = doc.get("topics") as? Map<String, List<String>> ?: emptyMap()

                // Sort by title (they already start with "1.", "2.", etc.)
                val topicList = topicsMap.keys.sorted()

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
    }
}
