package com.example.android_proj.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.R
import com.example.android_proj.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth // Đảm bảo đã thêm dependency Firebase Auth

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        loadUserProfile()
        setupListeners()
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Hiển thị Email
            binding.emailTxt.text = user.email ?: getString(R.string.no_email_available)

            // Hiển thị Username:
            // Nếu có display name từ Auth, dùng nó. Nếu không, dùng phần đầu của email.
            val username = user.displayName ?: user.email?.substringBefore('@') ?: "Guest User"
            binding.usernameTxt.text = username
        } else {
            // Trường hợp chưa đăng nhập (không nên xảy ra nếu đã có Auth Guard)
            binding.usernameTxt.text = "Không đăng nhập"
            binding.emailTxt.text = ""
        }
    }

    private fun setupListeners() = with(binding) {
        backBtn.setOnClickListener { finish() }

        logoutBtn.setOnClickListener {
            performLogout()
        }

        // Thêm xử lý cho các mục menu khác nếu cần
        ordersLayout.setOnClickListener {
            Toast.makeText(this@ProfileActivity, "Chuyển đến Lịch sử đơn hàng", Toast.LENGTH_SHORT).show()
        }
        addressLayout.setOnClickListener {
            Toast.makeText(this@ProfileActivity, "Chuyển đến Địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
        }
        settingsLayout.setOnClickListener {
            Toast.makeText(this@ProfileActivity, "Chuyển đến Cài đặt", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show()

        // Chuyển hướng về màn hình Đăng nhập/Home (tùy thuộc vào luồng ứng dụng)
        // Ví dụ: Chuyển về Dashboard và xóa stack Activity
        val intent = Intent(this, DashboardActivity::class.java) // Thay bằng LoginActivity nếu có
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}