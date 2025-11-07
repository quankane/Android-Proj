package com.example.android_proj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.android_proj.R
import com.example.android_proj.model.SliderModel

// ĐÃ LOẠI BỎ 'viewPager2' KHỎI CONSTRUCTOR
class SliderAdapter(
    private val sliderItems: List<SliderModel>
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>()
{
    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById<ImageView>(R.id.imageSlide)

        fun setImage(sliderItems: SliderModel, context: Context) {
            val requestOption = RequestOptions().transform(CenterInside())

            Glide.with(context)
                .load(sliderItems.url)
                .apply(requestOption)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderAdapter.SliderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slider_item_container, parent, false)

        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderAdapter.SliderViewHolder, position: Int) {
        // CHỈ GỌI HÀM SET IMAGE. ĐÃ LOẠI BỎ LOGIC TỰ ĐỘNG CUỘN
        holder.setImage(sliderItems[position], holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return sliderItems.size
    }

}