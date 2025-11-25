package com.posapp.data

data class BillItem(
    val id: Long,
    val name: String,
    val price: Double,
    var quantity: Int = 1
)

