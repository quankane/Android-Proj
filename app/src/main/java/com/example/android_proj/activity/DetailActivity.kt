package com.example.android_proj.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.android_proj.R
import com.example.android_proj.adapter.ColorAdapter
import com.example.android_proj.adapter.PicsAdapter
import com.example.android_proj.adapter.SizeAdapter
import com.example.android_proj.databinding.ActivityDetailBinding
import com.example.android_proj.helper.ManagementCart
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managementCart : ManagementCart

    private var selectedSize: String? = null
    private var selectedColor: String? = null

    private val viewModel : MainViewModel by lazy {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart = ManagementCart(this)
        item = intent.getSerializableExtra("object")!! as ItemsModel

        setupViews()
        setupPicsList()
        setupColorsList()
        setupSizeList()
    }

    // HÀM KIỂM TRA VÀ CẬP NHẬT TRẠNG THÁI YÊU THÍCH BAN ĐẦU
    private fun checkFavoriteStatus() {
        val wishlist = viewModel.getWishlistItems()
        // Kiểm tra xem item hiện tại có tồn tại trong wishlist không (chỉ cần so sánh title/id)
        val isFavorite = wishlist.any { it.title == item.title }

        updateFavoriteIcon(isFavorite)
    }

    // HÀM CẬP NHẬT BIỂU TƯỢNG
    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.favBtn.setImageResource(R.drawable.fav_icon_filled) // Màu đỏ/Đã đầy
        } else {
            binding.favBtn.setImageResource(R.drawable.fav_icon) // Màu trắng/Trống
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() = with(binding) {
        titleTxt.text = item.title
        descriptionTxt.text = item.description
        priceTxt.text = "$${item.price}"
        oldPriceTxt.text = "$${item.oldPrice}"
        oldPriceTxt.paintFlags = priceTxt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        ratingTxt.text = "${item.rating}"
        numberItemTxt.text = item.numberInCart.toString()
        selectedColor = item.selectedColor.toString()
        selectedSize = item.selectedSize.toString()

        updateTotalPrice()

        Glide.with(this@DetailActivity)
            .load(item.picUrl.firstOrNull())
            .into(picMain)

        backBtn.setOnClickListener { finish() }

        plusBtn.setOnClickListener {
            item.numberInCart++
            numberItemTxt.text = item.numberInCart.toString()
            updateTotalPrice()
        }

        minusBtn.setOnClickListener {
            if (item.numberInCart > 1) {
                item.numberInCart--
                numberItemTxt.text = item.numberInCart.toString()
                updateTotalPrice()
            }
        }

        favBtn.setOnClickListener {
            // Gọi ViewModel để thay đổi trạng thái
            val isAdded = viewModel.toggleWishlistItem(item)

            // Cập nhật biểu tượng UI ngay lập tức
            updateFavoriteIcon(isAdded)

            val message = if (isAdded) "Đã thêm vào danh sách yêu thích!" else "Đã xóa khỏi danh sách yêu thích!"
            Toast.makeText(this@DetailActivity, message, Toast.LENGTH_SHORT).show()
        }

        // Trong DetailActivity.kt, bên trong hàm setupViews()

        addToCartBtn.setOnClickListener {
            Log.i("Detail Activity", "1")
            // 1. KIỂM TRA ĐĂNG NHẬP (Quan trọng nhất)
            if (FirebaseAuth.getInstance().currentUser == null) {
                Log.i("Detail Activity", "2")
                Toast.makeText(this@DetailActivity, "Vui lòng đăng nhập để thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                // (Tùy chọn) Chuyển người dùng đến màn hình đăng nhập
                 startActivity(Intent(this@DetailActivity, LoginActivity::class.java))
                return@setOnClickListener
            }

            val hasSizes = item.size.isNotEmpty()
            val hasColors = item.color.isNotEmpty()

            // Lấy giá trị đã chọn (vẫn là String?)
            val chosenSize = selectedSize
            val chosenColor = selectedColor

            // 2. SỬA LỖI VALIDATION (dùng isNullOrEmpty thay vì == null)
            Log.i("Detail Activity", "3")

            if (hasSizes && chosenSize.isNullOrEmpty()) {
                Log.i("Detail Activity", "4")
                Toast.makeText(this@DetailActivity, "Vui lòng chọn Size!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hasColors && chosenColor.isNullOrEmpty()) {
                Log.i("Detail Activity", "5")
                Toast.makeText(this@DetailActivity, "Vui lòng chọn Màu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Log.i("Detail Activity", "6")
            // 3. SỬA LỖI gán `null.toString()` (dùng toán tử ?: "")
            item.selectedSize = chosenSize ?: ""
            item.selectedColor = chosenColor ?: ""
            item.numberInCart = numberItemTxt.text.toString().toInt()
            Log.i("Detail Activity", "7")
            managementCart.insertFood(item)
            Log.i("Detail Activity", "8")
            // 4. (Khuyến nghị) XÓA TOAST "ĐÃ THÊM" Ở ĐÂY
            // Toast "Đã thêm vào giỏ hàng" đã có sẵn bên trong ManagementCart.
            // Giữ nó ở đây sẽ gây ra 2 Toast (một từ ManagementCart, một từ Activity)
            // Toast.makeText(this@DetailActivity, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPicsList() {
        val picList = item.picUrl.toList()
        binding.picList.apply {
            adapter = PicsAdapter(picList as MutableList<String>) {
                imageUrl ->
                Glide.with(this@DetailActivity)
                    .load(imageUrl)
                    .into(binding.picMain)
            }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupColorsList() {
        binding.colorList.adapter = ColorAdapter(
            item.color,
            onColorSelected = { color ->
                selectedColor = color // Cập nhật biến lưu trữ
            })
        binding.colorList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun setupSizeList() {
        val sizeList = item.size.map { it }
        binding.sizeList.apply {
            adapter = SizeAdapter(
                sizeList as MutableList<String>,
                onSizeSelected = { size ->
                    selectedSize = size // Cập nhật biến lưu trữ
                    // Cập nhật lại tổng giá nếu cần
                }
            )
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalPrice() = with(binding) {
        totalPriceTxt.text = "$${item.price * item.numberInCart}"
    }
}