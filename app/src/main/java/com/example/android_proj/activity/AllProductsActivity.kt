package com.example.android_proj.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android_proj.adapter.PopularAdapter // Dùng lại adapter của bạn
import com.example.android_proj.databinding.ActivityAllProductsBinding
import com.example.android_proj.viewmodel.MainViewModel

class AllProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllProductsBinding

    private lateinit var popularAdapter: PopularAdapter
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Cài đặt Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút Back

        // 2. Khởi tạo RecyclerView
        initRecyclerView()

        // 3. Tải dữ liệu
        loadAllProducts()
    }

    private fun initRecyclerView() {
        popularAdapter = PopularAdapter(mutableListOf())
        binding.allProductsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AllProductsActivity, 2) // Dạng lưới 2 cột
            adapter = popularAdapter
        }
    }

    private fun loadAllProducts() {
        binding.progressBar.visibility = View.VISIBLE

        // Quan sát dữ liệu từ ViewModel
        viewModel.populars.observe(this) { items ->
            popularAdapter.updateData(items)
            binding.progressBar.visibility = View.GONE
        }

        // Kích hoạt việc tải dữ liệu
        viewModel.loadPopulars()
    }

    // Xử lý khi nhấn nút Back trên Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed() // Quay lại màn hình trước
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}