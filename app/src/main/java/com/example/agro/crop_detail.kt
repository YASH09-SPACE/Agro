// com/example/agro/crop_detail.kt
package com.example.agro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agro.Adapter.ParagraphAdapter
import com.example.agro.databinding.ActivityCropDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class crop_detail : AppCompatActivity() {

    private lateinit var binding: ActivityCropDetailBinding
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var cropId: String = ""
    private var cropName: String = ""
    private var topicList: ArrayList<String> = arrayListOf()
    private var currentIndex: Int = 0

    private lateinit var paragraphAdapter: ParagraphAdapter
    private var topicsMap: Map<String, List<String>> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cropId = intent.getStringExtra("crop_id") ?: ""
        cropName = intent.getStringExtra("crop_name") ?: ""
        topicList = intent.getStringArrayListExtra("topic_list") ?: arrayListOf()
        currentIndex = intent.getIntExtra("topic_index", 0)

        paragraphAdapter = ParagraphAdapter()
        binding.paragraphRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@crop_detail)
            adapter = paragraphAdapter
        }

        binding.closeButton.setOnClickListener { finish() }

        binding.nextButton.setOnClickListener {
            if (currentIndex < topicList.size - 1) {
                currentIndex++
                showCurrentTopic()
            }
        }

        binding.previousButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                showCurrentTopic()
            }
        }

        loadTopicsFromFirestore()
    }

    private fun loadTopicsFromFirestore() {
        if (cropId.isEmpty()) return

        db.collection("crops")
            .document(cropId)
            .get()
            .addOnSuccessListener { doc ->
                topicsMap = doc.get("topics") as? Map<String, List<String>> ?: emptyMap()
                showCurrentTopic()
            }
    }

    private fun showCurrentTopic() {
        if (topicList.isEmpty()) return

        val title = topicList[currentIndex]
        binding.contentTitle.text = title
        binding.pageCounter.text = "${currentIndex + 1} of ${topicList.size}"

        val paragraphs = topicsMap[title] ?: listOf(
            "Content not available yet for $title."
        )
        paragraphAdapter.submitList(paragraphs)
    }
}
