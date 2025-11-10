package com.example.android_proj.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_proj.adapter.CartAdapter
import com.example.android_proj.databinding.ActivityCartBinding
import com.example.android_proj.helper.ChangeNumberItemsListener
import com.example.android_proj.helper.ManagementCart

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagementCart
    private var tax: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managementCart = ManagementCart(this)

        initView()
        initCartItemList()
        calculateCart()
    }

    private fun initView() {
        binding.backBtn.setOnClickListener {
            finish()
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
        val delivery = 10.0
        tax = Math.round((managementCart.getTotalFee() * percentTax) * 100) / 100.0
        val total = Math.round((managementCart.getTotalFee() +  tax + delivery) * 100) / 100.0
        val itemTotal = Math.round(managementCart.getTotalFee() * 100) / 100

        with(binding) {
            totalFeeTxt.text = "$$itemTotal"
            taxTxt.text = "$$tax"
            deliveryTxt.text = "$$delivery"
            totalTxt.text = "$$total"
        }
    }
}