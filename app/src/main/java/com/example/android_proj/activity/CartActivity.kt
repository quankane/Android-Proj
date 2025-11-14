package com.example.android_proj.activity

import android.content.Intent
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
import com.example.android_proj.R
import com.example.android_proj.adapter.CartAdapter
import com.example.android_proj.databinding.ActivityCartBinding
import com.example.android_proj.helper.ManagementCart
import com.example.android_proj.model.CartItem // SỬ DỤNG MODEL MỚI
import com.example.android_proj.model.Order
import com.example.android_proj.model.OrderItem
import com.example.android_proj.model.ShippingAddress
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.round

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagementCart
    private lateinit var cartAdapter: CartAdapter
    private var cartListener: ListenerRegistration? = null

    private var tax: Double = 0.0
    private var delivery: Double = 0.0
    private var total: Double = 0.0
    private var itemTotal: Double = 0.0

    private var currentCartList: List<CartItem> = emptyList() // Biến lưu trữ giỏ hàng hiện tại

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart = ManagementCart(this)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Kiểm tra đăng nhập
        if (auth.currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initView()
        initCartAdapter()
    }

    override fun onResume() {
        super.onResume()
        // Bắt đầu lắng nghe thay đổi giỏ hàng từ Firestore
        setupCartListener()
    }

    override fun onPause() {
        super.onPause()
        // Dừng lắng nghe khi Activity bị tạm dừng
        cartListener?.remove()
    }

    private fun initView() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.button.setOnClickListener {
            if (currentCartList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showCheckoutDialog()
        }
    }

    /**
     * Khởi tạo Adapter. Adapter sẽ trống lúc đầu.
     */
    private fun initCartAdapter() {
        cartAdapter = CartAdapter(
            mutableListOf(), // Bắt đầu với danh sách trống
            this,
            // Xử lý sự kiện click + và -
            object : CartAdapter.CartItemListener {
                override fun onPlusClicked(item: CartItem) {
                    managementCart.plusItem(item)
                }

                override fun onMinusClicked(item: CartItem) {
                    managementCart.minusItem(item)
                }
            }
        )

        binding.viewCart.layoutManager = LinearLayoutManager(this)
        binding.viewCart.adapter = cartAdapter
    }

    /**
     * Thiết lập trình lắng nghe Firestore.
     * Đây là hàm cốt lõi mới thay thế initCartItemList()
     */
    private fun setupCartListener() {
        binding.progressBar.visibility = View.VISIBLE // Thêm ProgressBar vào XML

        cartListener = managementCart.getCartItemsListener { cartList, totalFee ->
            binding.progressBar.visibility = View.GONE

            // Cập nhật danh sách hiện tại
            currentCartList = cartList

            // Cập nhật Adapter
            cartAdapter.updateList(cartList)

            // Tính toán tổng tiền
            calculateCart(totalFee)

            // Xử lý hiển thị View trống
            binding.emptyTxt.visibility = if (cartList.isEmpty()) View.VISIBLE else View.GONE
            binding.scrollView3.visibility = if (cartList.isEmpty()) View.GONE else View.VISIBLE
        }
    }


    /**
     * Tính toán tổng tiền dựa trên dữ liệu từ listener.
     */
    private fun calculateCart(totalFee: Double) {
        val percentTax = 0.2
        delivery = 10.0

        itemTotal = round(totalFee * 100) / 100.0
        tax = round(itemTotal * percentTax * 100) / 100.0
        total = round((itemTotal + tax + delivery) * 100) / 100.0

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
            .setCancelable(false)

        val dialog = builder.create()

        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<View>(R.id.btnConfirm)

        // (Tùy chọn) Tải SĐT/Địa chỉ đã lưu
        loadDefaultAddress(etPhone, etAddress)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            if (phone.isEmpty()) {
                etPhone.error = "Vui lòng nhập số điện thoại"
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                etAddress.error = "Vui lòng nhập địa chỉ"
                return@setOnClickListener
            }

            // 2. Tạo đối tượng ShippingAddress
            val shippingAddress = ShippingAddress(
                fullName = auth.currentUser?.displayName ?: "",
                phoneNumber = phone,
                streetAddress = address,
                city = "" // Có thể thêm trường thành phố
            )

            // 3. Tạo danh sách OrderItem từ giỏ hàng hiện tại (currentCartList)
            val orderItems = currentCartList.map { item ->
                OrderItem(
                    id = item.itemId.toString(),
                    title = item.title,
                    picUrl = item.picUrl,
                    priceAtPurchase = item.price,
                    quantity = item.numberInCart,
                    selectedSize = item.selectedSize,
                    selectedColor = item.selectedColor
                )
            }

            // 4. Tạo đối tượng Order
            val order = Order(
                userId = userId,
                orderDate = Timestamp.now(),
                status = "Pending",
                items = orderItems,
                shippingAddress = shippingAddress,
                paymentMethod = "Cash on Delivery",
                subtotal = itemTotal,
                tax = tax,
                deliveryFee = delivery,
                totalAmount = total
            )

            // 5. Lưu đơn hàng
            saveOrderToFirebase(order)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadDefaultAddress(etPhone: EditText, etAddress: EditText) {
        getUserId()?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    etPhone.setText(doc.getString("phoneNumber"))
                    etAddress.setText(doc.getString("streetAddress"))
                }
        }
    }

    // --- HÀM LƯU ĐƠN HÀNG ---
    private fun saveOrderToFirebase(order: Order) {
        db.collection("orders")
            .add(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                // Xóa giỏ hàng sau khi đặt thành công
                managementCart.clearCart()
                // Listener sẽ tự động cập nhật UI (không cần gọi initCartItemList)

                // Đóng CartActivity và quay lại
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("CartActivity", "Error saving order", e)
                Toast.makeText(this, "Đặt hàng thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }
}