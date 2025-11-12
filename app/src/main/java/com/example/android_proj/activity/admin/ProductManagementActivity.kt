package com.example.android_proj.activity.admin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.adapter.ProductManagementAdapter
import com.example.android_proj.databinding.ActivityProductManagementBinding
import com.example.android_proj.model.ItemsModel
// --- Đảm bảo import đúng FIRESTORE ---
import com.google.firebase.firestore.FirebaseFirestore

class ProductManagementActivity : AppCompatActivity(), ProductManagementAdapter.ProductClickListener {

    private lateinit var binding: ActivityProductManagementBinding
    private lateinit var adapter: ProductManagementAdapter
    // --- Dùng FIRESTORE ---
    private val db = FirebaseFirestore.getInstance()
    private var productList = mutableListOf<ItemsModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        loadProducts()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = ProductManagementAdapter(productList, this, this)
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.productsRecyclerView.adapter = adapter

        // Nút THÊM:
        binding.fabAddProduct.setOnClickListener {
            // (Chúng ta sẽ làm màn hình này sau)
            // val intent = Intent(this, AddEditProductActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Chuyển đến màn hình Thêm sản phẩm", Toast.LENGTH_SHORT).show()
        }
    }

    // --- HÀM LOAD ĐÃ SỬA LẠI ĐỂ DÙNG FIRESTORE ---
    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE

        // Truy cập collection "items" (hoặc tên bạn đặt) trong FIRESTORE
        // Dựa trên ảnh của bạn, tên collection là "items"
        db.collection("items")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                // Chuyển QuerySnapshot thành danh sách ItemsModel
                val items = documents.toObjects(ItemsModel::class.java).toMutableList()

                // Gán Document ID vào trường 'id' của mỗi object
                for (i in items.indices) {
                    items[i].id = documents.documents[i].id
                }

                productList.clear()
                productList.addAll(items)
                adapter.updateData(items) // Cập nhật adapter
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("ProductManagement", "Lỗi tải sản phẩm Firestore", e)
                Toast.makeText(this, "Lỗi tải sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Xử lý khi nhấn nút SỬA
    override fun onEditClick(item: ItemsModel) {
        // (Logic sửa sẽ làm sau)
        Toast.makeText(this, "Sửa sản phẩm: ${item.title}", Toast.LENGTH_SHORT).show()
    }

    // Xử lý khi nhấn nút XÓA
    override fun onDeleteClick(item: ItemsModel) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '${item.title}'?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteProductFromFirebase(item)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // --- HÀM XÓA ĐÃ SỬA LẠI ĐỂ DÙNG FIRESTORE ---
    private fun deleteProductFromFirebase(item: ItemsModel) {
        if (item.id.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show()
            return
        }

        // Xóa document trong FIRESTORE
        db.collection("items").document(item.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa: ${item.title}", Toast.LENGTH_SHORT).show()
                // Xóa khỏi RecyclerView
                adapter.removeItem(item)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}