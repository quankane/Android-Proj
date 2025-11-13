package com.example.android_proj.activity.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.activity.OrderDetailActivity
import com.example.android_proj.adapter.OrderManagementAdapter
import com.example.android_proj.databinding.ActivityHomeAdminBinding
import com.example.android_proj.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class HomeAdminActivity : AppCompatActivity(), OrderManagementAdapter.OrderClickListener {

    private lateinit var binding: ActivityHomeAdminBinding
    private lateinit var adapter: OrderManagementAdapter
    private val db = FirebaseFirestore.getInstance()
    private var recentOrderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    // Dùng onResume để load lại mỗi khi quay về
    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = OrderManagementAdapter(recentOrderList, this)
        binding.recentOrdersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentOrdersRecyclerView.adapter = adapter
    }

    private fun loadDashboardData() {
        binding.progressBar.visibility = View.VISIBLE

        // Lấy thời điểm bắt đầu của ngày hôm nay
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.time

        // 1. Tải các thẻ thống kê
        db.collection("orders")
            .whereGreaterThanOrEqualTo("orderDate", startOfToday) // Lọc đơn hàng hôm nay
            .get()
            .addOnSuccessListener { documents ->
                var revenueToday = 0.0
                var newOrdersToday = 0

                for (doc in documents) {
                    val order = doc.toObject(Order::class.java)
                    // Chỉ tính doanh thu đơn "Delivered"
                    if(order.status == "Delivered") {
                        revenueToday += order.totalAmount
                    }
                    // Đếm tất cả đơn mới
                    newOrdersToday++
                }

                binding.cardRevenue.text = String.format("$%.2f", revenueToday)
                binding.cardNewOrders.text = newOrdersToday.toString()
            }
            .addOnFailureListener {
                // Đặt lại là 0 nếu lỗi
                binding.cardRevenue.text = "$0.00"
                binding.cardNewOrders.text = "0"
            }

        // 2. Tải các đơn hàng Pending
        db.collection("orders")
            .whereEqualTo("status", "Pending")
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .limit(10) // Lấy 10 đơn gần nhất
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val orders = documents.toObjects(Order::class.java).toMutableList()
                for (i in orders.indices) {
                    orders[i].orderId = documents.documents[i].id
                }
                adapter.updateData(orders)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("HomeAdmin", "Lỗi tải đơn hàng Pending", e)
                Log.e("HomeAdmin", "Lỗi tải đơn hàng Pending: " + e.message )
                Toast.makeText(this, "Lỗi tải đơn hàng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(order: Order) {
        val intent = Intent(this, OrderDetailActivity::class.java).apply {
            putExtra("ORDER_ID", order.orderId)
        }
        startActivity(intent)
    }

    override fun onUpdateStatusClick(order: Order) {
        val statuses = arrayOf("Pending", "Shipped", "Delivered", "Cancelled")
        val currentStatusIndex = statuses.indexOf(order.status)

        AlertDialog.Builder(this)
            .setTitle("Cập nhật trạng thái cho ĐH #${order.orderId.take(6)}")
            .setSingleChoiceItems(statuses, currentStatusIndex) { dialog, which ->
                val selectedStatus = statuses[which]
                updateOrderStatus(order, selectedStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateOrderStatus(order: Order, newStatus: String) {
        db.collection("orders").document(order.orderId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show()
                loadDashboardData() // Tải lại toàn bộ data
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Cập nhật thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}