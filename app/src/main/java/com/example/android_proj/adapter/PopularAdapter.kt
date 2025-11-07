package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.android_proj.databinding.ViewholderRecommendationBinding
import com.example.android_proj.model.ItemModel

class PopularAdapter(
    private val items : MutableList<ItemModel>
) : RecyclerView.Adapter<PopularAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ViewholderRecommendationBinding)
        : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<ItemModel>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ViewholderRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]

        holder.binding.apply {
            titleTxt.text = item.title
            priceTxt.text = "$${item.price}"
            ratingTxt.text = item.rating.toString()

            Glide.with(holder.itemView.context)
                .load(item.picUrl.firstOrNull())
                .apply(RequestOptions().transform(CenterCrop()))
                .into(pic)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}