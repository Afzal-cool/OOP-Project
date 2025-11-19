package com.posapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.posapp.data.InventoryItem
import com.posapp.data.databinding.ItemInventoryBinding

/**
 * Adapter for displaying the list of inventory items.
 */
class InventoryAdapter(
    private val items: List<InventoryItem>,
    private val onEditClicked: (InventoryItem) -> Unit,
    private val onDeleteClicked: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventoryItem, onEdit: (InventoryItem) -> Unit, onDelete: (InventoryItem) -> Unit) {
            binding.tvItemName.text = item.name
            binding.tvItemPrice.text = String.format("Rs.%.2f", item.price)
            binding.tvItemStock.text = "Stock: ${item.stock}"

            // Highlight low stock (optional styling)
            if (item.stock < 10) {
                binding.tvItemStock.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
            } else {
                binding.tvItemStock.setTextColor(binding.root.context.getColor(android.R.color.black))
            }

            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, onEditClicked, onDeleteClicked)
    }

    override fun getItemCount(): Int = items.size
}