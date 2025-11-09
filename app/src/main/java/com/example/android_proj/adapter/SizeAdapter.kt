package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android_proj.R
import com.example.android_proj.databinding.ViewholderSizeBinding

class SizeAdapter(
    val items: MutableList<String>,
    private val onSizeSelected: (String) -> Unit // <-- Callback
) :
    RecyclerView.Adapter<SizeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewholderSizeBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1
    private var lastSelectedPosition = -1


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SizeAdapter.ViewHolder {
        val binding = ViewholderSizeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SizeAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        // Lấy giá trị size (ví dụ: "S", "M", "L")
        val sizeString = items[position]
        holder.binding.sizeTxt.text = sizeString

        holder.binding.root.setOnClickListener {
            if (selectedPosition != position) { // Chỉ xử lý nếu chọn mục khác

                // Cập nhật vị trí đã chọn
                lastSelectedPosition = selectedPosition
                selectedPosition = position

                // THÊM: Gọi callback để thông báo Size đã chọn cho DetailActivity
                onSizeSelected(sizeString)

                // Cập nhật UI
                if (lastSelectedPosition != -1) notifyItemChanged(lastSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
        }

        // Logic highlight
        if (selectedPosition == position) {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.blue_bg)
            // Đảm bảo sử dụng context.getColor cho API 23+ hoặc ContextCompat.getColor
            holder.binding.sizeTxt.setTextColor(holder.itemView.context.getColor(R.color.white))
        } else {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.stroke_bg_blue)
            holder.binding.sizeTxt.setTextColor(holder.itemView.context.getColor(R.color.black))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}