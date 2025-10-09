package com.example.agro
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.data.BannerItem

class BannerAdapter(private val banners: List<BannerItem>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    // Inner class for holding the views
    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerLayout: LinearLayout = itemView.findViewById(R.id.bannerLayout) // You'll need to set an ID in item_banner.xml
        val tvTitle: TextView = itemView.findViewById(R.id.tvBannerTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvBannerDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

//

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]

        // Bind the data to the views
        holder.tvTitle.text = banner.title
        holder.tvDescription.text = banner.description

        // FIX: Use setBackgroundColor() instead of setBackgroundResource()
        holder.bannerLayout.setBackgroundColor(banner.backgroundColor)
    }
    override fun getItemCount(): Int = banners.size
}