package com.example.android_proj.helper

import android.content.Context
import android.widget.Toast
import com.example.android_proj.model.ItemsModel
import kotlin.math.log


class ManagementCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertFood(item: ItemsModel) {
        var listFood = getListCart()
        val existAlready = listFood.any { it.title == item.title }
        val index = listFood.indexOfFirst { it.title == item.title }

        for (food in listFood) {
            println(food.toString());
        }

        println("==================")
        println("item title = " + item.title)

        if (existAlready) {
            println("1")
            listFood[index].numberInCart = item.numberInCart
        } else {
            println("2")
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
}