package com.example.android_proj.repository

import android.content.Context // Cần import Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_proj.helper.ManagementWishList // Import lớp WishList helper
import com.example.android_proj.model.BrandModel
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.model.SliderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// CHÚ Ý: Cần nhận Context trong constructor
class MainRepository(private val context: Context) {

    // --- Khai báo thuộc tính ---
    private val firebaseDatabase = FirebaseDatabase.getInstance();
    private val managementWishList = ManagementWishList(context) // <-- Khởi tạo WishList helper

    var _populars = MutableLiveData<MutableList<ItemsModel>>()
    private val _brands = MutableLiveData<MutableList<BrandModel>>()
    private val _banners = MutableLiveData<List<SliderModel>>()

    val banners: LiveData<List<SliderModel>> get() = _banners
    val brands: LiveData<MutableList<BrandModel>> get() = _brands
    val populars: LiveData<MutableList<ItemsModel>> get() = _populars

    // ------------------------------------------
    // --- PHƯƠNG THỨC QUẢN LÝ WISHLIST (MỚI) ---
    // ------------------------------------------

    /**
     * Lấy danh sách sản phẩm yêu thích từ TinyDB (WishList local).
     */
    fun getWishlistItems(): ArrayList<ItemsModel> {
        return managementWishList.getListWishlist()
    }

    /**
     * Thêm hoặc xóa một item khỏi WishList local.
     */
    fun toggleWishlistItem(item: ItemsModel): Boolean {
        return managementWishList.toggleWishlistItem(item)
    }

    // ------------------------------------------
    // --- PHƯƠNG THỨC TẢI DỮ LIỆU FIREBASE ---
    // ------------------------------------------

    fun loadBrands() {
        val ref = firebaseDatabase.getReference("Category")
        // ... (Logic tải Brands giữ nguyên)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BrandModel>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(BrandModel::class.java)?.let {
                        list.add(it)
                    }
                }
                _brands.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }

        })
    }

    fun loadBanners() {
        val ref = firebaseDatabase.getReference("Banner")
        // ... (Logic tải Banners giữ nguyên)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SliderModel>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(SliderModel::class.java)?.let {
                        list.add(it)
                    }
                }
                _banners.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }

        })
    }

    fun loadPopulars() {
        val ref = firebaseDatabase.getReference("Items")
        // ... (Logic tải Items giữ nguyên)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(ItemsModel::class.java)?.let {
                        list.add(it)
                    }
                }
                _populars.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })
    }
}