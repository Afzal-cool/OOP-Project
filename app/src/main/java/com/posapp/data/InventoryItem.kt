package com.posapp.data



/**
 * Data class representing an item in the inventory.
 */
data class InventoryItem(
    val id: Long = 0,
    val name: String,
    val price: Double,
    var stock: Int // Quantity currently in stock
)
