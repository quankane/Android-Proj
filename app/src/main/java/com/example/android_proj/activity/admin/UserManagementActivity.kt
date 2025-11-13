package com.example.android_proj.activity.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.adapter.UserManagementAdapter
import com.example.android_proj.databinding.ActivityUserManagementBinding
import com.example.android_proj.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore

class UserManagementActivity : AppCompatActivity(), UserManagementAdapter.UserClickListener {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var adapter: UserManagementAdapter
    private val db = FirebaseFirestore.getInstance()
    private var userList = mutableListOf<UserModel>()

    // Launcher để refresh list khi Thêm/Sửa
    private val addEditUserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUsers() // Tải lại danh sách
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        loadUsers()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = UserManagementAdapter(userList, this, this)
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.usersRecyclerView.adapter = adapter

        // Listener cho nút THÊM
        binding.fabAddUser.setOnClickListener {
            // Mở AddEditUserActivity ở chế độ "Thêm" (không gửi ID)
            val intent = Intent(this, AddEditUserActivity::class.java)
            addEditUserLauncher.launch(intent)
        }
    }

    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("users")
            .whereNotEqualTo("email", "quanducbui2017@gmail.com")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val users = documents.toObjects(UserModel::class.java).toMutableList()

                for (i in users.indices) {
                    users[i].userId = documents.documents[i].id
                }

                adapter.updateData(users)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("UserManagement", "Lỗi tải users", e)
                Toast.makeText(this, "Lỗi tải users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Xử lý click vào item (để SỬA)
    override fun onUserClick(user: UserModel) {
        // Mở AddEditUserActivity ở chế độ "Sửa" (gửi kèm ID)
        val intent = Intent(this, AddEditUserActivity::class.java).apply {
            putExtra("USER_ID", user.userId)
        }
        addEditUserLauncher.launch(intent)
    }

    // Xử lý click vào nút XÓA
    override fun onDeleteClick(user: UserModel) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa user '${user.email}'? \n\nLƯU Ý: Thao tác này chỉ xóa dữ liệu (role, name) của user khỏi Firestore, không xóa tài khoản Đăng nhập (Authentication).")
            .setPositiveButton("Xóa") { _, _ ->
                deleteUserFromFirestore(user)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Chỉ "Soft Delete" (Xóa document Firestore, không xóa Auth)
    private fun deleteUserFromFirestore(user: UserModel) {
        if (user.userId.isEmpty()) return

        db.collection("users").document(user.userId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa user: ${user.email}", Toast.LENGTH_SHORT).show()
                adapter.removeUser(user) // Xóa khỏi RecyclerView
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Xóa thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Xóa Role (cách cũ - không dùng nữa)
    // override fun onEditRoleClick(user: UserModel) { ... }
    // private fun updateUserRole(user: UserModel, newRole: String) { ... }
}