package com.example.android_proj.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class AddressModel(
    @get:Exclude var addressId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val streetAddress: String = "",
    val city: String = "",
    val isDefault: Boolean = false
) : Serializable