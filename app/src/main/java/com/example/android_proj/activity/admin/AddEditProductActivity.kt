package com.example.android_proj.activity.admin

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.R
import com.example.android_proj.adapter.admin.ImagePreviewAdapter
import com.example.android_proj.databinding.ActivityAddEditProductBinding
import com.example.android_proj.model.ItemsModel
import com.example.android_proj.response.CloudinaryResponse // IMPORT MỚI
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson // IMPORT MỚI
import kotlinx.coroutines.CoroutineScope // IMPORT MỚI
import kotlinx.coroutines.Dispatchers // IMPORT MỚI
import kotlinx.coroutines.launch // IMPORT MỚI
import kotlinx.coroutines.withContext // IMPORT MỚI
import okhttp3.MediaType.Companion.toMediaTypeOrNull // IMPORT MỚI
import okhttp3.MultipartBody // IMPORT MỚI
import okhttp3.OkHttpClient // IMPORT MỚI
import okhttp3.Request // IMPORT MỚI
import okhttp3.RequestBody // IMPORT MỚI
import java.io.IOException // IMPORT MỚI
import java.util.UUID

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Cloudinary (Từ file của bạn)
    private val CLOUDINARY_CLOUD_NAME = "dgwnoquie" // TỪ EditProfile
    private val CLOUDINARY_UPLOAD_PRESET = "upload-1" // TỪ EditProfile

    // HTTP Client (Dùng chung)
    private val httpClient = OkHttpClient() // TỪ EditProfile
    private val gson = Gson() // TỪ EditProfile

    // Image Adapter
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    // Trạng thái Add/Edit
    private var mCurrentProductId: String? = null
    private var mProductToEdit: ItemsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Lấy ID sản phẩm (nếu là "edit")
        mCurrentProductId = intent.getStringExtra("PRODUCT_ID")

        initToolbar()
        initImagePicker()
        initRecyclerView()
        initListeners()

        // Kiểm tra chế độ Add hay Edit
        if (mCurrentProductId != null) {
            binding.toolbar.title = "Chỉnh sửa Sản phẩm"
            binding.btnSave.text = "Lưu thay đổi"
            loadProductDetails()
        } else {
            binding.toolbar.title = "Thêm Sản phẩm"
            binding.btnSave.text = "Lưu sản phẩm"
        }
    }

    // --- Các hàm khởi tạo (Không thay đổi) ---

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initImagePicker() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data?.clipData != null) {
                    // Người dùng chọn nhiều ảnh
                    val count = result.data!!.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = result.data!!.clipData!!.getItemAt(i).uri
                        imagePreviewAdapter.addImage(imageUri)
                    }
                } else if (result.data?.data != null) {
                    // Người dùng chọn một ảnh
                    val imageUri = result.data?.data
                    if (imageUri != null) {
                        imagePreviewAdapter.addImage(imageUri)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        imagePreviewAdapter = ImagePreviewAdapter(this) { sourceToRemove ->
            imagePreviewAdapter.removeImage(sourceToRemove)
        }
        binding.imagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddEditProductActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imagePreviewAdapter
        }
    }

    private fun initListeners() = with(binding) {
        btnAddImage.setOnClickListener { openImagePicker() }
        btnAddSize.setOnClickListener {
            showAddChipDialog("Thêm Size", "Nhập size (ví dụ: M)") { sizeText ->
                createChip(sizeText, sizeChipGroup)
            }
        }
        btnAddColor.setOnClickListener {
            showAddChipDialog("Thêm Màu", "Nhập tên màu (ví dụ: Đỏ)") { colorText ->
                createChip(colorText, colorChipGroup)
            }
        }
        btnSave.setOnClickListener {
            validateAndSaveProduct()
        }
    }

    // --- Các hàm tải dữ liệu và UI (Không thay đổi) ---

    private fun loadProductDetails() {
        showLoading(true)
        db.collection("items").document(mCurrentProductId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    mProductToEdit = document.toObject(ItemsModel::class.java)
                    if (mProductToEdit == null) {
                        Toast.makeText(this, "Lỗi đọc dữ liệu sản phẩm.", Toast.LENGTH_SHORT).show()
                        finish()
                        return@addOnSuccessListener
                    }
                    binding.apply {
                        etTitle.setText(mProductToEdit?.title)
                        etDescription.setText(mProductToEdit?.description)
                        etPrice.setText(mProductToEdit?.price.toString())
                        etOldPrice.setText(mProductToEdit?.oldPrice.toString())
                        etRating.setText(mProductToEdit?.rating.toString())
                        imagePreviewAdapter.setImages(mProductToEdit?.picUrl ?: emptyList())
                        sizeChipGroup.removeAllViews()
                        mProductToEdit?.size?.forEach { createChip(it, sizeChipGroup) }
                        colorChipGroup.removeAllViews()
                        mProductToEdit?.color?.forEach { createChip(it, colorChipGroup) }
                    }
                    showLoading(false)
                } else {
                    Toast.makeText(this, "Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Log.e("AddEditProduct", "Lỗi tải sản phẩm: ${it.message}")
                Toast.makeText(this, "Lỗi tải chi tiết sản phẩm.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImageLauncher.launch(intent)
    }

    private fun showAddChipDialog(title: String, hint: String, onOkClicked: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        val input = EditText(this)
        input.hint = hint
        builder.setView(input)
        builder.setPositiveButton("Thêm") { dialog, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                onOkClicked(text)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun createChip(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }

    private fun getChipsFromGroup(chipGroup: ChipGroup): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            list.add(chip.text.toString())
        }
        return list
    }

    // --- (THAY ĐỔI) Logic Lưu và Upload ---

    private fun validateAndSaveProduct() {
        val title = binding.etTitle.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val oldPrice = binding.etOldPrice.text.toString().toDoubleOrNull() ?: 0.0
        val rating = binding.etRating.text.toString().toDoubleOrNull() ?: 0.0

        if (title.isEmpty() || price <= 0) {
            Toast.makeText(this, "Tên sản phẩm và Giá là bắt buộc.", Toast.LENGTH_SHORT).show()
            return
        }

        val newImageUris = imagePreviewAdapter.getUris()
        val existingImageUrls = imagePreviewAdapter.getUrls()

        if (newImageUris.isEmpty() && existingImageUrls.isEmpty()) {
            Toast.makeText(this, "Sản phẩm phải có ít nhất một ảnh.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val sizes = getChipsFromGroup(binding.sizeChipGroup)
        val colors = getChipsFromGroup(binding.colorChipGroup)
        val docId = mCurrentProductId ?: db.collection("items").document().id
        val item = ItemsModel(
            id = docId,
            title = title,
            description = desc,
            price = price,
            oldPrice = oldPrice,
            rating = rating,
            size = sizes,
            color = colors,
            picUrl = ArrayList() // Sẽ cập nhật sau
        )

        // Bắt đầu quá trình upload và lưu (ĐÃ THAY ĐỔI)
        uploadImagesAndSave(newImageUris, existingImageUrls, item)
    }

    /**
     * THAY THẾ: Upload bằng Coroutine và OkHttp (Giống EditProfile)
     */
    private fun uploadImagesAndSave(
        newImageUris: List<Uri>,
        existingImageUrls: List<String>,
        itemData: ItemsModel
    ) {
        val finalImageUrls = ArrayList(existingImageUrls)
        val totalNewUploads = newImageUris.size

        if (totalNewUploads == 0) {
            // Không có ảnh mới, lưu luôn
            itemData.picUrl = finalImageUrls
            saveProductToFirestore(itemData)
            return
        }

        // Bắt đầu Coroutine trên Main thread
        CoroutineScope(Dispatchers.Main).launch {
            val uploadedUrls = mutableListOf<String>()
            var hasUploadFailed = false

            Toast.makeText(this@AddEditProductActivity, "Đang tải lên $totalNewUploads ảnh...", Toast.LENGTH_SHORT).show()

            // Chuyển sang IO thread để thực hiện upload
            withContext(Dispatchers.IO) {
                for (uri in newImageUris) {
                    val newUrl = uploadImageWithOkHttp(uri) // Hàm upload blocking
                    if (newUrl != null) {
                        uploadedUrls.add(newUrl)
                    } else {
                        // Một ảnh bị lỗi, dừng upload
                        hasUploadFailed = true
                        break
                    }
                }
            }

            // Quay lại Main thread để xử lý kết quả
            if (hasUploadFailed) {
                showLoading(false)
                Toast.makeText(this@AddEditProductActivity, "Một ảnh tải lên thất bại, đã hủy lưu.", Toast.LENGTH_LONG).show()
            } else {
                // Tất cả thành công, gộp URL và lưu
                finalImageUrls.addAll(uploadedUrls)
                itemData.picUrl = finalImageUrls
                saveProductToFirestore(itemData)
            }
        }
    }

    /**
     * HÀM MỚI: Upload MỘT ảnh bằng OkHttp (Logic từ EditProfile)
     * Chạy bên trong Dispatchers.IO, nên có thể là hàm blocking.
     * Trả về URL (String) nếu thành công, null nếu thất bại.
     */
    private fun uploadImageWithOkHttp(imageUri: Uri): String? {
        val user = auth.currentUser
        if (user == null) {
            Log.e("OkHttpUpload", "Không có user, dừng upload.")
            return null
        }

        // 1. Đọc file bytes (Giống EditProfile)
        val inputStream = contentResolver.openInputStream(imageUri)
        val fileBytes = inputStream?.readBytes()
        inputStream?.close()
        if (fileBytes == null) {
            Log.e("OkHttpUpload", "Không thể đọc file bytes từ Uri: $imageUri")
            return null
        }

        val url = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"
        val publicId = "items/${user.uid}/${UUID.randomUUID()}" // Tạo publicId duy nhất

        return try {
            // 2. Tạo Request Body (Giống EditProfile)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "item_image.jpg",
                    RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes))
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("public_id", publicId) // Thêm public_id
                .build()

            // 3. Tạo Request (Giống EditProfile)
            val request = Request.Builder().url(url).post(requestBody).build()

            // 4. Thực thi request (Giống EditProfile)
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("OkHttpUpload", "Upload thất bại: ${response.code} ${response.message}")
                    throw IOException("Unexpected code $response")
                }

                val responseBody = response.body?.string()
                // 5. Parse response (Giống EditProfile)
                parseCloudinaryResponse(responseBody)
            }
        } catch (e: Exception) {
            Log.e("OkHttpUpload", "Upload failed: ${e.message}", e)
            null // Trả về null nếu có lỗi
        }
    }

    /**
     * HÀM MỚI: Parse JSON bằng Gson (Từ EditProfile)
     * ĐÃ SỬA: Dùng transformation w_300, h_300 cho sản phẩm
     */
    private fun parseCloudinaryResponse(responseBody: String?): String? {
        if (responseBody == null) {
            Log.e("Cloudinary", "Response body is null.")
            return null
        }
        return try {
            val response = gson.fromJson(responseBody, CloudinaryResponse::class.java)
            val originalUrl = response.secure_url
            if (originalUrl.isNullOrEmpty()) {
                Log.e("Cloudinary", "Secure URL is missing in the response.")
                return null
            }
            // Thay đổi transformation cho phù hợp với ảnh sản phẩm (lớn hơn avatar)
            val transformations = "c_fill,w_300,h_300,f_auto,q_auto"
            val transformedUrl = originalUrl.replace("/upload/", "/upload/$transformations/")
            Log.d("Cloudinary", "Parsed URL: $transformedUrl")
            return transformedUrl
        } catch (e: Exception) {
            Log.e("Cloudinary", "Failed to parse JSON response using Gson: ${e.message}", e)
            null
        }
    }

    /**
     * Lưu vào Firestore (Không thay đổi)
     */
    private fun saveProductToFirestore(item: ItemsModel) {
        db.collection("items").document(item.id)
            .set(item)
            .addOnSuccessListener {
                showLoading(false)
                val message = if (mCurrentProductId == null) "Thêm sản phẩm thành công!" else "Cập nhật thành công!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Lỗi lưu vào Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }
}