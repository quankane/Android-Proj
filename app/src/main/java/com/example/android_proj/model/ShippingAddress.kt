package com.example.android_proj.model

import java.io.Serializable

data class ShippingAddress(
    val fullName: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val city: String = ""
) : Serializable