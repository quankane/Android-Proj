package com.example.android_proj.viewmodel

import android.app.Application // Cần import Application
import androidx.lifecycle.AndroidViewModel // <-- Thay thế ViewModel bằng AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.android_proj.model.BrandModel
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.model.SliderModel
import com.example.android_proj.repository.MainRepository

// CHÚ Ý: Phải kế thừa AndroidViewModel và truyền Application vào constructor
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Khởi tạo Repository bằng Application context
    private val repository = MainRepository(application)

    // --- Các LiveData hiện có ---
    val populars: LiveData<MutableList<ItemsModel>> = repository.populars
    val brands: LiveData<MutableList<BrandModel>> = repository.brands
    val banners: LiveData<List<SliderModel>> = repository.banners

    // --- Phương thức tải dữ liệu hiện có ---
    fun loadBrands() = repository.loadBrands()
    fun loadBanners() = repository.loadBanners()
    fun loadPopulars() = repository.loadPopulars()

    // ------------------------------------------
    // --- PHƯƠNG THỨC QUẢN LÝ WISHLIST (MỚI) ---
    // ------------------------------------------

    /**
     * Lấy danh sách ItemModel hiện tại trong WishList từ Repository.
     */
    fun getWishlistItems(): ArrayList<ItemsModel> {
        return repository.getWishlistItems()
    }

    /**
     * Thêm hoặc xóa một item khỏi WishList.
     * @return True nếu item được thêm, False nếu item bị xóa.
     */
    fun toggleWishlistItem(item: ItemsModel): Boolean {
        return repository.toggleWishlistItem(item)
    }
}