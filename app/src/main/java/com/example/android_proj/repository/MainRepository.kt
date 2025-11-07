package com.example.android_proj.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_proj.model.BrandModel
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.model.SliderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance();

    var _populars = MutableLiveData<MutableList<ItemsModel>>()
    private val _brands = MutableLiveData<MutableList<BrandModel>>()
    private val _banners = MutableLiveData<List<SliderModel>>()

    val banners: LiveData<List<SliderModel>> get() = _banners
    val brands: LiveData<MutableList<BrandModel>> get() = _brands
    val populars: LiveData<MutableList<ItemsModel>> get() = _populars

    fun loadBrands() {
        val ref = firebaseDatabase.getReference("Category")
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
                TODO("Not yet implemented")
            }

        })
    }

    fun loadBanners() {
        val ref = firebaseDatabase.getReference("Banner")
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
                TODO("Not yet implemented")
            }

        })
    }

    fun loadPopulars() {
        val ref = firebaseDatabase.getReference("Items")
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
                TODO("Not yet implemented")
            }

        })
    }
}