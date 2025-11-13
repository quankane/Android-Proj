// FILE: helper/ManagementWishList.kt (NEW)

package com.example.android_proj.helper

import android.content.Context
import com.example.android_proj.model.ItemsModel
import com.google.firebase.auth.FirebaseAuth

class ManagementWishList(val context: Context) {

    private val tinyDB = TinyDB(context)
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    private val key = "WishList_$userId"

    // Lấy danh sách yêu thích
    fun getListWishlist(): ArrayList<ItemsModel> {
        return tinyDB.getListObject(key) ?: arrayListOf()
    }

    // Lưu danh sách yêu thích
    private fun saveList(list: ArrayList<ItemsModel>) {
        tinyDB.putListObject(key, list)
    }

    // Thêm hoặc Xóa item khỏi WishList
    fun toggleWishlistItem(item: ItemsModel): Boolean {
        val list = getListWishlist()

        // Kiểm tra xem item đã tồn tại chưa
        val exists = list.any { it.title == item.title }

        return if (exists) {
            // Xóa khỏi danh sách
            list.removeAll { it.title == item.title }
            saveList(list)
            false
        } else {
            // Thêm vào danh sách
            list.add(item.copy(numberInCart = 0, selectedSize = "N/A", selectedColor = "N/A"))
            saveList(list)
            true
        }
    }
}