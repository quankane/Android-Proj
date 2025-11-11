package com.example.android_proj.model

import com.google.firebase.Timestamp

data class Order(
    // ID Firestore Document
    val orderId: String = "",

    // Liên kết với người dùng
    val userId: String = "",

    // Tổng hợp đơn hàng
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Pending", // Trạng thái: Pending, Shipped, Delivered, Cancelled

    // Thời gian và Địa chỉ
    val orderDate: Timestamp = Timestamp.now(),
    val shippingAddress: ShippingAddress? = null
)