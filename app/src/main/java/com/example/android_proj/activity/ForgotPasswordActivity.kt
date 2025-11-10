package com.example.android_proj.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. GỌI enableEdgeToEdge NGAY LẬP TỨC
        enableEdgeToEdge()

        // 2. Thiết lập Binding và Layout
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupListeners()

        // Bạn có thể cần thêm đoạn code này nếu layout root của bạn chưa xử lý insets:
        // ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
        //     val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //     insets
        // }
    }

    private fun setupListeners() = with(binding) {
        backBtn.setOnClickListener { finish() }

        sendResetBtn.setOnClickListener {
            sendPasswordReset()
        }
    }

    private fun sendPasswordReset() = with(binding) {
        val email = emailEdt.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this@ForgotPasswordActivity, "Vui lòng nhập địa chỉ Email của bạn.", Toast.LENGTH_SHORT).show()
            return@with
        }

        setLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this@ForgotPasswordActivity) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "Liên kết đặt lại mật khẩu đã được gửi đến Email của bạn.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Không tìm thấy Email này hoặc lỗi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.sendResetBtn.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.resetProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}