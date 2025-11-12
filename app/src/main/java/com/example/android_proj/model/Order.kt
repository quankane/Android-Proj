package com.example.android_proj.model

import com.google.firebase.Timestamp
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Order(
    @get:Exclude var orderId: String = "", // Document ID
    val userId: String = "",
    val orderDate: Timestamp = Timestamp.now(),
    var status: String = "Pending",
    val items: List<OrderItem> = listOf(),
    val shippingAddress: ShippingAddress = ShippingAddress(),
    val paymentMethod: String = "Cash on Delivery",
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0
)