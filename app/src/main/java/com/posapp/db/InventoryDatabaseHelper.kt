package com.posapp.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.posapp.data.InventoryItem

class InventoryDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "POSInventoryDB"
        const val TABLE_INVENTORY = "inventory"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_PRICE = "price"
        const val KEY_STOCK = "stock" // Quantity in stock
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_INVENTORY_TABLE = ("CREATE TABLE $TABLE_INVENTORY("
                + "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$KEY_NAME TEXT,"
                + "$KEY_PRICE REAL,"
                + "$KEY_STOCK INTEGER" + ")")
        db.execSQL(CREATE_INVENTORY_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTORY")
        onCreate(db)
    }

    //insert new items
    fun addItem(item: InventoryItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, item.name)
            put(KEY_PRICE, item.price)
            put(KEY_STOCK, item.stock)
        }
        val id = db.insert(TABLE_INVENTORY, null, values)
        db.close()
        return id
    }

    //update the existing item
    fun updateItem(item: InventoryItem): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, item.name)
            put(KEY_PRICE, item.price)
            put(KEY_STOCK, item.stock)
        }
        // Updating row
        val rowsAffected = db.update(TABLE_INVENTORY, values, "$KEY_ID = ?", arrayOf(item.id.toString()))
        db.close()
        return rowsAffected
    }
    fun updateStock(id: Long, newStock: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("stock", newStock)
        }
        db.update("inventory", values, "id=?", arrayOf(id.toString()))
        db.close()
    }

    //delete an item
    fun deleteItem(itemId: Long): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(TABLE_INVENTORY, "$KEY_ID = ?", arrayOf(itemId.toString()))
        db.close()
        return rowsAffected
    }

    //retrieve item
    fun getAllItems(): List<InventoryItem> {
        val itemList = mutableListOf<InventoryItem>()
        val selectQuery = "SELECT * FROM $TABLE_INVENTORY ORDER BY $KEY_NAME ASC"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(it.getColumnIndexOrThrow(KEY_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(KEY_NAME))
                    val price = it.getDouble(it.getColumnIndexOrThrow(KEY_PRICE))
                    val stock = it.getInt(it.getColumnIndexOrThrow(KEY_STOCK))
                    itemList.add(InventoryItem(name, price, stock, id))
                } while (it.moveToNext())
            }
        }
        db.close()
        return itemList
    }
}