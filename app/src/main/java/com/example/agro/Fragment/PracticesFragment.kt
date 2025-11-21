package com.example.agro.Fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.CropAdapter
import com.example.agro.CropDetailsActivity
import com.example.agro.data.Crop
import com.example.agro.databinding.FragmentPracticesBinding

class PracticesFragment : Fragment() {

    private var _binding: FragmentPracticesBinding? = null
    private val binding get() = _binding!!
    private val TAG = "PracticesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticesBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        val cropList = generateSampleCrops()

        val adapter = CropAdapter(cropList) { crop ->
            // On crop click → open CropDetailsActivity
            val intent = Intent(requireContext(), CropDetailsActivity::class.java)
            intent.putExtra("crop_name", crop.name)
            intent.putExtra("crop_image", crop.imageUrl)
            startActivity(intent)
        }

        binding.rvCropGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }
    }

    private fun generateSampleCrops(): List<Crop> {
        return listOf(
            Crop("Date Palm", "url_date_palm_img"),
            Crop("Coffee", "url_coffee_img"),
            Crop("Rubber", "url_rubber_img"),
            Crop("Coconut", "url_coconut_img"),
            Crop("Cinnamon", "url_cinnamon_img"),
            Crop("Cocoa", "url_cocoa_img"),
            Crop("Cardamom", "url_cardamom_img"),
            Crop("Tea", "url_tea_img")
        )
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
