package com.example.android_proj.helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.android_proj.model.CartItem
import com.example.android_proj.model.ItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Lớp ManagementCart này đã được REFACTOR (viết lại)
 * để sử dụng Firebase Firestore thay vì TinyDB.
 * Mọi hoạt động giờ đây đều bất đồng bộ.
 */
class ManagementCart(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lấy userId hiện tại
    private fun getUserId(): String? {
        val user = auth.currentUser
        if (user == null) {
            Log.w("ManagementCart", "Người dùng chưa đăng nhập.")
            Toast.makeText(context, "Vui lòng đăng nhập để sử dụng giỏ hàng", Toast.LENGTH_SHORT).show()
        }
        return user?.uid
    }

    // Lấy collection giỏ hàng của người dùng
    private fun getCartCollection() = getUserId()?.let {
        db.collection("users").document(it).collection("cartItems")
    }

    /**
     * Thêm hoặc cập nhật sản phẩm trong giỏ hàng Firestore.
     */
    fun insertFood(item: ItemsModel) {
        val userId = getUserId() ?: return
        val cartCollection = getCartCollection() ?: return

        // 1. Tạo một CartItem từ ItemsModel
        val cartItem = CartItem(
            itemId = item.id,
            title = item.title,
            picUrl = item.picUrl.firstOrNull() ?: "",
            price = item.price,
            numberInCart = item.numberInCart,
            selectedSize = item.selectedSize ?: "",
            selectedColor = item.selectedColor ?: ""
        )

        // 2. Kiểm tra xem mục này đã tồn tại chưa (dựa trên itemId, size, color)
        cartCollection
            .whereEqualTo("itemId", cartItem.itemId)
            .whereEqualTo("selectedSize", cartItem.selectedSize)
            .whereEqualTo("selectedColor", cartItem.selectedColor)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // 3a. Nếu chưa có -> Thêm mới
                    cartCollection.add(cartItem)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ManagementCart", "Lỗi thêm vào giỏ hàng", e)
                        }
                } else {
                    // 3b. Nếu đã có -> Cập nhật số lượng
                    val docId = documents.first().id
                    val existingItem = documents.first().toObject(CartItem::class.java)
                    val newQuantity = existingItem.numberInCart + cartItem.numberInCart

                    cartCollection.document(docId).update("numberInCart", newQuantity)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ManagementCart", "Lỗi cập nhật giỏ hàng", e)
                        }
                }
            }
    }

    /**
     * Lấy danh sách giỏ hàng và LẮNG NGHE thay đổi (thay thế cho getListCart() đồng bộ)
     * Hàm này trả về một ListenerRegistration để Activity có thể hủy đăng ký khi onPause.
     */
    fun getCartItemsListener(onUpdate: (List<CartItem>, Double) -> Unit): ListenerRegistration? {
        val cartCollection = getCartCollection() ?: return null

        return cartCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ManagementCart", "Lỗi lắng nghe giỏ hàng", error)
                onUpdate(emptyList(), 0.0) // Trả về danh sách rỗng nếu lỗi
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val cartList = mutableListOf<CartItem>()
                var totalFee = 0.0

                for (doc in snapshot.documents) {
                    val item = doc.toObject(CartItem::class.java)
                    if (item != null) {
                        item.documentId = doc.id // Gán ID của Firestore document
                        cartList.add(item)
                        totalFee += item.price * item.numberInCart
                    }
                }
                onUpdate(cartList, totalFee) // Gửi danh sách và tổng tiền về Activity
            }
        }
    }

    /**
     * Giảm số lượng item trong Firestore.
     * Xóa nếu số lượng là 1.
     */
    fun minusItem(cartItem: CartItem) {
        val cartCollection = getCartCollection() ?: return

        if (cartItem.numberInCart == 1) {
            // Xóa document
            cartCollection.document(cartItem.documentId).delete()
                .addOnFailureListener { e -> Log.e("ManagementCart", "Lỗi xóa item", e) }
        } else {
            // Cập nhật (giảm) số lượng
            cartCollection.document(cartItem.documentId)
                .update("numberInCart", cartItem.numberInCart - 1)
                .addOnFailureListener { e -> Log.e("ManagementCart", "Lỗi cập nhật item", e) }
        }
    }

    /**
     * Tăng số lượng item trong Firestore.
     */
    fun plusItem(cartItem: CartItem) {
        val cartCollection = getCartCollection() ?: return

        cartCollection.document(cartItem.documentId)
            .update("numberInCart", cartItem.numberInCart + 1)
            .addOnFailureListener { e -> Log.e("ManagementCart", "Lỗi cập nhật item", e) }
    }

    /**
     * Xóa toàn bộ giỏ hàng trên Firestore.
     */
    fun clearCart() {
        val cartCollection = getCartCollection() ?: return

        cartCollection.get().addOnSuccessListener { documents ->
            val batch = db.batch()
            for (doc in documents) {
                batch.delete(doc.reference)
            }
            batch.commit()
                .addOnSuccessListener {
                    Log.d("ManagementCart", "Đã xóa giỏ hàng trên Firestore")
                }
                .addOnFailureListener { e -> Log.e("ManagementCart", "Lỗi xóa giỏ hàng", e) }
        }
    }
}