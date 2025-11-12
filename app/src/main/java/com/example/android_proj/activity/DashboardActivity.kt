package com.example.android_proj.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import androidx.core.view.isVisible
import com.example.android_proj.activity.MyOrdersActivity
import com.example.android_proj.activity.admin.OrderManagementActivity
import com.example.android_proj.activity.admin.ProductManagementActivity
import com.example.android_proj.activity.admin.StatisticsActivity
import com.example.android_proj.activity.admin.UserManagementActivity

// implements NavigationView.OnNavigationItemSelectedListener để xử lý các sự kiện click trong Nav Drawer
class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel : MainViewModel by lazy {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    private lateinit var binding : ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout // Tham chiếu đến DrawerLayout

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
        Log.i("DASHBOARD ACTIVITY", "Đã chạy vào dashboard")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tham chiếu DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        if (FirebaseAuth.getInstance().currentUser == null) {
            // Nếu chưa đăng nhập, chuyển đến LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Dừng khởi tạo Dashboard
        }
    }

    // Xử lý sự kiện khi người dùng chọn một mục trong Nav Drawer
    // Phương thức này chỉ xử lý các mục KHÔNG có setOnMenuItemClickListener
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Đóng Drawer sau khi chọn mục
        drawerLayout.closeDrawer(GravityCompat.START)

        when (item.itemId) {
            R.id.nav_home -> return true

            // R.id.nav_orders, R.id.nav_product_management, ... đã được xử lý bằng setOnMenuItemClickListener

            R.id.nav_logout -> {
                performLogout()
            }
        }
        return true
    }

    // Xử lý nút Back (Đóng Drawer nếu đang mở)
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }


    // QUẢN LÝ VÒNG ĐỜI ĐỂ TRÁNH MEMORY LEAK
    override fun onResume() {
        super.onResume()
        if ((binding.viewPagerSlider.adapter?.itemCount ?: 0) > 1) {
            startAutoScroll()
        }
        initUI()
        loadUserRoleAndSetupDrawer()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    private fun initUI() {
        initBrands()
        initBanners()
        initRecommendation()
        initBottomNavigation()
        // Gọi setupDrawerOpener để gán Listener cho nút Menu và Chuông
        // Gắn listener cho Navigation View
        val navigationView = findViewById<NavigationView>(R.id.nav_view_drawer)
        navigationView.setNavigationItemSelectedListener(this)
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
        Log.i("DASHBOARD ACTIVITY", "Load user role and set up drawer")
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                Log.i("DASHBOARD ACTIVITY", "SUCCESS")
                // Mặc định là "user" nếu role chưa được đặt
                val role = document.getString("role") ?: "user"
                setupDrawerOpener(role)
                setupNavDrawer(role)
            }
            .addOnFailureListener { e ->
                Log.e("DASHBOARD ACTIVITY", "FAILURE", e)
                setupNavDrawer("user")
            }
    }

    private fun setupDrawerOpener(role: String) {
        val isUserAdmin = role == "admin"

        // Tìm nút Menu (menuIconBtn)
        val menuButton = findViewById<ImageView>(R.id.menuIconBtn)

        // 1. Chỉ hiển thị nút Menu cho Admin
        menuButton?.isVisible = isUserAdmin

        if (isUserAdmin) {
            // 2. Gán Listener cho ICON MENU (menuIconBtn)
            menuButton?.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        // 3. Gán Listener cho ICON THÔNG BÁO (imageView2) - Luôn mở Drawer
        binding.imageView2.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavDrawer(role: String) {
        Log.i("DASHBOARD ACTIVITY", "Set up Nav Drawer")
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val navigationView = findViewById<NavigationView>(R.id.nav_view_drawer)
        val menu = navigationView.menu

        // 1. Lấy Header View
        val headerView = navigationView.getHeaderView(0)

        // 2. Tìm các ID trong Header View
        val userNameTv = headerView.findViewById<TextView>(R.id.user_name_text_view)
        val userEmailTv = headerView.findViewById<TextView>(R.id.user_email_text_view)
        val avatarImg = headerView.findViewById<ImageView>(R.id.nav_header_avatar)

        val isUserAdmin = role == "admin"

        Log.i("DASHBOARD ACTIVITY", "Is Admin: $isUserAdmin")

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
                .transform(CircleCrop())
                .into(avatarImg)
        } else {
            // Đặt lại ảnh mặc định nếu không có photoUrl
            avatarImg.setImageResource(R.drawable.ic_user_profile)
        }

        // **********************************************
        // 5. THIẾT LẬP LISTENER CHO TẤT CẢ CÁC MỤC
        // **********************************************

        // 5a. Thiết lập hiển thị nhóm Admin
        Log.i("CHECK", "USER ADMIN = " + isUserAdmin)
        menu.setGroupVisible(R.id.admin_group, isUserAdmin)

        // 5b. My Orders (User và Admin đều thấy)
        val myOrderBtn = menu.findItem(R.id.nav_orders)
        myOrderBtn?.setOnMenuItemClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, MyOrdersActivity::class.java))
            true
        }

        // 5c. Các mục chỉ Admin thấy (Cần kiểm tra trước khi gán listener)
        if (isUserAdmin) {
            // Product Management
            menu.findItem(R.id.nav_product_management)?.setOnMenuItemClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, ProductManagementActivity::class.java))
                true
            }

            // Order Management
            menu.findItem(R.id.nav_order_management)?.setOnMenuItemClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, OrderManagementActivity::class.java))
                true
            }

            // User Management
            menu.findItem(R.id.nav_user_management)?.setOnMenuItemClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, UserManagementActivity::class.java))
                true
            }

            // Home Admin
            menu.findItem(R.id.nav_admin_home)?.setOnMenuItemClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                // Giả định Home Admin sẽ chuyển đến một Activity khác hoặc chỉ là nav_home
                // Nếu chỉ là Nav Home, bạn có thể bỏ qua hoặc xử lý lại logic này.
                true
            }

            // Statistics
            menu.findItem(R.id.nav_statistics)?.setOnMenuItemClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                 startActivity(Intent(this, StatisticsActivity::class.java)) // Cần tạo StatisticsActivity
                true
            }
        }
    }
}