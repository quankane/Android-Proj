package com.example.android_proj.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.android_proj.model.BrandModel
import com.example.android_proj.repository.MainRepository

class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    val brands: LiveData<MutableList<BrandModel>> = repository.brands

    fun loadBrands() = repository.loadBrands()
}