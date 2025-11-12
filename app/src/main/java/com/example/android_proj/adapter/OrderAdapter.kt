package com.example.android_proj.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_proj.R
import com.example.android_proj.databinding.ItemOrderBinding
import com.example.android_proj.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private var orders: MutableList<Order>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            // Lấy ID đơn hàng (sử dụng 6 ký tự đầu nếu là ID Firestore)
            binding.orderIdTxt.text = "Mã đơn hàng: #${order.orderId.take(6)}"
            binding.totalAmountTxt.text = String.format("$%.2f", order.totalAmount)
            binding.dateTxt.text = "Ngày đặt: ${dateFormatter.format(order.orderDate.toDate())}"

            // 1. Hiển thị Địa chỉ
            val address = order.shippingAddress
            binding.addressTxt.text = "Địa chỉ: ${address?.streetAddress}, ${address?.city} - SĐT: ${address?.phoneNumber}"

            // 2. Hiển thị Sản phẩm đầu tiên và tổng số lượng
            if (order.items.isNotEmpty()) {
                val firstItem = order.items.first()
                val totalItemsCount = order.items.sumOf { it.quantity }

                if (order.items.size > 1) {
                    binding.firstItemTxt.text = "Sản phẩm: ${firstItem.title} (x${firstItem.quantity}) và ${order.items.size - 1} món khác"
                } else {
                    binding.firstItemTxt.text = "Sản phẩm: ${firstItem.title} (x${firstItem.quantity})"
                }
                binding.totalItemsTxt.text = "$totalItemsCount món"
            } else {
                binding.firstItemTxt.text = "Không có sản phẩm."
                binding.totalItemsTxt.text = ""
            }

            // Thiết lập trạng thái và màu sắc
            binding.statusTxt.text = order.status
            setStatusColor(binding.statusTxt, order.status)
        }

        // Cập nhật màu sắc cho trạng thái (Bạn cần định nghĩa màu trong colors.xml)
        private fun setStatusColor(textView: TextView, status: String) {
            val color: Int = when (status) {
                "Delivered" -> Color.parseColor("#4CAF50") // Green
                "Shipped" -> Color.parseColor("#FFC107")   // Amber
                "Cancelled" -> Color.parseColor("#F44336") // Red
                else -> Color.parseColor("#2196F3")        // Blue (Pending)
            }
            textView.setBackgroundColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}