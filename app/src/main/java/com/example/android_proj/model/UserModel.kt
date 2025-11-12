package com.example.android_proj.model

import com.google.firebase.firestore.Exclude

data class UserModel(
    @get:Exclude var userId: String = "", // Document ID
    val email: String = "",
    val name: String = "",
    val role: String = "user" // Mặc định là 'user'
)