package com.example.android_proj.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop // THÊM IMPORT
import com.example.android_proj.R // THÊM IMPORT (nếu thiếu)
import com.example.android_proj.databinding.ItemUserManagementBinding
import com.example.android_proj.model.UserModel

class UserManagementAdapter(
    private var users: MutableList<UserModel>,
    private val context: Context,
    private val listener: UserClickListener
) : RecyclerView.Adapter<UserManagementAdapter.ViewHolder>() {

    interface UserClickListener {
        fun onUserClick(user: UserModel)
        fun onDeleteClick(user: UserModel)
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
            userNameTxt.text = user.name.ifEmpty { "(Chưa có tên)" }
            userRoleTxt.text = "Role: ${user.role}"

            // --- SỬA LỖI VÀ CẬP NHẬT LOGIC LOAD AVATAR ---
            if (user.avatarUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(user.avatarUrl) // Sửa từ 'user.u'
                    .placeholder(R.drawable.ic_user_profile) // Ảnh chờ
                    .transform(CircleCrop()) // Bo tròn ảnh
                    .into(userAvatar) // Giả định ID trong layout là 'userAvatar'
            } else {
                // Nếu không có URL, dùng icon mặc định
                userAvatar.setImageResource(R.drawable.ic_user_profile)
            }
            // --- HẾT PHẦN SỬA ---

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