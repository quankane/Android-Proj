package com.example.android_proj.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.R
import com.example.android_proj.adapter.OrderAdapter
import com.example.android_proj.databinding.ActivityMyOrdersBinding
import com.example.android_proj.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOrdersBinding
    private lateinit var orderAdapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đảm bảo người dùng đã đăng nhập
        if (auth.currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        loadOrders()
    }

    private fun initView() {
        // Thiết lập Toolbar
        binding.toolbar.title = "Đơn hàng của tôi"
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Thiết lập RecyclerView
        orderAdapter = OrderAdapter(mutableListOf())
        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(this@MyOrdersActivity)
            adapter = orderAdapter
        }
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        binding.progressBarOrders.visibility = View.VISIBLE

        Log.i("ORDER", "USERID = " + userId)

        db.collection("orders")
            // Lọc đơn hàng chỉ thuộc về người dùng này
            .whereEqualTo("userId", userId)
            // Sắp xếp theo ngày đặt hàng mới nhất
            .orderBy("orderDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBarOrders.visibility = View.GONE
                val ordersList = documents.toObjects(Order::class.java).toMutableList()

                // Gán ID document vào trường orderId
                for (i in ordersList.indices) {
                    ordersList[i].orderId = documents.documents[i].id
                }

                if (ordersList.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.recyclerViewOrders.visibility = View.GONE
                } else {
                    orderAdapter.updateData(ordersList)
                    binding.emptyView.visibility = View.GONE
                    binding.recyclerViewOrders.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                binding.progressBarOrders.visibility = View.GONE
                Log.e("MyOrders", "Lỗi tải đơn hàng: ${e.message}")
                Toast.makeText(this, "Không thể tải đơn hàng: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}