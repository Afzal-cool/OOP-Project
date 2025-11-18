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
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>()
