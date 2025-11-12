package com.example.android_proj.activity.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
    }

    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val users = documents.toObjects(UserModel::class.java).toMutableList()

                // Gán Document ID
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

    override fun onEditRoleClick(user: UserModel) {
        val roles = arrayOf("user", "admin") // Các lựa chọn
        val currentRoleIndex = roles.indexOf(user.role)

        AlertDialog.Builder(this)
            .setTitle("Chọn vai trò cho ${user.email}")
            .setSingleChoiceItems(roles, currentRoleIndex) { dialog, which ->
                val selectedRole = roles[which]
                updateUserRole(user, selectedRole)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun updateUserRole(user: UserModel, newRole: String) {
        if (user.userId.isEmpty()) return

        db.collection("users").document(user.userId)
            .update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật vai trò thành công", Toast.LENGTH_SHORT).show()
                loadUsers() // Tải lại danh sách
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Cập nhật thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}