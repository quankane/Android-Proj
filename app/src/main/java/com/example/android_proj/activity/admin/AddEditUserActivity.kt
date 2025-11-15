package com.example.android_proj.activity.admin

import android.R
import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.android_proj.databinding.ActivityAddEditUserBinding
import com.example.android_proj.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AddEditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditUserBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private var currentUserId: String? = null // Biến lưu ID khi ở chế độ Sửa
    private var currentUserModel: UserModel? = null // Biến lưu user data khi Sửa

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Sử dụng ViewBinding để liên kết layout
        binding = ActivityAddEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        currentUserId = intent.getStringExtra("USER_ID") // Lấy ID từ Intent

        initView()

        // Kiểm tra xem đây là chế độ "Thêm" hay "Sửa"
        if (currentUserId == null) {
            setupAddMode()
        } else {
            setupEditMode()
        }
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSave.setOnClickListener { handleSave() }
        binding.btnSendResetPassword.setOnClickListener { sendPasswordReset() }


        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(R.attr.state_checked), // Trạng thái checked
                intArrayOf(-R.attr.state_checked) // Trạng thái unchecked (default)
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.holo_blue_light), // Màu khi checked
                ContextCompat.getColor(this, R.color.darker_gray)  // Màu khi unchecked
            )
        )

        binding.radioUser.buttonTintList = colorStateList
        binding.radioAdmin.buttonTintList = colorStateList
    }

    // --- CHẾ ĐỘ THÊM MỚI ---
    private fun setupAddMode() {
        binding.toolbar.title = "Thêm Người dùng mới"
        binding.layoutPassword.visibility = View.VISIBLE
        binding.btnSendResetPassword.visibility = View.GONE
        binding.layoutEmail.isEnabled = true
    }

    // --- CHẾ ĐỘ SỬA ---
    private fun setupEditMode() {
        binding.toolbar.title = "Chỉnh sửa Người dùng"
        // Ẩn mật khẩu, không cho sửa email, hiện nút Reset
        binding.layoutPassword.visibility = View.GONE
        binding.btnSendResetPassword.visibility = View.VISIBLE
        binding.layoutEmail.isEnabled = false // Không cho sửa email

        loadUserDetails()
    }

    // Tải dữ liệu user (chỉ ở chế độ Sửa)
    private fun loadUserDetails() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("users").document(currentUserId!!)
            .get().addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                if (document.exists()) {
                    currentUserModel = document.toObject(UserModel::class.java)
                    currentUserModel?.userId = document.id
                    populateForm(currentUserModel) // Điền data vào form
                } else {
                    Toast.makeText(this, "Không tìm thấy user", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Lỗi tải dữ liệu: ${it.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    // Điền dữ liệu vào form (Sửa)
    private fun populateForm(user: UserModel?) {
        user ?: return
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phoneNumber) // <-- ĐIỀN SĐT

        if (user.role == "admin") {
            binding.radioAdmin.isChecked = true
        } else {
            binding.radioUser.isChecked = true
        }
    }

    // Xử lý khi nhấn LƯU
    private fun handleSave() {
        if (currentUserId == null) {
            performAddUser()
        } else {
            performUpdateUser()
        }
    }

    // Logic THÊM
    private fun performAddUser() {
        // Lưu thông tin đăng nhập của admin để ko bị chuyển hướng khi tạo user mới
        val adminEmail = auth.currentUser?.email
        val adminPassword = "quankane"

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()   // <-- LẤY SĐT
        val role = if (binding.radioAdmin.isChecked) "admin" else "user"

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên, Email, Mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        // 1. Tạo user trong Authentication
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            val newUid = authResult.user?.uid
            if (newUid == null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Tạo Auth thất bại, UID rỗng", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // 2. Tạo user document trong Firestore
            val userModel = UserModel(
                userId = newUid,
                email = email,
                name = name,
                role = role,
                phoneNumber = phone,  // <-- LƯU SĐT
            )
            db.collection("users").document(newUid).set(userModel).addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Thêm user thành công!", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK) // Báo cho list refresh
                finish()
            }.addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Lỗi lưu Firestore: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
            if (adminEmail != null) {
                auth.signInWithEmailAndPassword(adminEmail, adminPassword)
                    .addOnSuccessListener {
                        // Đăng nhập lại Admin thành công!
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Thêm user thành công! Admin đã đăng nhập lại.",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }.addOnFailureListener { e ->
                        // Lỗi đăng nhập lại (rất hiếm nếu password đúng)
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Thêm user thành công, nhưng lỗi đăng nhập lại Admin: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        auth.signOut()
                    }
            }
        }.addOnFailureListener { e ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Lỗi tạo Auth: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Logic SỬA
    private fun performUpdateUser() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()   // <-- LẤY SĐT
        val role = if (binding.radioAdmin.isChecked) "admin" else "user"

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được trống", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        // Cập nhật các trường
        db.collection("users").document(currentUserId!!).update(
            mapOf(
                "name" to name,
                "role" to role,
                "phoneNumber" to phone,
            )
        ).addOnSuccessListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK) // Báo cho list refresh
            finish()
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Cập nhật thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Gửi Email Reset
    private fun sendPasswordReset() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Email rỗng", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email).addOnSuccessListener {
            Toast.makeText(this, "Đã gửi email reset đến: $email", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}