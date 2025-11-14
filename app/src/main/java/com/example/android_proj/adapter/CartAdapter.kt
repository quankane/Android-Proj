package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.databinding.ViewholderCartBinding
import com.example.android_proj.model.CartItem
import java.text.DecimalFormat

class CartAdapter(
    private var list: MutableList<CartItem>,
    private val context: Context,
    private val listener: CartItemListener
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    // Interface để gửi sự kiện click về Activity
    interface CartItemListener {
        fun onPlusClicked(item: CartItem)
        fun onMinusClicked(item: CartItem)
    }

    private val formatter = DecimalFormat("###,###,###.##")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderCartBinding.inflate(LayoutInflater.from(context),
            parent,
            false)
        return ViewHolder(binding)
    }

    @SuppressLint("UseKtx")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.feeEachItemTxt.text = "$${formatter.format(item.price)}"
        holder.binding.totalEachItem.text = "$${formatter.format(item.price * item.numberInCart)}"
        holder.binding.numberItemTxt.text = item.numberInCart.toString()

        holder.binding.sizeTxt.text = "Size: ${item.selectedSize}"
        holder.binding.colorTxt.text = "Color: ${item.selectedColor}"

        // Tải ảnh
        Glide.with(context)
            .load(item.picUrl)
            .into(holder.binding.pic)

        // Xử lý màu sắc
        try {
            val color = Color.parseColor(item.selectedColor)
            val drawable = holder.binding.colorCircle.background as GradientDrawable
            drawable.setColor(color)
        } catch (e: Exception) {
            // Xử lý nếu mã màu không hợp lệ
        }

        // Gửi sự kiện click về Activity
        holder.binding.plusCartBtn.setOnClickListener {
            listener.onPlusClicked(item)
        }

        holder.binding.minusCartBtn.setOnClickListener {
            listener.onMinusClicked(item)
        }
    }

    override fun getItemCount(): Int = list.size

    // Hàm cập nhật danh sách từ Firestore
    fun updateList(newList: List<CartItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ViewholderCartBinding) : RecyclerView.ViewHolder(binding.root)
}