package com.example.agro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.Crop
// ACTION REQUIRED: Ensure you have View Binding enabled in your build.gradle for this import to work.
import com.example.agro.databinding.ItemCropBinding
// import com.bumptech.glide.Glide // Recommended for image loading

///**
// * Adapter for displaying a grid of Crop items in a RecyclerView.
// *
// * @param crops The list of Crop objects to display.
// * @param itemClickListener A lambda function to handle clicks on individual crop items.
// */
//class CropAdapter(
//    private val crops: List<Crop>,
//    private val itemClickListener: (Crop) -> Unit
//) : RecyclerView.Adapter<CropAdapter.CropViewHolder>() {
//
//    // 1. ViewHolder: Caches the views and binds the data for a single item.
//    inner class CropViewHolder(private val binding: ItemCropBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(crop: Crop) {
//            // Set the crop name
//            binding.tvCropName.text = crop.name
//
//            // 2. Image Loading (Placeholder for Glide or Picasso implementation)
//            // Example using Glide:
//            // Glide.with(binding.ivCropImage.context)
//            //    .load(crop.imageUrl)
//            //    .placeholder(R.drawable.img_placeholder)
//            //    .into(binding.ivCropImage)
//
//            // To test, you might set a static placeholder image for now:
//            // binding.ivCropImage.setImageResource(R.drawable.img_placeholder)
//
//            // 3. Click Listener: Executes the lambda when the card is clicked.
//            binding.root.setOnClickListener {
//                itemClickListener(crop)
//            }
//        }
//    }
//
//    // 2. onCreateViewHolder: Inflates the item layout and creates the ViewHolder.
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
//        val binding = ItemCropBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CropViewHolder(binding)
//    }
//
//    // 3. onBindViewHolder: Binds the data at a specific position to the ViewHolder's views.
//    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
//        holder.bind(crops[position])
//    }
//
//    // 4. getItemCount: Returns the total number of items in the list.
//    override fun getItemCount(): Int = crops.size
//}


/**
 * Adapter for displaying a grid of Crop items in a RecyclerView.
 *
 * @param crops The list of Crop objects to display.
 * @param itemClickListener A lambda function to handle clicks on individual crop items.
 */
class CropAdapter(
    private val crops: List<Crop>,
    private val itemClickListener: (Crop) -> Unit
) : RecyclerView.Adapter<CropAdapter.CropViewHolder>() {

    inner class CropViewHolder(private val binding: ItemCropBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(crop: Crop) {
            // Set crop name
            binding.tvCropName.text = crop.name

            // Load image (use static placeholder for now)
            // Glide.with(binding.ivCropImage.context)
            //     .load(crop.imageUrl)
            //     .placeholder(R.drawable.img_placeholder)
            //     .into(binding.ivCropImage)
            // OR:
            // binding.ivCropImage.setImageResource(R.drawable.img_placeholder)

            // Handle card click
            binding.root.setOnClickListener {
                itemClickListener(crop)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        val binding = ItemCropBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CropViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        holder.bind(crops[position])
    }

    override fun getItemCount(): Int = crops.size
}
