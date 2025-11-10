package com.example.android_proj.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_proj.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

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
                    // Cập nhật Display Name (Username)
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username).build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        setLoading(false)
                        if (profileTask.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, "Đăng ký thành công! Đã cập nhật Username.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Đăng ký thành công, nhưng lỗi cập nhật Username.", Toast.LENGTH_LONG).show()
                        }
                        goToDashboard()
                    }
                } else {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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