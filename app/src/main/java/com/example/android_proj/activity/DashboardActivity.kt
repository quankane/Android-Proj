package com.example.android_proj.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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

        loadUserRoleAndSetupDrawer()
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
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val navigationView = findViewById<NavigationView>(R.id.nav_view_drawer) // Thay ID thích hợp
        val menu = navigationView.menu

        // 1. Lấy Header View
        val headerView = navigationView.getHeaderView(0)

        // 2. Tìm các ID mới
        val userNameTv = headerView.findViewById<TextView>(R.id.user_name_text_view)
        val userEmailTv = headerView.findViewById<TextView>(R.id.user_email_text_view)
        val avatarImg = headerView.findViewById<ImageView>(R.id.nav_header_avatar) // ID mới cho ImageView

        val isUserAdmin = role == "admin"

        // 3. Cập nhật Text và Email
        if (isUserAdmin) {
            userNameTv?.text = "Admin"
        } else {
            userNameTv?.text = user.displayName ?: "User"
        }
        userEmailTv?.text = user.email // Hiển thị email

        // 4. Tải Ảnh đại diện (Avatar)
        if (user.photoUrl != null) {
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_user_profile)
                .transform(CircleCrop()) // Giả sử bạn muốn ảnh tròn
                .into(avatarImg)
        } else {
            // Đặt lại ảnh mặc định nếu không có photoUrl
            avatarImg.setImageResource(R.drawable.ic_user_profile)
        }

        // 5. Ẩn/Hiện nhóm các mục quản trị (Logic phân quyền)
        val adminGroup = menu.findItem(R.id.admin_group)
        adminGroup?.isVisible = isUserAdmin
    }
}