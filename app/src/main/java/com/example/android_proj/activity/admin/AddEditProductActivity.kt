package com.example.android_proj.activity.admin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.databinding.ActivityAddEditProductBinding
import com.example.android_proj.model.ItemsModel
import com.google.firebase.firestore.FirebaseFirestore

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentProductId: String? = null
    private var currentItem: ItemsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentProductId = intent.getStringExtra("PRODUCT_ID")

        initView()

        if (currentProductId == null) {
            binding.toolbar.title = "Thêm sản phẩm mới"
        } else {
            binding.toolbar.title = "Chỉnh sửa sản phẩm"
            loadProductDetails()
        }
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveProduct() }
    }

    //Sửa
    private fun loadProductDetails() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("Items").document(currentProductId!!)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document.exists()) {
                    currentItem = document.toObject(ItemsModel::class.java)
                    currentItem?.id = document.id // Gán ID
                    populateForm(currentItem)
                } else {
                    Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Lỗi tải dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun populateForm(item: ItemsModel?) {
        item ?: return
        binding.etTitle.setText(item.title)
        binding.etDescription.setText(item.description)
        binding.etPrice.setText(item.price.toString())
        binding.etOldPrice.setText(item.oldPrice.toString())
        binding.etRating.setText(item.rating.toString())

        binding.etPicUrl.setText(item.picUrl.joinToString(","))
        binding.etSize.setText(item.size.joinToString(","))
        binding.etColor.setText(item.color.joinToString(","))
    }

    private fun saveProduct() {
        binding.progressBar.visibility = View.VISIBLE

        // 1. Đọc dữ liệu từ Form
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val oldPrice = binding.etOldPrice.text.toString().toDoubleOrNull() ?: 0.0
        val rating = binding.etRating.text.toString().toDoubleOrNull() ?: 0.0 // <-- THÊM DÒNG NÀY

        // 2. Chuyển chuỗi "a, b, c" thành List<String>
        val picUrlList = binding.etPicUrl.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() } as ArrayList<String>

        val sizeList = binding.etSize.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() } as ArrayList<String>

        val colorList = binding.etColor.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() } as ArrayList<String>

        if (title.isEmpty() || price == 0.0 || picUrlList.isEmpty()) {
            Toast.makeText(this, "Tên, Giá, và URL ảnh không được trống", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        // 4. Tạo hoặc Cập nhật đối tượng (Elvis Operator)
        val product = currentItem ?: ItemsModel() // Lấy item cũ (Sửa) hoặc tạo item mới (Thêm)
        product.title = title
        product.description = description
        product.price = price
        product.oldPrice = oldPrice
        product.rating = rating
        product.picUrl = picUrlList
        product.size = sizeList
        product.color = colorList

        // 5. Lưu lên Firestore
        saveToFirestore(product)
    }

    private fun saveToFirestore(product: ItemsModel) {
        if (currentProductId == null) {
            // --- Chế độ THÊM MỚI ---
            db.collection("Items")
                .add(product)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK) // Trả kết quả OK để Activity trước reload
                    finish()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Thêm thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // --- Chế độ SỬA ---
            // Non null asserted operator
            db.collection("Items").document(currentProductId!!)
                .set(product) // Dùng set() để ghi đè toàn bộ object
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK) // Trả kết quả OK để Activity trước reload
                    finish()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Cập nhật thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}