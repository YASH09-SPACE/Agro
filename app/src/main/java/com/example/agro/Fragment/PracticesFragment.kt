// com/example/agro/Fragment/PracticesFragment.kt
package com.example.agro.Fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.CropAdapter
import com.example.agro.CropDetailsActivity
import com.example.agro.data.Crop
import com.example.agro.databinding.FragmentPracticesBinding
import com.google.firebase.firestore.FirebaseFirestore

class PracticesFragment : Fragment() {

    private var _binding: FragmentPracticesBinding? = null
    private val binding get() = _binding!!

    private lateinit var cropAdapter: CropAdapter
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticesBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupSearch()
        loadCropsFromFirestore()

        return binding.root
    }

    private fun setupRecyclerView() {
        cropAdapter = CropAdapter { crop ->
            val intent = Intent(requireContext(), CropDetailsActivity::class.java)
            intent.putExtra("crop_id", crop.id)
            intent.putExtra("crop_name", crop.name)
            intent.putExtra("crop_image", crop.imageUrl)
            startActivity(intent)
        }

        binding.rvCropGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = cropAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }
    }

    private fun setupSearch() {
        binding.etSearchCrop.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            cropAdapter.filterCrops(query)
        }
    }

    private fun loadCropsFromFirestore() {
        db.collection("crops")
            .get()
            .addOnSuccessListener { snapshot ->
                val cropList = snapshot.documents.map { doc ->
                    Crop(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }

                cropAdapter.setData(cropList)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load crops", Toast.LENGTH_SHORT).show()
            }
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount
            val spacingPx = (spacing * view.context.resources.displayMetrics.density).toInt()

            if (includeEdge) {
                outRect.left = spacingPx - column * spacingPx / spanCount
                outRect.right = (column + 1) * spacingPx / spanCount
                if (position < spanCount) {
                    outRect.top = spacingPx
                }
                outRect.bottom = spacingPx
            } else {
                outRect.left = column * spacingPx / spanCount
                outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
                if (position >= spanCount) {
                    outRect.top = spacingPx
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
