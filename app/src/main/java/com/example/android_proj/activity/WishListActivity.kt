// FILE: activity/WishlistActivity.kt (NEW)

package com.example.android_proj.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.adapter.WishlistAdapter
import com.example.android_proj.databinding.ActivityWishListBinding
import com.example.android_proj.helper.ManagementWishList

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishListBinding
    private lateinit var managementWishList: ManagementWishList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementWishList = ManagementWishList(this)

    }

    override fun onResume() {
        super.onResume()
        initView()
        initWishlist()
    }

    private fun initView() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun initWishlist() {
        val wishlistItems = managementWishList.getListWishlist()

        binding.viewWishlist.apply {
            layoutManager = LinearLayoutManager(
                this@WishlistActivity,
                LinearLayoutManager.VERTICAL, // Danh sách dọc
                false
            )

            // Thiết lập Adapter và Callback để xử lý khi WishList thay đổi
            adapter = WishlistAdapter(
                wishlistItems,
                this@WishlistActivity,
                onWishlistChanged = {
                    checkEmptyState(wishlistItems.isEmpty())
                }
            )
        }

        checkEmptyState(wishlistItems.isEmpty())
    }

    // Hàm kiểm tra trạng thái trống/có dữ liệu
    private fun checkEmptyState(isEmpty: Boolean) {
        binding.emptyTxt.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.viewWishlist.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}