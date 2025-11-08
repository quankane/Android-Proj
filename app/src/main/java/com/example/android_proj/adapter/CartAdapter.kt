package com.example.android_proj.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.android_proj.databinding.ViewholderCartBinding
import com.example.android_proj.helper.ChangeNumberItemsListener
import com.example.android_proj.helper.ManagementCart
import com.example.android_proj.model.ItemsModel
import kotlin.math.roundToLong

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener? = null
) : RecyclerView.Adapter<CartAdapter.ViewHolder>()
{

    private val managementCart = ManagementCart(context)

    class ViewHolder(val binding: ViewholderCartBinding) :
    RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartAdapter.ViewHolder {
        val binding = ViewholderCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = listItemSelected[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.feeEachItemTxt.text = "$${item.price}"
        holder.binding.totalEachItem.text = "$${(item.numberInCart * item.price).roundToLong()}"
        holder.binding.numberItemTxt.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.pic)

        holder.binding.plusCartBtn.setOnClickListener {
            managementCart.plusItem(
                listItemSelected,
                position,
                object : ChangeNumberItemsListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumberItemsListener?.onChanged()
                    }

                }
            )
        }

        holder.binding.minusCartBtn.setOnClickListener {
            managementCart.minusItem(
                listItemSelected,
                position,
                object : ChangeNumberItemsListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChanged() {
                        notifyDataSetChanged()
                        changeNumberItemsListener?.onChanged()
                    }

                }
            )
        }
    }

    override fun getItemCount(): Int {
        return listItemSelected.size
    }
}