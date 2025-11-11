package com.example.android_proj.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.R
import com.example.android_proj.adapter.OrderAdapter
import com.example.android_proj.databinding.ActivityMyOrdersBinding
import com.example.android_proj.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOrdersBinding
    private lateinit var orderAdapter: OrderAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        loadOrders()
    }

    private fun initRecyclerView() {
        orderAdapter = OrderAdapter(mutableListOf())
        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(this@MyOrdersActivity)
            adapter = orderAdapter
        }
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            binding.emptyView.visibility = View.VISIBLE
            return
        }

        binding.progressBarOrders.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        // Truy vấn Firestore: Lọc theo userId và sắp xếp theo ngày đặt hàng
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("orderDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                binding.progressBarOrders.visibility = View.GONE
                val ordersList = mutableListOf<Order>()

                for (document in result) {
                    try {
                        val order = document.toObject(Order::class.java).copy(orderId = document.id)
                        ordersList.add(order)
                    } catch (e: Exception) {
                        Log.e("MyOrdersActivity", "Lỗi ánh xạ Order: ${e.message}", e)
                    }
                }

                if (ordersList.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    orderAdapter.updateData(ordersList)
                }
            }
            .addOnFailureListener { exception ->
                binding.progressBarOrders.visibility = View.GONE
                Log.e("MyOrdersActivity", "Lỗi tải đơn hàng: $exception")
                binding.emptyView.text = "Lỗi kết nối hoặc tải dữ liệu."
                binding.emptyView.visibility = View.VISIBLE
            }
    }
}