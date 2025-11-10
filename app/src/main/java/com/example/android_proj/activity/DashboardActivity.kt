package com.example.android_proj.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.android_proj.R
import com.example.android_proj.adapter.BrandsAdapter
import com.example.android_proj.adapter.PopularAdapter
import com.example.android_proj.adapter.SliderAdapter
import com.example.android_proj.databinding.ActivityMainBinding
import com.example.android_proj.model.SliderModel
import com.example.android_proj.viewmodel.MainViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
-

class DashboardActivity : AppCompatActivity() {

    private val viewModel : MainViewModel by lazy {
        // 1. Lấy Application Context
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)

        // 2. Sử dụng Factory để tạo ViewModel
        ViewModelProvider(this, factory)[MainViewModel::class.java]
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

        if (FirebaseAuth.getInstance().currentUser == null) {
            // Nếu chưa đăng nhập, chuyển đến LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Dừng khởi tạo Dashboard
        }

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
        initBottomNavigation()
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

    private fun initBottomNavigation() {
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.wishlistBtn.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun loadUserRoleAndSetupDrawer() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                // Mặc định là "user" nếu role chưa được đặt
                val role = document.getString("role") ?: "user"
                setupNavDrawer(role)
            }
            .addOnFailureListener { e ->
                // Xử lý lỗi (ví dụ: mất mạng) và setup với quyền user mặc định
                Log.e("Drawer", "Không thể tải vai trò: ${e.message}")
                setupNavDrawer("user")
            }
    }

    private fun setupNavDrawer(role: String) {
        // 1. Lấy tham chiếu đến NavigationView
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu

        // 2. Tìm Header View để cập nhật tên
        val headerView = navigationView.getHeaderView(0)
        val userNameTv = headerView.findViewById<TextView>(R.id.user_name_text_view) // Thay ID TextView tên người dùng

        val isUserAdmin = role == "admin"

        // 3. ẨN/HIỆN các mục quản trị

        // Tìm nhóm Admin theo ID đã định nghĩa trong nav_drawer_menu.xml
        val adminGroup = menu.findItem(R.id.admin_group)

        // Hoặc tìm từng mục riêng lẻ (Tùy thuộc vào cách bạn định nghĩa trong XML)
        val adminHomeItem = menu.findItem(R.id.nav_admin_home)
        val productMgmtItem = menu.findItem(R.id.nav_product_management)
        val orderMgmtItem = menu.findItem(R.id.nav_order_management)
        val userMgmtItem = menu.findItem(R.id.nav_user_management)
        val statisticsItem = menu.findItem(R.id.nav_statistics)

        if (adminGroup != null) {
            adminGroup.isVisible = isUserAdmin
        } else {
            // Nếu không dùng group, hãy ẩn/hiện từng mục:
            adminHomeItem?.isVisible = isUserAdmin
            productMgmtItem?.isVisible = isUserAdmin
            orderMgmtItem?.isVisible = isUserAdmin
            userMgmtItem?.isVisible = isUserAdmin
            statisticsItem?.isVisible = isUserAdmin
        }

        // 4. Cập nhật tên hiển thị trong Nav Header
        if (isUserAdmin) {
            userNameTv?.text = "Admin"
        } else {
            userNameTv?.text = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
        }
    }
}