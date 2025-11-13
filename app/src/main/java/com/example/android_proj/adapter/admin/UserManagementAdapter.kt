package com.example.android_proj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_proj.R
import com.example.android_proj.databinding.ItemUserManagementBinding
import com.example.android_proj.model.UserModel

class UserManagementAdapter(
    private var users: MutableList<UserModel>,
    private val context: Context,
    private val listener: UserClickListener // Sửa tên interface
) : RecyclerView.Adapter<UserManagementAdapter.ViewHolder>() {

    // Sửa interface
    interface UserClickListener {
        fun onUserClick(user: UserModel) // Click cả item
        fun onDeleteClick(user: UserModel) // Click nút xóa
    }

    inner class ViewHolder(val binding: ItemUserManagementBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserManagementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.binding.apply {
            userEmailTxt.text = user.email
            userNameTxt.text = user.name.ifEmpty { "(Chưa có tên)" } // Hiển thị tên
            userRoleTxt.text = "Role: ${user.role}"

             Glide.with(context).load(user.u)

            // Sửa listener
            holder.itemView.setOnClickListener {
                listener.onUserClick(user)
            }

            deleteUserBtn.setOnClickListener {
                listener.onDeleteClick(user)
            }
        }
    }

    fun updateData(newUsers: List<UserModel>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun removeUser(user: UserModel) {
        val position = users.indexOf(user)
        if (position > -1) {
            users.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}