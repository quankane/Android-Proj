package com.example.android_proj.model

import java.io.Serializable

data class OrderItem(
    val id: String = "", // ID của sản phẩm (từ ItemsModel)
    val title: String = "",
    val picUrl: String = "", // Lấy URL đầu tiên của picUrl
    val priceAtPurchase: Double = 0.0,
    val quantity: Int = 0
) : Serializable