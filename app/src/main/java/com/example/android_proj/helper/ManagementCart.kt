package com.example.android_proj.helper

import android.content.Context
import android.widget.Toast
import com.example.android_proj.model.ItemsModel


class ManagementCart(val context: Context) {

    private val tinyDB = TinyDB(context)
    private val managementWishList = ManagementWishList(context)

    //Wishlist
    fun getWishlistItems(): ArrayList<ItemsModel> {
        return managementWishList.getListWishlist()
    }

    fun toggleWishlistItem(item: ItemsModel): Boolean {
        return managementWishList.toggleWishlistItem(item)
    }

    fun insertFood(item: ItemsModel) {
        var listFood = getListCart()

        // --- THAY ĐỔI LOGIC TỪ TITLE SANG ID ---
        // Giả định ItemsModel có thuộc tính 'id' duy nhất cho mỗi sản phẩm
        val existAlready = listFood.any { it.id == item.id && // THAY ĐỔI
                it.selectedSize == item.selectedSize &&
                it.selectedColor == item.selectedColor}
        val index = listFood.indexOfFirst { it.id == item.id && // THAY ĐỔI
                it.selectedSize == item.selectedSize &&
                it.selectedColor == item.selectedColor}
        // --- HẾT THAY ĐỔI ---

        if (listFood != null && !listFood.isEmpty()) {
            for (food in listFood) {
                println(food.toString());
            }
        }

        println("==================")
        println("item id = " + item.id) // THAY ĐỔI (để log id cho rõ)

        if (existAlready) {
            println("1")
            // Cập nhật số lượng cho mặt hàng đã tồn tại
            listFood[index].numberInCart = item.numberInCart
        } else {
            println("2")
            // Thêm mặt hàng mới (với size/color khác)
            listFood.add(item)
        }
        tinyDB.putListObject("CartList", listFood)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemsModel> {
        return tinyDB.getListObject("CartList") ?: arrayListOf()
    }

    fun minusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (listFood[position].numberInCart == 1) {
            listFood.removeAt(position)
        } else {
            listFood[position].numberInCart--
        }
        tinyDB.putListObject("CartList", listFood)
        listener.onChanged()
    }

    fun plusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        listFood[position].numberInCart++
        tinyDB.putListObject("CartList", listFood)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listFood = getListCart()
        var fee = 0.0
        for (item in listFood) {
            fee += item.price * item.numberInCart
        }
        return fee
    }

    // --- HÀM MỚI ---
    /**
     * Xóa tất cả các mặt hàng khỏi giỏ hàng trong TinyDB.
     * Thường được gọi sau khi thanh toán thành công.
     */
    fun clearCart() {
        tinyDB.remove("CartList")
        // Bạn không cần trả về list rỗng,
        // vì getListCart() sẽ tự động trả về list rỗng vào lần tới.
    }
}