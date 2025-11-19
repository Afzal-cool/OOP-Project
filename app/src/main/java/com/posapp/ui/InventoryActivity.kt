package com.posapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.posapp.data.InventoryItem
import com.posapp.data.R
import com.posapp.data.databinding.ActivityInventoryBinding
import com.posapp.data.databinding.DialogInventoryCrudBinding
import com.posapp.db.InventoryDatabaseHelper


/**
 * Handles the CRUD operations and real-time viewing of the inventory stock.
 */
class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var dbHelper: InventoryDatabaseHelper
    private lateinit var adapter: InventoryAdapter
    private var inventoryList = mutableListOf<InventoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Inventory Management"

        dbHelper = InventoryDatabaseHelper(this)

        setupRecyclerView()
        loadInventory()

        binding.fabAddItem.setOnClickListener {
            showCrudDialog(null) // Pass null for Add operation
        }
    }

    private fun setupRecyclerView() {
        adapter = InventoryAdapter(inventoryList, ::showCrudDialog, ::deleteItem)
        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter
    }

    private fun loadInventory() {
        inventoryList.clear()
        inventoryList.addAll(dbHelper.getAllItems())
        adapter.notifyDataSetChanged()
    }

    //toast dialog for editing update or add
    private fun showCrudDialog(item: InventoryItem?) {
        val dialogBinding = DialogInventoryCrudBinding.inflate(LayoutInflater.from(this))
        val isEdit = item != null

        val dialogTitle = if (isEdit) "Update Item: ${item?.name}" else "Add New Item"
        dialogBinding.btnSave.text = if (isEdit) "UPDATE" else "ADD"

        // Pre-fill fields if editing
        if (isEdit) {
            dialogBinding.etItemName.setText(item?.name)
            dialogBinding.etItemPrice.setText(item?.price.toString())
            dialogBinding.etItemStock.setText(item?.stock.toString())
        }

        // Create the AlertDialog instance
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
            .setTitle(dialogTitle)
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .create()


        // Set the custom click listener using setOnShowListener to access the dialog instance
        dialog.setOnShowListener {
            // Onclick listener for save and update
            dialogBinding.btnSave.setOnClickListener {
                val name = dialogBinding.etItemName.text.toString().trim()
                val priceText = dialogBinding.etItemPrice.text.toString().trim()
                val stockText = dialogBinding.etItemStock.text.toString().trim()

                if (name.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
                    Toast.makeText(this@InventoryActivity, "All fields are required.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val price = priceText.toDouble()
                    val stock = stockText.toInt()

                    val newItem = InventoryItem(
                        id = item?.id ?: 0,
                        name = name,
                        price = price,
                        stock = stock
                    )

                    if (isEdit) {
                        dbHelper.updateItem(newItem)
                        Toast.makeText(this@InventoryActivity, "Item updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        dbHelper.addItem(newItem)
                        Toast.makeText(this@InventoryActivity, "Item added successfully!", Toast.LENGTH_SHORT).show()
                    }

                    loadInventory() // Refresh the list
                    dialog.dismiss() // Explicitly dismiss the dialog
                } catch (e: NumberFormatException) {
                    Toast.makeText(this@InventoryActivity, "Invalid Price or Stock value.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show() //show dialog when clicked
    }

    //delete inventory items
    private fun deleteItem(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete '${item.name}'?")
            .setPositiveButton("DELETE") { _, _ ->
                dbHelper.deleteItem(item.id)
                loadInventory() // Refresh the list
                Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}