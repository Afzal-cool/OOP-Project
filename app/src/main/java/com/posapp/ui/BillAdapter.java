package com.posapp.ui;

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.posapp.data.BillItem
import com.posapp.data.databinding.ItemBillBinding
class BillAdapter(
        private var items: MutableList<BillItem>,
        private val onRemoveClicked: (BillItem, Int) -> Unit,
private val onAddClicked: (BillItem, Int) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>(){

inner class BillViewHolder(private val binding: ItemBillBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(item: BillItem, position: Int) {

        binding.tvBillItemName.text = item.name
        binding.tvBillItemQuantity.text = "Qty: ${item.quantity}"
        binding.tvBillItemPrice.text = "Rs.${item.price * item.quantity}"

        binding.btnRemove.setOnClickListener {
            onRemoveClicked(item, position)
        }

        binding.btnAdd.setOnClickListener {
            onAddClicked(item, position)
        }
    }
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
    val binding = ItemBillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
    )
    return BillViewHolder(binding)
}

override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
    holder.bind(items[position], position)
}

override fun getItemCount(): Int = items.size

fun updateList(newList: MutableList<BillItem>) {
    items = newList
    notifyDataSetChanged()
}
}
