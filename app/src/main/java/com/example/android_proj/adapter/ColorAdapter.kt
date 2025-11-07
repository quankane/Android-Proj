package com.example.android_proj.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.android_proj.databinding.ViewholderColorBinding

class ColorAdapter(
    private val items: ArrayList<String>
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

    override fun onBindViewHolder(holder: ColorAdapter.ViewHolder, position: Int) {
        val color = items[position].toColorInt()
        holder.binding.colorCircle.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        holder.binding.strokeView.visibility = if (selectedPosition == position)
            View.VISIBLE else View.GONE

        holder.binding.root.setOnClickListener {
            if (selectedPosition != position) {
                lastSelectedPosition = selectedPosition
                selectedPosition = position
                if (lastSelectedPosition != -1) notifyItemChanged(lastSelectedPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}