package com.example.android_proj.activity

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
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
        ratingTxt.text = "${item.rating} Rating"
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

        addToCartBtn.setOnClickListener {
            // Lấy giá trị đã được cập nhật từ biến lưu trữ (String?)
            val hasSizes = item.size.isNotEmpty()
            val hasColors = item.color.isNotEmpty()

            val chosenSize = selectedSize
            val chosenColor = selectedColor

            // Nếu có tùy chọn size/color nhưng người dùng chưa chọn
            if (hasSizes && chosenSize == null) {
                Toast.makeText(this@DetailActivity, "Vui lòng chọn Size!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (hasColors && chosenColor == null) {
                Toast.makeText(this@DetailActivity, "Vui lòng chọn Màu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Sử dụng các biến String đã lưu
            item.selectedSize = chosenSize.toString()
            item.selectedColor = chosenColor.toString()
            item.numberInCart = numberItemTxt.text.toString().toInt();

            managementCart.insertFood(item)
            Toast.makeText(this@DetailActivity, "Đã thêm sản phẩm vào giỏ hàng", Toast.LENGTH_SHORT).show()
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