package com.example.android_proj.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.android_proj.databinding.ActivityEditProfileBinding
import com.example.android_proj.response.CloudinaryResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private val CLOUDINARY_CLOUD_NAME = "dgwnoquie" // Thay thế bằng Cloud Name của bạn
    private val CLOUDINARY_UPLOAD_PRESET = "upload-1"

    // Launcher để chọn ảnh từ Gallery
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        setupImagePicker() // Khởi tạo Launcher
        loadCurrentProfile()
        setupListeners()
    }

    private fun loadCurrentProfile() = with(binding) {
        val user = auth.currentUser
        if (user != null) {
            usernameEdt.setText(user.displayName)
            emailEdt.setText(user.email)

            // Tải ảnh đại diện hiện tại (nếu có)
            println("PhotoURL = " + user.photoUrl)
            if (user.photoUrl != null) {
                Glide.with(this@EditProfileActivity)
                    .load(user.photoUrl)
                    .placeholder(com.example.android_proj.R.drawable.ic_user_profile)
                    .transform(CircleCrop())
                    .into(avatarImg)
            }
        } else {
            Toast.makeText(this@EditProfileActivity, "Lỗi: Không tìm thấy người dùng.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() = with(binding) {
        backBtn.setOnClickListener { finish() }

        saveBtn.setOnClickListener {
            saveProfileChanges()
        }

        changePasswordBtn.setOnClickListener {
            sendPasswordResetEmail()
        }

        // KÍCH HOẠT CHỌN ẢNH KHI NHẤN VÀO AVATAR
        avatarImg.setOnClickListener {
            openImagePicker()
        }

        logoutBtn.setOnClickListener {
            performLogout()
        }
    }

    private fun openImagePicker() {
        // Tạo Intent để mở thư viện ảnh
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // SỬA ĐỔI trong setupImagePicker() để gọi hàm mới:
    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    // Hiển thị ảnh mới trên giao diện tạm thời
                    Glide.with(this).load(imageUri).into(binding.avatarImg)

                    // *** THAY THẾ HÀM CŨ BẰNG HÀM MỚI ***
                    uploadImageToCloudinary(imageUri) // <--- SỬ DỤNG HÀM MỚI
                }
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        val user = auth.currentUser ?: return
        Toast.makeText(this, "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show()

        // 1. Chuẩn bị tệp ảnh
        val inputStream = contentResolver.openInputStream(imageUri)
        val fileBytes = inputStream?.readBytes()
        inputStream?.close()

        if (fileBytes == null) {
            Toast.makeText(this, "Không thể đọc dữ liệu ảnh.", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Định nghĩa API URL
        val url = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"

        // 3. Khởi chạy Coroutine để thực hiện thao tác mạng
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                // Xây dựng body request (Multipart Form Data)
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "profile_${user.uid}.jpg", // tên file
                        RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes))
                    .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET) // preset
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                // Thực hiện request
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseBody = response.body?.string()

                    // Phân tích JSON Response để lấy secure_url
                    val newPhotoUrl = parseCloudinaryResponse(responseBody)

                    // Quay lại luồng chính để cập nhật UI/Auth
                    if (newPhotoUrl != null) {
                        runOnUiThread {
                            updateAuthProfilePhotoUrl(newPhotoUrl) // Hàm này đã có sẵn
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@EditProfileActivity, "Lỗi phân tích URL Cloudinary.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload failed: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Tải ảnh lên Cloudinary thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Hàm đơn giản để lấy URL từ JSON Response
    private fun parseCloudinaryResponse(responseBody: String?): String? {
        if (responseBody == null) {
            Log.e("Cloudinary", "Response body is null.")
            return null
        }

        return try {
            // Khởi tạo Gson
            val gson = Gson()

            // Phân tích chuỗi JSON thành đối tượng CloudinaryResponse
            val response = gson.fromJson(responseBody, CloudinaryResponse::class.java)

            // Lấy URL và THÊM BIẾN ĐỔI (để ảnh có hình tròn 100x100)
            val originalUrl = response.secure_url

            if (originalUrl.isNullOrEmpty()) {
                Log.e("Cloudinary", "Secure URL is missing in the response.")
                return null
            }

            // CHÈN PHÉP BIẾN ĐỔI CLOUDINARY VÀO URL (Như đã thảo luận trước đó)
            val transformations = "c_fill,w_100,h_100,r_max,f_auto,q_auto"
            return originalUrl.replace("/upload/", "/upload/$transformations/")

        } catch (e: Exception) {
            // Bắt các lỗi phân tích JSON
            Log.e("Cloudinary", "Failed to parse JSON response using Gson: ${e.message}", e)
            null
        }
    }

    private fun updateAuthProfilePhotoUrl(newPhotoUrl: String) {
        val user = auth.currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(newPhotoUrl))
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
                    // Tải lại ảnh để đảm bảo Glide lưu cache đúng
                    println("Photo url = " + user.photoUrl)
                    Glide.with(this)
                        .load(user.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Không lưu cache đĩa
                        .skipMemoryCache(true)                     // Bỏ qua cache bộ nhớ
                        .placeholder(com.example.android_proj.R.drawable.ic_user_profile)
                        .apply(RequestOptions().transform(CenterInside()))
                        .into(binding.avatarImg)
                } else {
                    Toast.makeText(this, "Lỗi cập nhật hồ sơ Firebase Auth.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveProfileChanges() {
        val newUsername = binding.usernameEdt.text.toString().trim()
        val user = auth.currentUser

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Tên người dùng không được để trống.", Toast.LENGTH_SHORT).show()
            return
        }

        user?.let {
            if (newUsername != it.displayName) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build()

                it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Cập nhật tên người dùng thành công!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Cập nhật tên thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this@EditProfileActivity, "Không có thay đổi nào để lưu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email
        if (email != null) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Liên kết đặt lại mật khẩu đã được gửi đến Email của bạn. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Lỗi khi gửi email đặt lại mật khẩu.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}