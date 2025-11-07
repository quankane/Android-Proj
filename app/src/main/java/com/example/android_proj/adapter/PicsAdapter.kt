package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.R
import com.example.android_proj.databinding.ViewholderPicsBinding
import com.google.firebase.database.core.Context

class PicsAdapter(
    val items: MutableList<String>,
    private val onImageSelected: (String) -> Unit
) : RecyclerView.Adapter<PicsAdapter.ViewHolder>()
{
    inner class ViewHolder(val binding: ViewholderPicsBinding) :
    RecyclerView.ViewHolder(binding.root)

    //field
    private var selectedPosition = -1
    private var lastSelectedPosition = -1
    @SuppressLint("RestrictedApi")
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PicsAdapter.ViewHolder {
        val binding = ViewholderPicsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PicsAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        Glide.with(holder.itemView.context)
            .load(items[position])
            .into(holder.binding.pic)

        holder.binding.root.setOnClickListener {
            lastSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)
            onImageSelected(items[position])
        }

        if (selectedPosition == position) {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.grey_bg_selected)
        } else {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.grey_bg)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}