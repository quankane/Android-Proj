package com.example.android_proj.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android_proj.R
import com.example.android_proj.databinding.ViewholderSizeBinding

class SizeAdapter(val items: MutableList<String>) :
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

    override fun onBindViewHolder(holder: SizeAdapter.ViewHolder, position: Int) {
        holder.binding.sizeTxt.text = items[position]

        holder.binding.root.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
        }

        if (selectedPosition == position) {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.blue_bg)
            holder.binding.sizeTxt.setTextColor(holder.itemView.context.resources.getColor(R.color.white))
        } else {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.stroke_bg_blue)
            holder.binding.sizeTxt.setTextColor(holder.itemView.context.resources.getColor(R.color.black))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}