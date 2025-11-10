package com.example.android_proj.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        loadCurrentProfile()
        setupListeners()
    }

    private fun loadCurrentProfile() = with(binding) {
        val user = auth.currentUser
        if (user != null) {
            usernameEdt.setText(user.displayName)
            emailEdt.setText(user.email)
            // (Avatar logic would go here)
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
            // Chức năng này được xử lý bằng cách gửi email reset password
            sendPasswordResetEmail()
        }
    }

    private fun saveProfileChanges() = with(binding) {
        val newUsername = usernameEdt.text.toString().trim()
        val user = auth.currentUser

        if (newUsername.isEmpty()) {
            Toast.makeText(this@EditProfileActivity, "Tên người dùng không được để trống.", Toast.LENGTH_SHORT).show()
            return@with null
        }

        user?.let {
            // Chỉ cập nhật Display Name
            if (newUsername != it.displayName) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build()

                it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Cập nhật hồ sơ thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
}