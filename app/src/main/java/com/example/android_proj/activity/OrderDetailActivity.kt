package com.example.android_proj.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.android_proj.R
import com.example.android_proj.databinding.ActivityOrderDetailBinding
import com.example.android_proj.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private var orderId: String? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy ID từ Intent
        orderId = intent.getStringExtra("ORDER_ID")

        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        loadOrderDetails()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadOrderDetails() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        db.collection("orders").document(orderId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val order = document.toObject(Order::class.java)
                    if (order != null) {
                        order.orderId = document.id // Gán ID cho order
                        bindDetailsToView(order)
                    } else {
                        showError("Không thể đọc dữ liệu đơn hàng.")
                    }
                } else {
                    showError("Không tìm thấy đơn hàng.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrderDetail", "Lỗi tải chi tiết đơn hàng", e)
                showError("Lỗi: ${e.message}")
            }
    }

    private fun bindDetailsToView(order: Order) {
        binding.progressBar.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE

        // Thông tin chung
        binding.orderIdDetailTxt.text = "Mã đơn hàng: #${order.orderId.take(6)}"
        binding.orderDateDetailTxt.text = "Ngày đặt: ${dateFormatter.format(order.orderDate.toDate())}"
        binding.orderStatusDetailTxt.text = "Trạng thái: ${order.status}"

        // Thông tin giao hàng
        binding.shippingNameTxt.text = "Tên: ${order.shippingAddress.fullName}"
        binding.shippingAddressTxt.text = "Địa chỉ: ${order.shippingAddress.streetAddress}, ${order.shippingAddress.city}"
        binding.shippingPhoneTxt.text = "SĐT: ${order.shippingAddress.phoneNumber}"

        // Chi tiết thanh toán
        binding.paymentMethodTxt.text = "Hình thức: ${order.paymentMethod}"
        binding.subtotalTxt.text = String.format("$%.2f", order.subtotal)
        binding.deliveryTxt.text = String.format("$%.2f", order.deliveryFee)
        binding.taxTxt.text = String.format("$%.2f", order.tax)
        binding.totalTxt.text = String.format("$%.2f", order.totalAmount)

        // --- Hiển thị danh sách sản phẩm (Quan trọng) ---
        // Xóa mọi view cũ (nếu có)
        binding.itemsContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)
        for (item in order.items) {
            // "Thổi" (inflate) layout item_order_detail_product.xml
            val itemBinding = com.example.android_proj.databinding.ItemOrderDetailProductBinding.inflate(
                inflater,
                binding.itemsContainer,
                false // Phải là false
            )

            // Gán dữ liệu cho item
            itemBinding.productTitleTxt.text = item.title
            itemBinding.productQuantityTxt.text = "Số lượng: ${item.quantity}"
            itemBinding.productPriceTxt.text = String.format("$%.2f", item.priceAtPurchase)

            Glide.with(this)
                .load(item.picUrl)
                .placeholder(R.drawable.ic_product) // Thay bằng ảnh placeholder của bạn
                .into(itemBinding.productImage)

            // Thêm view của item này vào LinearLayout
            binding.itemsContainer.addView(itemBinding.root)
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}