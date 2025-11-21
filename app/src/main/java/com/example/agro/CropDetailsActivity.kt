package com.example.agro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agro.Adapter.TopicAdapter
import com.example.agro.databinding.ActivityCropDetailsBinding

class CropDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Get crop name (passed from PracticesFragment)
        val cropName = intent.getStringExtra("crop_name") ?: "Crop Details"
        binding.contentTitle.text = cropName

        // 🔹 Back button
        binding.backButton.setOnClickListener { finish() }

        // 🔹 Sample topic list
        val topicList = arrayListOf(
            "1. Introduction",
            "2. Soil Preparation",
            "3. Climate Requirements",
            "4. Irrigation & Watering",
            "5. Fertilization",
            "6. Pest Management",
            "7. Harvesting",
            "8. Post-Harvest Practices"
        )

        // 🔹 Setup RecyclerView
        val adapter = TopicAdapter(topicList) { topic ->
            // 👉 On topic click → open TopicDetailActivity
            val intent = Intent(this, crop_detail::class.java)
            intent.putExtra("topic_title", topic)
            intent.putExtra("crop_name", cropName)
            startActivity(intent)
        }

        binding.topicRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.topicRecyclerView.adapter = adapter
    }
}
