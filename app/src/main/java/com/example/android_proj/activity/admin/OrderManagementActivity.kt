package com.example.android_proj.activity.admin

import android.content.Intent // THÊM IMPORT NÀY
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.activity.OrderDetailActivity // THÊM IMPORT NÀY
import com.example.android_proj.adapter.OrderManagementAdapter
import com.example.android_proj.databinding.ActivityOrderManagementBinding
import com.example.android_proj.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderManagementActivity : AppCompatActivity(), OrderManagementAdapter.OrderClickListener {

    private lateinit var binding: ActivityOrderManagementBinding
    private lateinit var adapter: OrderManagementAdapter
    private val db = FirebaseFirestore.getInstance()
    private var orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        loadOrders()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Truyền 'this' (vì Activity này đã implement OrderClickListener)
        adapter = OrderManagementAdapter(orderList, this)
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = adapter
    }

    private fun loadOrders() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("orders")
            .orderBy("orderDate", Query.Direction.DESCENDING)
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
                Log.e("OrderManagement", "Lỗi tải đơn hàng", e)
                Toast.makeText(this, "Lỗi tải đơn hàng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Xử lý click vào item ---
    override fun onItemClick(order: Order) {
        // Mở màn hình chi tiết (giống hệt bên OrderAdapter)
        val intent = Intent(this, OrderDetailActivity::class.java).apply {
            putExtra("ORDER_ID", order.orderId)
        }
        startActivity(intent)
    }

    // Xử lý click vào Status
    override fun onUpdateStatusClick(order: Order) {
        val statuses = arrayOf("Đang chờ", "Đang xử lý", "Thành công", "Hủy bỏ")
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
                loadOrders() // Tải lại danh sách
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Cập nhật thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}