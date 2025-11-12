package com.example.android_proj.viewmodel

import android.app.Application // <-- Đổi từ Context sang Application
import androidx.lifecycle.AndroidViewModel // <-- Đổi từ ViewModel sang AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.android_proj.model.BrandModel
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.model.SliderModel
import com.example.android_proj.repository.MainRepository

// 1. ĐỔI sang AndroidViewModel và nhận Application
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Truyền 'application' (là một Context) vào Repository
    private val repository = MainRepository(application)

    // Các LiveData không đổi
    val banners: LiveData<List<SliderModel>> = repository.banners
    val brands: LiveData<MutableList<BrandModel>> = repository.brands
    val populars: LiveData<MutableList<ItemsModel>> = repository.populars

    // Các hàm load không đổi
    fun loadBanners() {
        repository.loadBanners()
    }

    fun loadBrands() {
        repository.loadBrands()
    }

    fun loadPopulars() {
        repository.loadPopulars()
    }

    // Các hàm Wishlist không đổi
    fun getWishlistItems(): ArrayList<ItemsModel> {
        return repository.getWishlistItems()
    }

    fun toggleWishlistItem(item: ItemsModel): Boolean {
        return repository.toggleWishlistItem(item)
    }
}