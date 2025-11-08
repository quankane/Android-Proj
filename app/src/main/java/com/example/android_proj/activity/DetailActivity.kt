package com.example.android_proj.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.android_proj.R
import com.example.android_proj.adapter.ColorAdapter
import com.example.android_proj.adapter.PicsAdapter
import com.example.android_proj.adapter.SizeAdapter
import com.example.android_proj.databinding.ActivityDetailBinding
import com.example.android_proj.helper.ManagementCart
import com.example.android_proj.model.ItemsModel

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managementCart : ManagementCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart = ManagementCart(this)
        item = intent.getSerializableExtra("object")!! as ItemsModel

        setupViews()
        setupPicsList()
        setupColorsList()
        setupSizeList()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() = with(binding) {
        titleTxt.text = item.title
        descriptionTxt.text = item.description
        priceTxt.text = "$${item.price}"
        oldPriceTxt.text = "$${item.oldPrice}"
        oldPriceTxt.paintFlags = priceTxt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        ratingTxt.text = "${item.rating} Rating"
        numberItemTxt.text = item.numberInCart.toString()

        updateTotalPrice()

        Glide.with(this@DetailActivity)
            .load(item.picUrl.firstOrNull())
            .into(picMain)

        backBtn.setOnClickListener { finish() }

        plusBtn.setOnClickListener {
            item.numberInCart++
            numberItemTxt.text = item.numberInCart.toString()
            updateTotalPrice()
        }

        minusBtn.setOnClickListener {
            if (item.numberInCart > 1) {
                item.numberInCart--
                numberItemTxt.text = item.numberInCart.toString()
                updateTotalPrice()
            }
        }

        addToCartBtn.setOnClickListener {
            managementCart.insertFood(item)
        }
    }

    private fun setupPicsList() {
        val picList = item.picUrl.toList()
        binding.picList.apply {
            adapter = PicsAdapter(picList as MutableList<String>) {
                imageUrl ->
                Glide.with(this@DetailActivity)
                    .load(imageUrl)
                    .into(binding.picMain)
            }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupColorsList() {
        binding.colorList.adapter = ColorAdapter(item.color)
        binding.colorList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun setupSizeList() {
        val sizeList = item.size.map { it }
        binding.sizeList.apply {
            adapter = SizeAdapter(sizeList as MutableList<String>)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTotalPrice() = with(binding) {
        totalPriceTxt.text = "$${item.price * item.numberInCart}"
    }
}