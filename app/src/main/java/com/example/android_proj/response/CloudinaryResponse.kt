package com.example.android_proj.response

data class CloudinaryResponse(
    // Gson sẽ tự động map trường JSON "secure_url" sang biến Kotlin này
    val secure_url: String?
)