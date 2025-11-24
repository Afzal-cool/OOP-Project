package com.posapp.data

data class InventoryItem(
    val name: String,
    val price: Double,
    var stock: Int, // Quantity currently in stock
    val id: Long

)
