package com.posapp.ui

import android.app.AlertDialog
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.posapp.data.BillItem
import com.posapp.data.InventoryItem
import com.posapp.data.databinding.ActivityBillingBinding
import com.posapp.db.InventoryDatabaseHelper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BillingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillingBinding
    private lateinit var dbHelper: InventoryDatabaseHelper
    private lateinit var inventoryList: MutableList<InventoryItem>
    private var currentBill = mutableListOf<BillItem>()
    private lateinit var billAdapter: BillAdapter
    private var totalAmount = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = InventoryDatabaseHelper(this)

        setupInventorySpinner()
        setupRecyclerView()

        binding.btnAddItemToBill.setOnClickListener { addItemToBill() }


        binding.btnConfirmBill.setOnClickListener {
            if (currentBill.isNotEmpty()) {
                showConfirmationDialog()  // generate PDF only if confirmed
            } else {
                Toast.makeText(this, "Bill is empty", Toast.LENGTH_SHORT).show()
            }
        }



        updateTotalDisplay()
    }

    private fun setupInventorySpinner() {
        inventoryList = dbHelper.getAllItems().toMutableList()
        val names = inventoryList.map { "${it.name} (Stock: ${it.stock})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerInventory.adapter = adapter
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter(
            currentBill,
            onRemoveClicked = { item, position ->
                removeItemFromBill(item, position)
            },
            onAddClicked = { item, position ->
                addQuantity(item, position)
            }
        )

        binding.rvBillItems.layoutManager = LinearLayoutManager(this)
        binding.rvBillItems.adapter = billAdapter
    }

    private fun addItemToBill() {
        val pos = binding.spinnerInventory.selectedItemPosition
        if (pos < 0) return

        val item = inventoryList[pos]

        if (item.stock <= 0) {
            Toast.makeText(this, "OUT OF STOCK", Toast.LENGTH_SHORT).show()
            return
        }

        val index = currentBill.indexOfFirst { it.id == item.id }

        if (index >= 0) {
            currentBill[index].quantity++
        } else {
            currentBill.add(BillItem(item.id, item.name, item.price, 1))
        }

        reduceStock(item.id.toInt(), 1)
        billAdapter.updateList(currentBill)
        updateTotalDisplay()
    }

    private fun addQuantity(item: BillItem, position: Int) {
        val stockItem = inventoryList.find { it.id == item.id } ?: return
        if (stockItem.stock <= 0) {
            Toast.makeText(this, "NO MORE STOCK", Toast.LENGTH_SHORT).show()
            return
        }

        currentBill[position].quantity++
        reduceStock(item.id.toInt(), 1)

        billAdapter.updateList(currentBill)
        updateTotalDisplay()
    }

    private fun removeItemFromBill(item: BillItem, position: Int) {
        reduceStock(item.id.toInt(), -item.quantity)
        currentBill.removeAt(position)

        billAdapter.updateList(currentBill)
        updateTotalDisplay()
    }

    private fun reduceStock(id: Int, amount: Int) {
        val item = inventoryList.find { it.id.toInt() == id } ?: return
        item.stock -= amount

        dbHelper.updateStock(id, item.stock)
        setupInventorySpinner() // refresh spinner names
    }

    private fun updateTotalDisplay() {
        totalAmount = currentBill.sumOf { it.price * it.quantity }
        binding.tvTotalAmount.text = "Total: Rs.$totalAmount"
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Bill")
            .setMessage("Confirm sale of ${currentBill.size} items.\n Generate PDF receipt")
            .setPositiveButton("Confirm") { _, _ ->
                generatePdfBill()
                resetBill()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun resetBill() {
        currentBill.clear()
        billAdapter.updateList(currentBill)
        updateTotalDisplay()
        setupInventorySpinner()
        Toast.makeText(this, "Sale complete!", Toast.LENGTH_SHORT).show()
    }

    // ---------------- PDF GENERATION -------------------

    private fun generatePdfBill() {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val headerPaint = Paint()
        val linePaint = Paint()

        val purple = 0xFF6A0DAD.toInt() // deep purple
        val lightPurple = 0xFFEDE0F7.toInt() // very light purple for header background

        var y = 50f
        val x = 50f

        // --- HEADER BACKGROUND ---
        paint.color = lightPurple
        canvas.drawRect(x - 10, y - 30, 545f, y + 20, paint)

        // --- HEADER TEXT ---
        headerPaint.color = purple
        headerPaint.textSize = 28f
        headerPaint.isFakeBoldText = true
        canvas.drawText("E - RECEIPT", x, y, headerPaint)
        y += 40

        headerPaint.textSize = 16f
        headerPaint.isFakeBoldText = false
        canvas.drawText(
            "Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}",
            x,
            y,
            headerPaint
        )
        y += 30

        // --- LINE SEPARATOR ---
        linePaint.color = purple
        linePaint.strokeWidth = 2f
        canvas.drawLine(x, y, 545f, y, linePaint)
        y += 20

        // --- BILL ITEMS ---
        paint.textSize = 14f
        paint.color = 0xFF000000.toInt()
        for ((index, item) in currentBill.withIndex()) {
            // Alternate row shading for modern look
            if (index % 2 == 0) {
                paint.color = 0xFFF5E6FF.toInt() // very light purple row
                canvas.drawRect(x - 10, y - 14, 545f, y + 6, paint)
            }

            paint.color = 0xFF000000.toInt()
            canvas.drawText("${item.name}", x, y, paint)
            canvas.drawText("x${item.quantity}", 300f, y, paint)
            canvas.drawText("Rs.${item.price * item.quantity}", 450f, y, paint)
            y += 25
        }

        // --- LINE ABOVE TOTAL ---
        y += 10
        canvas.drawLine(x, y, 545f, y, linePaint)
        y += 30

        // --- TOTAL ---
        headerPaint.textSize = 28f
        headerPaint.isFakeBoldText = true
        headerPaint.color = purple
        canvas.drawText("TOTAL: Rs.$totalAmount", x, y, headerPaint)

        // --- FOOTER NOTE ---
        y += 40
        paint.textSize = 12f
        paint.color = 0xFF555555.toInt()
        canvas.drawText("Thank you for your purchase! This is an auto-generated receipt.", x, y, paint)

        // --- FINISH PAGE ---
        doc.finishPage(page)

        // --- SAVE PDF in Downloads ---
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val file = File(path, "Bill_${System.currentTimeMillis()}.pdf")
        try {
            doc.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF saved at $file", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            doc.close()
        }
    }

}
