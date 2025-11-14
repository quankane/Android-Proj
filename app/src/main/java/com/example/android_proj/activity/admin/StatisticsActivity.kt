package com.example.android_proj.activity.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.databinding.ActivityStatisticsBinding
import com.example.android_proj.model.Order
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        loadStatistics()
    }

    private fun loadStatistics() {
        binding.progressBar.visibility = View.VISIBLE

        // 1. Tạo các Task
        val ordersTask = db.collection("orders").get()
        val usersTask = db.collection("users").get()
        val itemsTask = db.collection("Items").get()

        // 2. Chạy tất cả song song
        Tasks.whenAllSuccess<QuerySnapshot>(ordersTask, usersTask, itemsTask)
            .addOnSuccessListener { results ->

                // Xử lý Orders
                val ordersSnapshot = results[0]
                val orders = ordersSnapshot.toObjects(Order::class.java)

                var totalRevenue = 0.0
                var pending = 0
                var shipped = 0
                var delivered = 0
                var cancelled = 0

                for (order in orders) {
                    // Chỉ tính doanh thu đơn hàng đã giao
                    if(order.status == "Thành công") {
                        totalRevenue += order.totalAmount
                    }
                    // Đếm trạng thái
                    when (order.status) {
                        "Đang chờ" -> pending++
                        "Đang xử lý" -> shipped++
                        "Thành công" -> delivered++
                        "Hủy bỏ" -> cancelled++
                    }
                }

                val totalOrders = orders.size
                val avgOrderValue = if (totalOrders > 0) totalRevenue / totalOrders else 0.0

                // Xử lý Users
                val usersSnapshot = results[1]
                val totalUsers = usersSnapshot.size()

                // Xử lý Items
                val itemsSnapshot = results[2]
                val totalProducts = itemsSnapshot.size()

                // 3. Cập nhật UI
                updateUI(totalRevenue, totalOrders, avgOrderValue, totalUsers, totalProducts, pending, shipped, delivered, cancelled)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Lỗi tải thống kê: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(
        revenue: Double,
        orders: Int,
        avg: Double,
        users: Int,
        products: Int,
        pending: Int,
        shipped: Int,
        delivered: Int,
        cancelled: Int
    ) {
        binding.progressBar.visibility = View.GONE

        binding.statTotalRevenue.text = String.format("$%.2f", revenue)
        binding.statTotalOrders.text = orders.toString()
        binding.statAvgOrderValue.text = String.format("$%.2f", avg)
        binding.statTotalUsers.text = users.toString()
        binding.statTotalProducts.text = products.toString()

        binding.statPending.text = "Chờ xử lý (Pending): $pending"
        binding.statShipped.text = "Đang giao (Shipped): $shipped"
        binding.statDelivered.text = "Đã giao (Delivered): $delivered"
        binding.statCancelled.text = "Đã hủy (Cancelled): $cancelled"
    }
}