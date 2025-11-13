package com.example.android_proj.adapter.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.databinding.ItemProductManagementBinding
import com.example.android_proj.model.ItemsModel
import android.graphics.Paint
import android.view.View

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

            // --- PHẦN CẬP NHẬT ---

            // 1. Hiển thị Old Price (nếu có và lớn hơn price)
            if (item.oldPrice > item.price) {
                productOldPrice.visibility = View.VISIBLE
                productOldPrice.text = String.format("$%.2f", item.oldPrice)
                // Thêm gạch ngang
                productOldPrice.paintFlags = productOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                productOldPrice.visibility = View.GONE
            }

            // 2. Hiển thị Rating (ví dụ: ⭐ 4.5)
            productRating.text = String.format("⭐ %.1f", item.rating)

            // 3. Hiển thị Description
            productDescription.text = item.description

            // --- HẾT PHẦN CẬP NHẬT ---

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