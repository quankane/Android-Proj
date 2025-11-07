package com.example.android_proj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.android_proj.model.BrandModel
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.model.SliderModel
import com.example.android_proj.repository.MainRepository

class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    val populars: LiveData<MutableList<ItemsModel>> = repository.populars
    val brands: LiveData<MutableList<BrandModel>> = repository.brands
    val banners: LiveData<List<SliderModel>> = repository.banners

    fun loadBrands() = repository.loadBrands()
    fun loadBanners() = repository.loadBanners()
    fun loadPopulars() = repository.loadPopulars()
}