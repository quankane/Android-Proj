package com.example.android_proj.model

data class OrderItem(
    val itemId: String = "",
    val title: String = "",
    val picUrl: String = "",
    val size: String = "",
    val color: String = "",
    val quantity: Int = 0,
    val priceAtPurchase: Double = 0.0
)