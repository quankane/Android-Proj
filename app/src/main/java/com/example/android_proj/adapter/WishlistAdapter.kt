// FILE: adapter/WishlistAdapter.kt (NEW)

package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.android_proj.activity.DetailActivity
import com.example.android_proj.databinding.ViewholderWishlistBinding
import com.example.android_proj.helper.ManagementWishList
import com.example.android_proj.model.ItemsModel

class WishlistAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    private val context: Context,
    private val onWishlistChanged: () -> Unit // Callback khi xóa/thêm
) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>()
{
    private val managementWishList = ManagementWishList(context)

    class ViewHolder(val binding: ViewholderWishlistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderWishlistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = listItemSelected[position]
        holder.binding.productTitle.text = item.title
        holder.binding.productPrice.text = "$${item.price}"
        holder.binding.productOldPrice.text = "$${item.oldPrice}"
        holder.binding.productDescription.text = "${item.description}"
        holder.binding.productRating.text = "${item.rating}"

        // Load ảnh sản phẩm
        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.productImage)

        // Xử lý nút Xóa khỏi WishList
        holder.binding.removeBtn.setOnClickListener {
            // Sử dụng logic toggle để xóa item khỏi danh sách
            managementWishList.toggleWishlistItem(item)

            // Xóa item khỏi danh sách hiển thị và thông báo cập nhật
            listItemSelected.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, listItemSelected.size)

            // Gọi callback để cập nhật UI
            onWishlistChanged()
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(holder.itemView.context,
                DetailActivity::class.java)
            intent.putExtra("object", item)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listItemSelected.size
    }
}