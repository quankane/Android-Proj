package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.android_proj.databinding.ViewholderColorBinding

class ColorAdapter(
    private val items: ArrayList<String>,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>()
{

    inner class ViewHolder(val binding: ViewholderColorBinding) :
    RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ColorAdapter.ViewHolder {
        val binding = ViewholderColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val colorString = items[position] // Giá trị màu sắc thực tế (ví dụ: "#FF0000")

        // 2. Chuyển đổi và thiết lập màu cho View
        val color = colorString.toColorInt()
        holder.binding.colorCircle.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        // 3. Logic highlight
        holder.binding.strokeView.visibility = if (selectedPosition == position)
            View.VISIBLE else View.GONE

        holder.binding.root.setOnClickListener {
            if (selectedPosition != position) {

                // Cập nhật vị trí đã chọn
                lastSelectedPosition = selectedPosition
                selectedPosition = position

                // Gọi callback để thông báo màu đã chọn cho DetailActivity
                onColorSelected(colorString) // <-- THÊM DÒNG NÀY

                // Cập nhật UI
                if (lastSelectedPosition != -1) notifyItemChanged(lastSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}