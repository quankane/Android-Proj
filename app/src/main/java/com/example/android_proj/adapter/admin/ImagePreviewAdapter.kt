package com.example.android_proj.adapter.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.R

class ImagePreviewAdapter(
    private val context: Context,
    private val imageList: MutableList<String>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imagePreview)
        val removeButton: ImageView = itemView.findViewById(R.id.imageRemoveBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]

        Glide.with(context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.removeButton.setOnClickListener {
            onRemoveClick(imageUrl)
        }
    }

    override fun getItemCount(): Int = imageList.size
}