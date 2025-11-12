package com.example.android_proj.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_proj.helper.ManagementWishList
import com.example.android_proj.model.BrandModel
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.model.SliderModel
// --- THAY ĐỔI IMPORT ---
import com.google.firebase.firestore.FirebaseFirestore
// (Xóa các import của Realtime Database)

class MainRepository(private val context: Context) {

    // --- THAY ĐỔI DB ---
    private val db = FirebaseFirestore.getInstance() // <-- ĐỔI SANG FIRESTORE
    private val managementWishList = ManagementWishList(context)

    // Các LiveData không đổi
    var _populars = MutableLiveData<MutableList<ItemsModel>>()
    private val _brands = MutableLiveData<MutableList<BrandModel>>()
    private val _banners = MutableLiveData<List<SliderModel>>()

    val banners: LiveData<List<SliderModel>> get() = _banners
    val brands: LiveData<MutableList<BrandModel>> get() = _brands
    val populars: LiveData<MutableList<ItemsModel>> get() = _populars

    // ------------------------------------------
    // --- PHƯƠNG THỨC QUẢN LÝ WISHLIST (Giữ nguyên) ---
    // ------------------------------------------

    fun getWishlistItems(): ArrayList<ItemsModel> {
        return managementWishList.getListWishlist()
    }

    fun toggleWishlistItem(item: ItemsModel): Boolean {
        return managementWishList.toggleWishlistItem(item)
    }

    // ------------------------------------------
    // --- CÁC HÀM TẢI DỮ LIỆU ĐÃ SỬA SANG FIRESTORE ---
    // ------------------------------------------

    fun loadBrands() {
        // Lấy từ collection "categories" (hoặc tên bạn đặt trong Firestore)
        db.collection("Category")
            .get()
            .addOnSuccessListener { documents ->
                // Tự động chuyển đổi toàn bộ list
                val list = documents.toObjects(BrandModel::class.java).toMutableList()
                _brands.value = list
            }
            .addOnFailureListener {
                // Xử lý lỗi
                _brands.value = mutableListOf() // Trả về list rỗng nếu lỗi
            }
    }

    fun loadBanners() {
        // Lấy từ collection "banners"
        db.collection("Banner")
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(SliderModel::class.java)
                _banners.value = list
            }
            .addOnFailureListener {
                // Xử lý lỗi
                _banners.value = listOf()
            }
    }

    fun loadPopulars() {
        // Lấy từ collection "items"
        db.collection("Items")
            .get()
            .addOnSuccessListener { documents ->
                val list = documents.toObjects(ItemsModel::class.java).toMutableList()

                // --- QUAN TRỌNG ---
                // Gán ID document vào trường 'id' của mỗi item
                // (Giống như cách làm trong MyOrdersActivity)
                for (i in list.indices) {
                    list[i].id = documents.documents[i].id
                }

                _populars.value = list
            }
            .addOnFailureListener {
                // Xử lý lỗi
                _populars.value = mutableListOf()
            }
    }
}