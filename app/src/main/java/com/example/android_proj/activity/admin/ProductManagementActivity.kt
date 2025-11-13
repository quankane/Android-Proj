package com.example.android_proj.activity.admin

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts // THÊM IMPORT NÀY
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.adapter.admin.ProductManagementAdapter
import com.example.android_proj.databinding.ActivityProductManagementBinding
import com.example.android_proj.model.ItemsModel
import com.google.firebase.firestore.FirebaseFirestore

class ProductManagementActivity : AppCompatActivity(), ProductManagementAdapter.ProductClickListener {

    private lateinit var binding: ActivityProductManagementBinding
    private lateinit var adapter: ProductManagementAdapter
    private val db = FirebaseFirestore.getInstance()
    private var productList = mutableListOf<ItemsModel>()

    // --- THÊM BỘ KHỞI CHẠY ACTIVITY ĐỂ NHẬN KẾT QUẢ ---
    private val addEditProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Nếu Activity kia trả về RESULT_OK (tức là đã Thêm/Sửa thành công)
        if (result.resultCode == Activity.RESULT_OK) {
            // Tải lại danh sách sản phẩm
            loadProducts()
        }
    }

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

        // --- CẬP NHẬT NÚT THÊM ---
        binding.fabAddProduct.setOnClickListener {
            // Mở AddEditProductActivity ở chế độ "Thêm mới" (không gửi ID)
            val intent = Intent(this, AddEditProductActivity::class.java)
            addEditProductLauncher.launch(intent)
        }
    }

    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("Items") // Dùng "Items"
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val items = documents.toObjects(ItemsModel::class.java).toMutableList()

                for (i in items.indices) {
                    items[i].id = documents.documents[i].id
                }

                productList.clear()
                productList.addAll(items)
                adapter.updateData(items)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("ProductManagement", "Lỗi tải sản phẩm Firestore", e)
                Toast.makeText(this, "Lỗi tải sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- CLICK sửa ---
    override fun onEditClick(item: ItemsModel) {
        // Mở AddEditProductActivity ở chế độ "Sửa" (gửi kèm ID)
        val intent = Intent(this, AddEditProductActivity::class.java).apply {
            putExtra("PRODUCT_ID", item.id)
        }
        addEditProductLauncher.launch(intent)
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

    // --- CLICK XÓA ---
    private fun deleteProductFromFirebase(item: ItemsModel) {
        if (item.id.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Items").document(item.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa: ${item.title}", Toast.LENGTH_SHORT).show()
                adapter.removeItem(item)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}