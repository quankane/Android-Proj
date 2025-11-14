package com.example.android_proj.model

import com.google.firebase.firestore.Exclude
import java.io.Serializable

// Model để lưu trữ một mục trong giỏ hàng trên Firestore
data class CartItem(
    @get:Exclude var documentId: String = "", // Dùng để lưu ID của document
    val itemId: String = "", // Liên kết với ItemsModel.id
    val title: String = "",
    val picUrl: String = "", // Chỉ lưu 1 ảnh đại diện
    val price: Double = 0.0,
    var numberInCart: Int = 1,
    val selectedSize: String = "",
    val selectedColor: String = ""
) : Serializable