package com.example.android_proj.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
// BỎ: import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cloudinary.android.MediaManager // THÊM
import com.cloudinary.android.callback.ErrorInfo // THÊM
import com.cloudinary.android.callback.UploadCallback // THÊM
import com.example.android_proj.R
import com.example.android_proj.databinding.ActivityAddEditProductBinding
import com.example.android_proj.model.ItemsModel // THAY ĐỔI: Dùng ItemsModel
// BỎ: import com.example.android_proj.response.CloudinaryResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// BỎ: import com.google.gson.Gson
// BỎ: import kotlinx.coroutines.Dispatchers
// BỎ: import kotlinx.coroutines.launch
// BỎ: import kotlinx.coroutines.withContext
// BỎ: import okhttp3.MediaType.Companion.toMediaTypeOrNull
// BỎ: import okhttp3.MultipartBody
// BỎ: import okhttp3.OkHttpClient
// BỎ: import okhttp3.Request
// BỎ: import okhttp3.RequestBody
// BỎ: import java.io.IOException
import java.util.UUID

class AddEditProductActivity : AppCompatActivity() {

    // ... (existing properties)
    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val CLOUDINARY_CLOUD_NAME = "dgwnoquie" // GIỮ LẠI
    private val CLOUDINARY_UPLOAD_PRESET = "upload-1" // THÊM

    private var mCurrentProductId: String? = null
    // ... (existing code)
    private var mExistingImageUrl: String? = null // URL ảnh hiện tại (nếu là "edit")

    companion object {
// ... (existing code)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
// ... (existing code)
// ... (existing code)
// ... (existing code)
// ... (existing code)
        if (mCurrentProductId != null) {
            binding.toolbar.title = "Chỉnh sửa Sản phẩm"
            loadProductDetails() // Tên hàm không đổi, logic bên trong thay đổi
// ... (existing code)
        }
    }

    // Logic load dữ liệu (tương tự loadCurrentProfile)
    private fun loadProductDetails() {
// ... (existing code)
// ... (existing code)
// ... (existing code)
        .addOnFailureListener {
            showLoading(false)
            Log.e("AddEditProduct", "Lỗi tải sản phẩm: ${it.message}")
        }
    }

    private fun setupListeners() = with(binding) {
// ... (existing code)
// ... (existing code)
// ... (existing code)
    }
}

// ... (openImagePicker and setupImagePicker remain the same)
// Giống hệt EditProfileActivity
private fun openImagePicker() {
    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickImageLauncher.launch(intent)
}

// Logic ImagePicker (gần giống, nhưng không tải lên ngay)
private fun setupImagePicker() {
    pickImageLauncher = registerForActivityResult(
// ... (existing code)
// ... (existing code)
// ... (existing code)
        // Chỉ hiển thị ảnh, không upload ngay
        Glide.with(this)
            .load(mSelectedImageUri)
            .transform(RoundedCorners(16))
            .into(binding.productImageImg)
}
}
}
}

private fun validateAndSaveProduct() {
// ... (existing code)
// ... (existing code)
// ... (existing code)
    // BỎ QUA: stock
    // val stockStr = binding.productStockEdt.text.toString().trim()

// ... (existing code)
// ... (existing code)
// ... (existing code)
    showLoading(true)

    // ... (Quy trình lưu 1, 2, 3 không thay đổi logic)

    if (mSelectedImageUri != null) {
        // 1. Tải ảnh mới lên
        // THAY ĐỔI: Gọi hàm SDK
        uploadImageToCloudinarySDK(mSelectedImageUri!!) { newImageUrl ->
            // Sau khi có URL ảnh, lưu vào Firestore
            // THAY ĐỔI: Bỏ 'stockStr'
            saveProductToFirestore(newImageUrl, name, desc, priceStr)
        }
    } else if (mCurrentProductId != null) {
// ... (existing code)
        // THAY ĐỔI: Bỏ 'stockStr'
        saveProductToFirestore(mExistingImageUrl, name, desc, priceStr)
    } else {
// ... (existing code)
        Toast.makeText(this, "Vui lòng chọn ảnh cho sản phẩm.", Toast.LENGTH_SHORT).show()
    }
}

// BỎ: Hàm uploadImageToCloudinary (OkHttp)
// BỎ: Hàm parseCloudinaryResponse (Gson)

