package com.example.android_proj.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.databinding.ActivityRegisterBinding
import com.example.android_proj.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        backBtn.setOnClickListener { finish() }

        registerBtn.setOnClickListener {
            performRegistration()
        }

        loginLinkTxt.setOnClickListener {
            finish() // Quay lại màn hình Login
        }
    }

    private fun performRegistration() = with(binding) {
        val username = usernameEdt.text.toString().trim()
        val email = emailEdt.text.toString().trim()
        val password = passwordEdt.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@RegisterActivity, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
            return@with
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@RegisterActivity) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid // Lấy UID của user

                    if (userId == null) {
                        setLoading(false)
                        Toast.makeText(this@RegisterActivity, "Đăng ký thất bại, không lấy được UID.", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    // 1. Cập nhật Display Name (Username) trong Auth
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username).build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {

                            // 2. TẠO DOCUMENT TRONG FIRESTORE
                            // Tạo một Map (dữ liệu) cho user mới
                            val newUser = UserModel(
                                userId = userId, // Sẽ bị @Exclude bỏ qua khi lưu
                                email = email,
                                name = username, // Gán 'username' từ form vào 'name'
                                phoneNumber = "", // Khởi tạo rỗng
                                avatarUrl = "",   // Khởi tạo rỗng
                                role = "user"     // Giá trị mặc định
                                // Trường 'createdAt' sẽ tự động được gán bởi @ServerTimestamp
                            )

                            // Ghi vào collection "users" với ID là UID của user
                            db.collection("users").document(userId)
                                .set(newUser)
                                .addOnSuccessListener {
                                    // Thành công!
                                    setLoading(false)
                                    Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    goToDashboard()
                                }
                                .addOnFailureListener { e ->
                                    // Lỗi khi ghi vào Firestore
                                    setLoading(false)
                                    Toast.makeText(this@RegisterActivity, "Auth thành công, nhưng lỗi tạo user document: ${e.message}", Toast.LENGTH_SHORT).show()
                                    // Bạn có thể cân nhắc xóa user Auth ở đây nếu muốn
                                }

                        } else {
                            // Lỗi cập nhật profile Auth, nhưng vẫn nên tạo Firestore
                            // (Bạn có thể sao chép code tạo doc ở trên vào đây nếu muốn)
                            setLoading(false)
                            Toast.makeText(this@RegisterActivity, "Đăng ký thành công, nhưng lỗi cập nhật Username (Auth).", Toast.LENGTH_SHORT).show()
                            goToDashboard() // Vẫn đi tiếp
                        }
                    }
                } else {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.registerBtn.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.registerProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}