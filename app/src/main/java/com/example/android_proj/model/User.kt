package com.example.android_proj.model

data class User(
    val email: String? = null,
    val role: String = "user", // Mặc định là 'user'
    val createdAt: com.google.firebase.Timestamp? = null
    // Bạn có thể thêm các trường khác như displayName, photoUrl...
)