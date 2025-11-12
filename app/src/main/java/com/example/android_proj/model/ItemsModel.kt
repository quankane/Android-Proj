package com.example.android_proj.model

import java.io.Serializable

data class ItemsModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var picUrl: ArrayList<String> = ArrayList(),
    var size: ArrayList<String> = ArrayList(),
    var color: ArrayList<String> = ArrayList(),
    var price: Double = 0.0,
    var oldPrice: Double = 0.0,
    var rating: Double = 0.0,
    var numberInCart: Int = 1,
    var selectedSize: String = "",
    var selectedColor: String = ""
) : Serializable
