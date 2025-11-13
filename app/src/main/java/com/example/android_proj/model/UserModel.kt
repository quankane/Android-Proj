package com.example.android_proj.model

import com.google.firebase.firestore.Exclude

data class UserModel(
    @get:Exclude var userId: String = "", // Document ID (sẽ là UID của Auth)
    val email: String = "",
    var name: String = "",      // Sẽ đồng bộ với 'displayName' của Auth
    var phoneNumber: String = "", // THÊM MỚI
    var avatarUrl: String = "",   // THÊM MỚI (sẽ đồng bộ với 'photoUrl' của Auth)
    val role: String = "user"
)