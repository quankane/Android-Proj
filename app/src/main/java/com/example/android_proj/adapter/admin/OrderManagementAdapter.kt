package com.example.android_proj.adapter


import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_proj.databinding.ItemOrderBinding
import com.example.android_proj.model.Order
import java.text.SimpleDateFormat
import java.util.Locale

class OrderManagementAdapter(
    private var orders: MutableList<Order>,
    private val listener: OrderClickListener // Sửa: Đổi Context thành Listener
) : RecyclerView.Adapter<OrderManagementAdapter.OrderViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // --- Cập nhật Interface ---
    interface OrderClickListener {
        fun onUpdateStatusClick(order: Order)
        fun onItemClick(order: Order)
    }

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.orderIdTxt.text = "Mã ĐH: #${order.orderId.take(6)}"
            binding.totalAmountTxt.text = String.format("$%.2f", order.totalAmount)
            binding.dateTxt.text = "Ngày: ${dateFormatter.format(order.orderDate.toDate())}"

            val address = order.shippingAddress
            // Hiển thị thêm email (hoặc ID) của người đặt
            binding.addressTxt.text = "Email: ${order.shippingAddress.phoneNumber} (User ID: ${order.userId.take(5)}...)"

            if (order.items.isNotEmpty()) {
                val firstItem = order.items.first()
                val totalItemsCount = order.items.sumOf { it.quantity }

                if (order.items.size > 1) {
                    binding.firstItemTxt.text = "${firstItem.title} (x${firstItem.quantity}) và ${order.items.size - 1} món khác"
                } else {
                    binding.firstItemTxt.text = "${firstItem.title} (x${firstItem.quantity})"
                }
                binding.totalItemsTxt.text = "$totalItemsCount món"
            } else {
                binding.firstItemTxt.text = "Không có sản phẩm."
                binding.totalItemsTxt.text = ""
            }

            binding.statusTxt.text = order.status
            setStatusColor(binding.statusTxt, order.status)
        }

        private fun setStatusColor(textView: TextView, status: String) {
            val color: Int = when (status) {
                "Thành công" -> Color.parseColor("#4CAF50") // Green
                "Đang xử lý" -> Color.parseColor("#FFC107")   // Amber
                "Hủy bỏ" -> Color.parseColor("#F44336") // Red
                else -> Color.parseColor("#2196F3")        // Blue (Pending)
            }
            textView.setBackgroundColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    // --- Cập nhật onBindViewHolder ---
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)

        // 1. Click vào NÚT TRẠNG THÁI (statusTxt) để CẬP NHẬT
        holder.binding.statusTxt.setOnClickListener {
            listener.onUpdateStatusClick(order)
        }

        // 2. Click vào TOÀN BỘ ITEM (itemView) để XEM CHI TIẾT
        holder.itemView.setOnClickListener {
            listener.onItemClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}