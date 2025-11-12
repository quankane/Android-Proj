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
    private val listener: UserClickListener
) : RecyclerView.Adapter<UserManagementAdapter.ViewHolder>() {

    interface UserClickListener {
        fun onEditRoleClick(user: UserModel)
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
            userRoleTxt.text = "Role: ${user.role}"

            // (Bạn có thể thêm logic load ảnh avatar nếu có URL)
            // Glide.with(context).load(user.avatarUrl)...

            editRoleBtn.setOnClickListener {
                listener.onEditRoleClick(user)
            }
        }
    }

    fun updateData(newUsers: List<UserModel>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}