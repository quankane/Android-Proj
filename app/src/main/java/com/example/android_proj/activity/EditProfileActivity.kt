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
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.android_proj.R // Đảm bảo import R
import com.example.android_proj.databinding.ActivityEditProfileBinding
import com.example.android_proj.model.UserModel // THÊM IMPORT NÀY
import com.example.android_proj.response.CloudinaryResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore // THÊM IMPORT NÀY
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
    private lateinit var db: FirebaseFirestore // THÊM FIRESTORE
    private lateinit var storageRef: StorageReference
    private val CLOUDINARY_CLOUD_NAME = "dgwnoquie"
    private val CLOUDINARY_UPLOAD_PRESET = "upload-1"

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // KHỞI TẠO FIRESTORE
        storageRef = FirebaseStorage.getInstance().reference

        setupImagePicker()
        loadCurrentProfile()
        setupListeners()
    }

    private fun loadCurrentProfile() = with(binding) {
        val user = auth.currentUser
        if (user != null) {
            // 1. Tải từ Firebase Auth (Email, Tên, Ảnh)
            usernameEdt.setText(user.displayName)
            emailEdt.setText(user.email)

            if (user.photoUrl != null) {
                Glide.with(this@EditProfileActivity)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_user_profile)
                    .transform(CircleCrop())
                    .into(avatarImg)
            }

            // 2. Tải từ Firestore (SĐT)
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userModel = document.toObject(UserModel::class.java)
                        phoneEdt.setText(userModel?.phoneNumber) // <-- HIỂN THỊ SĐT
                    } else {
                        Log.w("EditProfile", "User document không tồn tại trong Firestore!")
                    }
                }
                .addOnFailureListener {
                    Log.e("EditProfile", "Lỗi tải SĐT: ${it.message}")
                }
        } else {
            Toast.makeText(this@EditProfileActivity, "Lỗi: Không tìm thấy người dùng.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() = with(binding) {
        // ... (toolbar, changePasswordBtn, avatarImg, logoutBtn giữ nguyên) ...
        toolbar.setNavigationOnClickListener { finish() }
        avatarImg.setOnClickListener { openImagePicker() }
        changePasswordBtn.setOnClickListener { sendPasswordResetEmail() }
        logoutBtn.setOnClickListener { performLogout() }

        // Chỉ sửa saveBtn
        saveBtn.setOnClickListener {
            // Sửa hàm: Sẽ lưu cả Auth và Firestore
            saveProfileChanges()
        }
    }

    private fun openImagePicker() {
        // ... (giữ nguyên)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun setupImagePicker() {
        // ... (giữ nguyên)
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    Glide.with(this).load(imageUri).transform(CircleCrop()).into(binding.avatarImg)
                    uploadImageToCloudinary(imageUri)
                }
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        // ... (hàm này giữ nguyên, vì nó gọi updateAuthProfilePhotoUrl) ...
        val user = auth.currentUser ?: return
        Toast.makeText(this, "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show()
        val inputStream = contentResolver.openInputStream(imageUri)
        val fileBytes = inputStream?.readBytes()
        inputStream?.close()
        if (fileBytes == null) {
            Toast.makeText(this, "Không thể đọc dữ liệu ảnh.", Toast.LENGTH_LONG).show()
            return
        }
        val url = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "profile_${user.uid}.jpg",
                        RequestBody.create("image/*".toMediaTypeOrNull(), fileBytes))
                    .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                    .build()
                val request = Request.Builder().url(url).post(requestBody).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val responseBody = response.body?.string()
                    val newPhotoUrl = parseCloudinaryResponse(responseBody)
                    if (newPhotoUrl != null) {
                        runOnUiThread {
                            updateAuthProfilePhotoUrl(newPhotoUrl)
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

    private fun parseCloudinaryResponse(responseBody: String?): String? {
        // ... (hàm này giữ nguyên) ...
        if (responseBody == null) {
            Log.e("Cloudinary", "Response body is null.")
            return null
        }
        return try {
            val gson = Gson()
            val response = gson.fromJson(responseBody, CloudinaryResponse::class.java)
            val originalUrl = response.secure_url
            if (originalUrl.isNullOrEmpty()) {
                Log.e("Cloudinary", "Secure URL is missing in the response.")
                return null
            }
            val transformations = "c_fill,w_100,h_100,r_max,f_auto,q_auto"
            return originalUrl.replace("/upload/", "/upload/$transformations/")
        } catch (e: Exception) {
            Log.e("Cloudinary", "Failed to parse JSON response using Gson: ${e.message}", e)
            null
        }
    }

    // --- SỬA HÀM NÀY: Phải cập nhật cả Firestore ---
    private fun updateAuthProfilePhotoUrl(newPhotoUrl: String) {
        val user = auth.currentUser ?: return

        // 1. Cập nhật Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(newPhotoUrl))
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cập nhật ảnh Auth thành công!", Toast.LENGTH_SHORT).show()

                    // 2. Cập nhật Firestore
                    db.collection("users").document(user.uid)
                        .update("avatarUrl", newPhotoUrl) // <-- LƯU VÀO FIRESTORE
                        .addOnSuccessListener {
                            Log.d("EditProfile", "Cập nhật avatarUrl trong Firestore thành công.")
                            // Tải lại ảnh (giữ nguyên)
                            Glide.with(this)
                                .load(newPhotoUrl) // Dùng URL mới
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(R.drawable.ic_user_profile)
                                .transform(CircleCrop())
                                .into(binding.avatarImg)
                        }
                        .addOnFailureListener {
                            Log.e("EditProfile", "Lỗi cập nhật avatarUrl Firestore: ${it.message}")
                        }
                } else {
                    Toast.makeText(this, "Lỗi cập nhật hồ sơ Firebase Auth.", Toast.LENGTH_LONG).show()
                }
            }
    }

    // --- SỬA HÀM NÀY: Phải cập nhật cả Firestore ---
    private fun saveProfileChanges() {
        val newUsername = binding.usernameEdt.text.toString().trim()
        val newPhone = binding.phoneEdt.text.toString().trim() // <-- LẤY SĐT MỚI
        val user = auth.currentUser ?: return

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Tên người dùng không được để trống.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Cập nhật Firebase Auth (Chỉ displayName)
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        user.updateProfile(profileUpdates).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {

                // 2. Cập nhật Firestore (name và phoneNumber)
                val userUpdates = mapOf(
                    "name" to newUsername,
                    "phoneNumber" to newPhone
                )

                db.collection("users").document(user.uid)
                    .update(userUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(this@EditProfileActivity, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@EditProfileActivity, "Lỗi cập nhật Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this@EditProfileActivity, "Lỗi cập nhật Auth: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendPasswordResetEmail() {
        // ... (giữ nguyên)
        val email = auth.currentUser?.email
        if (email != null) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Liên kết đặt lại mật khẩu đã được gửi đến Email của bạn.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Lỗi khi gửi email đặt lại mật khẩu.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performLogout() {
        // ... (giữ nguyên)
        auth.signOut()
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}