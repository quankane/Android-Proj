package com.example.android_proj.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.android_proj.adapter.BrandsAdapter
import com.example.android_proj.adapter.PopularAdapter
import com.example.android_proj.adapter.SliderAdapter
import com.example.android_proj.databinding.ActivityMainBinding
import com.example.android_proj.model.SliderModel
import com.example.android_proj.viewModel.MainViewModel

class DashboardActivity : AppCompatActivity() {

    private val viewModel : MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private lateinit var binding : ActivityMainBinding

    private val brandsAdapter = BrandsAdapter(mutableListOf())
    private val popularAdapter = PopularAdapter(mutableListOf())

    // KHAI BÁO BIẾN RUNNABLE VÀ THỜI GIAN
    private val SLIDE_DELAY_MS = 3000L // 3 giây
    private var isAutoScrolling = false

    private val sliderRunnable : Runnable = Runnable {
        val viewPager = binding.viewPagerSlider
        val currentItem = viewPager.currentItem
        val itemCount = viewPager.adapter?.itemCount ?: 0

        if (itemCount > 1) {
            val nextItem = (currentItem + 1) % itemCount
            viewPager.setCurrentItem(nextItem, true)
        }

        // Đặt lại hẹn giờ để lặp lại
        if (isAutoScrolling) {
            viewPager.postDelayed(this.sliderRunnable, SLIDE_DELAY_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    // QUẢN LÝ VÒNG ĐỜI ĐỂ TRÁNH MEMORY LEAK
    override fun onResume() {
        super.onResume()
        // Bắt đầu cuộn lại khi Activity hiển thị
        if ((binding.viewPagerSlider.adapter?.itemCount ?: 0) > 1) {
            startAutoScroll()
        }
    }

    override fun onPause() {
        super.onPause()
        // Dừng cuộn khi Activity bị tạm dừng
        stopAutoScroll()
    }

    private fun initUI() {
        initBrands()
        initBanners()
        initRecommendation()
    }

    private fun initBrands() {
        binding.recyclerViewBrands.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewBrands.adapter = brandsAdapter
        binding.progressBarBrands.visibility = View.VISIBLE

        viewModel.brands.observe(this) {
                data ->
            brandsAdapter.updateData(data)
            binding.progressBarBrands.visibility = View.GONE
        }
        viewModel.loadBrands()
    }

    // BANNERS

    private fun setupBanners(image: List<SliderModel>) {

        // Dừng cuộn cũ nếu có
        stopAutoScroll()

        binding.viewPagerSlider.apply {
            adapter = SliderAdapter(image)

            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            (getChildAt(0) as? RecyclerView)?.overScrollMode =
                RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
        }

        binding.dotIndicator.apply {
            visibility = if (image.size > 1) View.VISIBLE else View.GONE
            if (image.size > 1) attachTo(binding.viewPagerSlider)
        }

        // BẮT ĐẦU TỰ ĐỘNG CUỘN SAU KHI THIẾT LẬP
        if (image.size > 1) {
            startAutoScroll()
        }
    }

    private fun initBanners() {
        binding.progressBarBanner.visibility = View.VISIBLE
        viewModel.banners.observe(this) {
                items ->
            setupBanners(items)
            binding.progressBarBanner.visibility = View.GONE
        }
        viewModel.loadBanners()
    }

    // CÁC HÀM MỚI ĐỂ QUẢN LÝ TỰ ĐỘNG CUỘN

    private fun startAutoScroll() {
        if (!isAutoScrolling) {
            // Bắt đầu hẹn giờ và đặt cờ
            binding.viewPagerSlider.postDelayed(sliderRunnable, SLIDE_DELAY_MS)
            isAutoScrolling = true
        }
    }

    private fun stopAutoScroll() {
        if (isAutoScrolling) {
            // Loại bỏ tất cả các callback để ngăn rò rỉ bộ nhớ
            binding.viewPagerSlider.removeCallbacks(sliderRunnable)
            isAutoScrolling = false
        }
    }

    private fun initRecommendation() {
        binding.recyclerViewRecommendation.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewRecommendation.adapter = popularAdapter
        binding.progressBarRecommendation.visibility = View.VISIBLE

        viewModel.populars.observe(this) {
            data ->
            popularAdapter.updateData(data)
            binding.progressBarRecommendation.visibility = View.GONE
        }
        viewModel.loadPopulars()
    }
}