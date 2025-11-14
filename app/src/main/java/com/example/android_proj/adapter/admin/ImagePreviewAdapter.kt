package com.example.android_proj.adapter.admin

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.R

class ImagePreviewAdapter(
    private val context: Context,
    private val onRemoveClicked: (Any) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {

    // Danh sách này có thể" chứa String (URL) hoặc Uri (ảnh mới)
    private val imageSources = ArrayList<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val source = imageSources[position]
        holder.bind(source)
    }

    override fun getItemCount(): Int = imageSources.size

    fun addImage(source: Any) {
        imageSources.add(source)
        notifyItemInserted(imageSources.size - 1)
    }

    fun removeImage(source: Any) {
        val position = imageSources.indexOf(source)
        if (position != -1) {
            imageSources.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setImages(sources: List<Any>) {
        imageSources.clear()
        imageSources.addAll(sources)
        notifyDataSetChanged()
    }

    fun getUris(): List<Uri> {
        return imageSources.filterIsInstance<Uri>()
    }

    fun getUrls(): List<String> {
        return imageSources.filterIsInstance<String>()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imagePreview)
        private val removeButton: ImageView = itemView.findViewById(R.id.imageRemoveBtn)

        fun bind(source: Any) {
            Glide.with(context)
                .load(source) // Glide tự động xử lý cả Uri và String URL
                .placeholder(R.drawable.blue_bg)
                .centerCrop()
                .into(imageView)

            removeButton.setOnClickListener {
                onRemoveClicked(source)
            }
        }
    }
}