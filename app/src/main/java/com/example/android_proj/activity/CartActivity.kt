package com.example.android_proj.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.R // Import R
import com.example.android_proj.adapter.CartAdapter
import com.example.android_proj.databinding.ActivityCartBinding
import com.example.android_proj.helper.ChangeNumberItemsListener
import com.example.android_proj.helper.ManagementCart
import com.example.android_proj.model.Order
import com.example.android_proj.model.OrderItem
import com.example.android_proj.model.ShippingAddress
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth // THÊM IMPORT NÀY
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagementCart

    // Thêm các biến để lưu trữ giá trị
    private var tax: Double = 0.0
    private var delivery: Double = 0.0
    private var total: Double = 0.0
    private var itemTotal: Double = 0.0

    // Khai báo biến Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart = ManagementCart(this)

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initView()
    }

    override fun onResume() {
        super.onResume()
        // Tải lại dữ liệu giỏ hàng mỗi khi quay lại trang
        initCartItemList()
        calculateCart()
    }

    private fun initView() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Thêm listener cho nút Check Out
        binding.button.setOnClickListener {
            if (managementCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showCheckoutDialog()
        }
    }

    private fun initCartItemList() {
        binding.apply {
            viewCart.layoutManager = LinearLayoutManager(
                this@CartActivity,
                LinearLayoutManager.VERTICAL,
                false
            )

            viewCart.adapter = CartAdapter(
                managementCart.getListCart(),
                this@CartActivity,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        calculateCart()
                    }

                }
            )

            emptyTxt.visibility = if (managementCart.getListCart().isEmpty())
                View.VISIBLE else View.GONE

            scrollView3.visibility = if (managementCart.getListCart().isEmpty())
                View.GONE else View.VISIBLE
        }
    }

    private fun calculateCart() {
        val percentTax = 0.2
        delivery = 10.0 // Gán vào biến của class
        tax = Math.round((managementCart.getTotalFee() * percentTax) * 100) / 100.0
        total = Math.round((managementCart.getTotalFee() + tax + delivery) * 100) / 100.0
        itemTotal = Math.round(managementCart.getTotalFee() * 100) / 100.0 // Sửa lại

        with(binding) {
            totalFeeTxt.text = "$$itemTotal"
            taxTxt.text = "$$tax"
            deliveryTxt.text = "$$delivery"
            totalTxt.text = "$$total"
        }
    }

    // --- HÀM HIỆN DIALOG ---
    private fun showCheckoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkout, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Không cho hủy bằng cách bấm ra ngoài

        val dialog = builder.create()

        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (phone.isEmpty()) {
                etPhone.error = "Vui lòng nhập số điện thoại"
                return@setOnClickListener
            }

            if (address.isEmpty()) {
                etAddress.error = "Vui lòng nhập địa chỉ"
                return@setOnClickListener
            }

            // 1. Kiểm tra người dùng đã đăng nhập chưa
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Đóng dialog
                // Tùy chọn: Chuyển người dùng đến màn hình đăng nhập
                // startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }
            val userId = currentUser.uid

            // 2. Tạo đối tượng ShippingAddress
            val shippingAddress = ShippingAddress(
                fullName = "", // Bạn có thể thêm trường này vào dialog
                phoneNumber = phone,
                streetAddress = address,
                city = "" // Bạn có thể thêm trường này vào dialog
            )

            // 3. Tạo danh sách OrderItem từ giỏ hàng
            val orderItems = managementCart.getListCart().map { item ->
                OrderItem(
                    id = item.id.toString(), // Đảm bảo ID là String
                    title = item.title,
                    picUrl = item.picUrl.firstOrNull() ?: "", // Lấy ảnh đầu tiên
                    priceAtPurchase = item.price,
                    quantity = item.numberInCart
                )
            }

            // 4. Tạo đối tượng Order
            val order = Order(
                // orderId sẽ được tạo tự động bởi Firebase (nếu dùng)
                userId = userId, // Dùng userId thực tế
                orderDate = Timestamp.now(),
                status = "Pending", // Trạng thái chờ xử lý
                items = orderItems,
                shippingAddress = shippingAddress,
                paymentMethod = "Cash on Delivery",
                subtotal = itemTotal,
                tax = tax,
                deliveryFee = delivery,
                totalAmount = total
            )

            // 5. Lưu đơn hàng (ví dụ: lên Firebase)
            saveOrderToFirebase(order)

            dialog.dismiss()
        }

        dialog.show()
    }

    // --- HÀM LƯU ĐƠN HÀNG (ĐÃ HOÀN CHỈNH) ---
    private fun saveOrderToFirebase(order: Order) {

        // Tên collection "orders" phải khớp với tên bạn dùng trong MyOrdersActivity
        db.collection("orders")
            .add(order) // .add() tự động tạo Document ID
            .addOnSuccessListener { documentReference ->
                Log.d("CartActivity", "Order saved successfully with ID: ${documentReference.id}")
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()

                // Xóa giỏ hàng sau khi đặt thành công
                managementCart.clearCart() // Hàm này bạn đã thêm ở bước trước

                // Cập nhật lại UI (hiển thị giỏ hàng trống)
                initCartItemList()
                calculateCart()

                // Đóng CartActivity và quay lại
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("CartActivity", "Error saving order", e)
                Toast.makeText(this, "Đặt hàng thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}