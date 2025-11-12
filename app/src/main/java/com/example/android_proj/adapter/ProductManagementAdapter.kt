package com.example.android_proj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.databinding.ItemProductManagementBinding
import com.example.android_proj.model.ItemsModel

class ProductManagementAdapter(
    private var items: MutableList<ItemsModel>,
    private val context: Context,
    private val listener: ProductClickListener
) : RecyclerView.Adapter<ProductManagementAdapter.ViewHolder>() {

    // Interface để xử lý click từ Activity
    interface ProductClickListener {
        fun onEditClick(item: ItemsModel)
        fun onDeleteClick(item: ItemsModel)
    }

    inner class ViewHolder(val binding: ItemProductManagementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductManagementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            productTitle.text = item.title
            productPrice.text = String.format("$%.2f", item.price)

            Glide.with(context)
                .load(item.picUrl.firstOrNull()) // Lấy ảnh đầu tiên
                .into(productImage)

            // Gán sự kiện click cho nút Sửa và Xóa
            editButton.setOnClickListener {
                listener.onEditClick(item)
            }
            deleteButton.setOnClickListener {
                listener.onDeleteClick(item)
            }
        }
    }

    // Cập nhật danh sách khi load
    fun updateData(newItems: List<ItemsModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // Xóa item khỏi list (sau khi xóa trên DB)
    fun removeItem(item: ItemsModel) {
        val position = items.indexOf(item)
        if (position > -1) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}