/**
 * THAY THẾ: Upload ảnh bằng Cloudinary SDK (giống EditProfileActivity_SDK)
 * @param imageUri Uri của ảnh
 * @param onSuccess Callback trả về URL_SAU_KHI_BIẾN_ĐỔI
 */
private fun uploadImageToCloudinarySDK(imageUri: Uri, onSuccess: (String) -> Unit) {
    val user = auth.currentUser
    if (user == null) {
        showLoading(false)
        Toast.makeText(this, "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show()
        return
    }
    Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()

    val publicId = "items/${user.uid}/${UUID.randomUUID()}"
    val transformations = "c_fill,w_300,h_300,f_auto,q_auto"

    MediaManager.get().upload(imageUri)
        .unsigned(CLOUDINARY_UPLOAD_PRESET)
        .option("public_id", publicId)
        .option("overwrite", false) // Không cần ghi đè vì publicId là duy nhất
        .callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                showLoading(true) // Đảm bảo đang show loading
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                // Có thể" cập nhật progress bar nếu muốn
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val secureUrl = resultData["secure_url"] as? String
                if (secureUrl != null) {
                    // ÁP DỤNG TRANSFORMATION (giống logic parse cũ của bạn)
                    val transformedUrl = secureUrl.replace("/upload/", "/upload/$transformations/")
                    Log.d("CloudinarySDK", "Upload thành công: $transformedUrl")
                    onSuccess(transformedUrl) // Trả về URL đã biến đổi
                } else {
                    Log.e("CloudinarySDK", "Lỗi phân tích URL từ kết quả: $resultData")
                    Toast.makeText(this@AddEditProductActivity, "Lỗi phân tích URL Cloudinary.", Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                Log.e("CloudinarySDK", "Upload thất bại: ${error.description}", error.exception)
                Toast.makeText(this@AddEditProductActivity, "Tải ảnh lên thất bại: ${error.description}", Toast.LENGTH_LONG).show()
                showLoading(false)
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                // Xảy ra khi upload bị gián đoạn và được lên lịch lại
            }
        }).dispatch()
}


// Hàm lưu vào Firestore (Tương tự saveProfileChanges)
private fun saveProductToFirestore(
// ... (existing code)
// ... (existing code)
// ... (existing code)
    // BỎ QUA: stockStr
) {
    val user = auth.currentUser
// ... (existing code)
// ... (existing code)
// ... (existing code)
    return
}

if (imageUrl.isNullOrEmpty()) {
// ... (existing code)
// ... (existing code)
// ... (existing code)
    return
}

// Quyết định ID: Lấy ID cũ (edit) hoặc tạo ID mới (add)
// ... (existing code)
// ... (existing code)
// ... (existing code)
val docId = mCurrentProductId ?: db.collection("items").document().id

// THAY ĐỔI: Logic lưu cho Add/Edit
// ... (existing code)
// ... (existing code)
// ... (existing code)
val item = ItemsModel(
    id = docId,
    title = name,
    description = desc,
    price = priceStr.toDoubleOrNull() ?: 0.0,
    picUrl = arrayListOf(imageUrl), // THAY ĐỔI: Lưu vào list
    // sellerId KHÔNG CÓ TRONG ItemsModel CỦA BẠN
    // sellerId = user.uid
    // Các trường khác (size, color, rating...) sẽ dùng giá trị mặc định
)
db.collection("items").document(docId).set(item)
.addOnSuccessListener {
// ... (existing code)
// ... (existing code)
// ... (existing code)
}
.addOnFailureListener { e ->
    showLoading(false)
    Toast.makeText(this, "Lỗi lưu vào Firestore: ${e.message}", Toast.LENGTH_LONG).show()
}
} else { // EDIT Mode
// ... (existing code)
// ... (existing code)
// ... (existing code)
    val updates = mapOf(
        "title" to name,
        "description" to desc,
        "price" to (priceStr.toDoubleOrNull() ?: 0.0),
        "picUrl" to arrayListOf(imageUrl) // GHI ĐÈ list ảnh chỉ với ảnh này
    )
    db.collection("items").document(docId).update(updates)
        .addOnSuccessListener {
// ... (existing code)
// ... (existing code)
// ... (existing code)
        }
        .addOnFailureListener { e ->
            showLoading(false)
            Toast.makeText(this, "Lỗi cập nhật Firestore: ${e.message}", Toast.LENGTH_LONG).show()
        }
}
}

private fun showLoading(isLoading: Boolean) {
    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    binding.saveBtn.isEnabled = !isLoading
}
}