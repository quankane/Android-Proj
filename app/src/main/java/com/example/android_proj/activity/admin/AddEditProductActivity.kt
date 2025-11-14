package com.example.android_proj.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.android_proj.R
import com.example.android_proj.adapter.admin.ImagePreviewAdapter
import com.example.android_proj.databinding.ActivityAddEditProductBinding
import com.example.android_proj.model.ItemsModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Cloudinary (Từ file của bạn)
    private val CLOUDINARY_UPLOAD_PRESET = "upload-1"

    // Image Adapter
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    // Trạng thái Add/Edit
    private var mCurrentProductId: String? = null
    private var mProductToEdit: ItemsModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Lấy ID sản phẩm (nếu là "edit")
        mCurrentProductId = intent.getStringExtra("PRODUCT_ID")

        initToolbar()
        initImagePicker()
        initRecyclerView()
        initListeners()

        // Kiểm tra chế độ Add hay Edit
        if (mCurrentProductId != null) {
            binding.toolbar.title = "Chỉnh sửa Sản phẩm"
            binding.btnSave.text = "Lưu thay đổi"
            loadProductDetails()
        } else {
            binding.toolbar.title = "Thêm Sản phẩm"
            binding.btnSave.text = "Lưu sản phẩm"
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initImagePicker() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data?.clipData != null) {
                    // Người dùng chọn nhiều ảnh
                    val count = result.data!!.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = result.data!!.clipData!!.getItemAt(i).uri
                        imagePreviewAdapter.addImage(imageUri)
                    }
                } else if (result.data?.data != null) {
                    // Người dùng chọn một ảnh
                    val imageUri = result.data?.data
                    if (imageUri != null) {
                        imagePreviewAdapter.addImage(imageUri)
                    }
                }
            }
        }
    }

    private fun initRecyclerView() {
        // Khởi tạo adapter với callback xử lý khi nhấn nút xóa
        imagePreviewAdapter = ImagePreviewAdapter(this) { sourceToRemove ->
            // sourceToRemove có thể là Uri (ảnh mới) hoặc String (URL cũ)
            imagePreviewAdapter.removeImage(sourceToRemove)
        }

        binding.imagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddEditProductActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = imagePreviewAdapter
        }
    }

    private fun initListeners() = with(binding) {
        // Nút thêm ảnh
        btnAddImage.setOnClickListener { openImagePicker() }

        // Nút thêm Size
        btnAddSize.setOnClickListener {
            showAddChipDialog("Thêm Size", "Nhập size (ví dụ: M)") { sizeText ->
                createChip(sizeText, sizeChipGroup)
            }
        }

        // Nút thêm Màu
        btnAddColor.setOnClickListener {
            showAddChipDialog("Thêm Màu", "Nhập tên màu (ví dụ: Đỏ)") { colorText ->
                createChip(colorText, colorChipGroup)
            }
        }

        // Nút Lưu
        btnSave.setOnClickListener {
            validateAndSaveProduct()
        }
    }

    /**
     * Tải chi tiết sản phẩm từ Firestore khi ở chế độ Edit
     */
    private fun loadProductDetails() {
        showLoading(true)
        db.collection("items").document(mCurrentProductId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    mProductToEdit = document.toObject(ItemsModel::class.java)
                    if (mProductToEdit == null) {
                        Toast.makeText(this, "Lỗi đọc dữ liệu sản phẩm.", Toast.LENGTH_SHORT).show()
                        finish()
                        return@addOnSuccessListener
                    }
                    // Điền dữ liệu vào UI
                    binding.apply {
                        etTitle.setText(mProductToEdit?.title)
                        etDescription.setText(mProductToEdit?.description)
                        etPrice.setText(mProductToEdit?.price.toString())
                        etOldPrice.setText(mProductToEdit?.oldPrice.toString())
                        etRating.setText(mProductToEdit?.rating.toString())

                        // Tải danh sách ảnh (URL String)
                        imagePreviewAdapter.setImages(mProductToEdit?.picUrl ?: emptyList())

                        // Tạo Chips cho Size
                        sizeChipGroup.removeAllViews()
                        mProductToEdit?.size?.forEach { createChip(it, sizeChipGroup) }

                        // Tạo Chips cho Màu
                        colorChipGroup.removeAllViews()
                        mProductToEdit?.color?.forEach { createChip(it, colorChipGroup) }
                    }
                    showLoading(false)
                } else {
                    Toast.makeText(this, "Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Log.e("AddEditProduct", "Lỗi tải sản phẩm: ${it.message}")
                Toast.makeText(this, "Lỗi tải chi tiết sản phẩm.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Mở trình chọn ảnh
     */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Cho phép chọn nhiều ảnh
        pickImageLauncher.launch(intent)
    }

    /**
     * Hiển thị Dialog đơn giản để nhập text cho Chip
     */
    private fun showAddChipDialog(title: String, hint: String, onOkClicked: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val input = EditText(this)
        input.hint = hint
        builder.setView(input)

        builder.setPositiveButton("Thêm") { dialog, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                onOkClicked(text)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    /**
     * Tạo và thêm một Chip vào ChipGroup
     */
    private fun createChip(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }

    /**
     * Lấy danh sách text từ các Chip trong một ChipGroup
     */
    private fun getChipsFromGroup(chipGroup: ChipGroup): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            list.add(chip.text.toString())
        }
        return list
    }

    /**
     * Kiểm tra dữ liệu và bắt đầu quá trình lưu
     */
    private fun validateAndSaveProduct() {
        val title = binding.etTitle.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        val oldPrice = binding.etOldPrice.text.toString().toDoubleOrNull() ?: 0.0
        val rating = binding.etRating.text.toString().toDoubleOrNull() ?: 0.0

        if (title.isEmpty() || price <= 0) {
            Toast.makeText(this, "Tên sản phẩm và Giá là bắt buộc.", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy danh sách ảnh từ adapter
        val newImageUris = imagePreviewAdapter.getUris()
        val existingImageUrls = imagePreviewAdapter.getUrls()

        if (newImageUris.isEmpty() && existingImageUrls.isEmpty()) {
            Toast.makeText(this, "Sản phẩm phải có ít nhất một ảnh.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Lấy danh sách Size và Color từ ChipGroups
        val sizes = getChipsFromGroup(binding.sizeChipGroup)
        val colors = getChipsFromGroup(binding.colorChipGroup)

        // Tạo đối tượng ItemsModel
        val docId = mCurrentProductId ?: db.collection("items").document().id
        val item = ItemsModel(
            id = docId,
            title = title,
            description = desc,
            price = price,
            oldPrice = oldPrice,
            rating = rating,
            size = sizes,
            color = colors,
            picUrl = ArrayList() // Sẽ được cập nhật sau khi upload
        )

        // Bắt đầu quá trình upload ảnh và sau đó lưu
        uploadImagesAndSave(newImageUris, existingImageUrls, item)
    }

    /**
     * Xử lý luồng upload:
     * 1. Upload tất cả ảnh mới (Uris) lên Cloudinary.
     * 2. Thu thập các URL mới.
     * 3. Kết hợp URL mới + URL cũ (Strings).
     * 4. Lưu vào Firestore.
     */
    private fun uploadImagesAndSave(
        newImageUris: List<Uri>,
        existingImageUrls: List<String>,
        itemData: ItemsModel
    ) {
        val finalImageUrls = ArrayList(existingImageUrls) // Bắt đầu với các URL cũ
        val totalNewUploads = newImageUris.size

        if (totalNewUploads == 0) {
            // Không có ảnh mới, chỉ cần lưu với các URL cũ
            itemData.picUrl = finalImageUrls
            saveProductToFirestore(itemData)
            return
        }

        val uploadedUrls = mutableListOf<String>()
        // AtomicInteger để đếm số lượng upload hoàn thành (thread-safe)
        val uploadCounter = AtomicInteger(0)
        var hasUploadFailed = false

        newImageUris.forEach { uri ->
            uploadImageToCloudinarySDK(uri,
                onSuccess = { newUrl ->
                    if (hasUploadFailed) return@uploadImageToCloudinarySDK

                    uploadedUrls.add(newUrl)
                    // Kiểm tra xem đây có phải là ảnh cuối cùng được upload không
                    if (uploadCounter.incrementAndGet() == totalNewUploads) {
                        // Đã upload xong tất cả, tiến hành lưu
                        finalImageUrls.addAll(uploadedUrls)
                        itemData.picUrl = finalImageUrls
                        saveProductToFirestore(itemData)
                    }
                },
                onFailure = {
                    if (hasUploadFailed) return@uploadImageToCloudinarySDK

                    // Nếu một ảnh thất bại, hủy toàn bộ
                    hasUploadFailed = true
                    showLoading(false)
                    Toast.makeText(this, "Một ảnh tải lên thất bại, đã hủy lưu.", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    /**
     * Tải MỘT ảnh lên Cloudinary bằng SDK
     * (Đây là hàm từ file mẫu của bạn, được điều chỉnh)
     */
    private fun uploadImageToCloudinarySDK(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure()
            return
        }

        val publicId = "items/${user.uid}/${UUID.randomUUID()}"
        // Biến đổi ảnh: fill, rộng 300, cao 300, tự động định dạng và chất lượng
        val transformations = "c_fill,w_300,h_300,f_auto,q_auto"

        MediaManager.get().upload(imageUri)
            .unsigned(CLOUDINARY_UPLOAD_PRESET)
            .option("public_id", publicId)
            .option("overwrite", false)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Đã gọi showLoading(true) từ trước
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        // Áp dụng biến đổi vào URL
                        val transformedUrl = secureUrl.replace("/upload/", "/upload/$transformations/")
                        Log.d("CloudinarySDK", "Upload thành công: $transformedUrl")
                        onSuccess(transformedUrl) // Trả về URL đã biến đổi
                    } else {
                        Log.e("CloudinarySDK", "Lỗi phân tích URL: $resultData")
                        onFailure()
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("CloudinarySDK", "Upload thất bại: ${error.description}", error.exception)
                    onFailure()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    /**
     * Lưu đối tượng ItemsModel hoàn chỉnh vào Firestore
     */
    private fun saveProductToFirestore(item: ItemsModel) {
        // Sử dụng set(item) sẽ hoạt động cho cả "Add" (tạo mới) và "Edit" (ghi đè)
        db.collection("items").document(item.id)
            .set(item)
            .addOnSuccessListener {
                showLoading(false)
                val message = if (mCurrentProductId == null) "Thêm sản phẩm thành công!" else "Cập nhật thành công!"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Lỗi lưu vào Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }
}