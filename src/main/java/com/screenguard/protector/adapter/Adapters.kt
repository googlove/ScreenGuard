package com.screenguard.protector.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.screenguard.protector.data.Alert
import com.screenguard.protector.data.WhitelistItem
import com.screenguard.protector.databinding.ItemAlertBinding
import com.screenguard.protector.databinding.ItemWhitelistBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertHistoryAdapter(private var items: MutableList<Alert>) : 
    RecyclerView.Adapter<AlertHistoryAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alert: Alert) {
            binding.alertDescription.text = alert.description
            binding.alertTime.text = formatTime(alert.timestamp)
            binding.alertIcon.text = if (alert.isUnknown) "⚠️" else "✓"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        return AlertViewHolder(
            ItemAlertBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: MutableList<Alert>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("uk"))
        return sdf.format(Date(timestamp))
    }
}

class WhitelistAdapter(
    private var items: MutableList<WhitelistItem>,
    private val onDelete: (WhitelistItem) -> Unit
) : RecyclerView.Adapter<WhitelistAdapter.WhitelistViewHolder>() {

    inner class WhitelistViewHolder(private val binding: ItemWhitelistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WhitelistItem) {
            binding.itemName.text = item.name
            binding.itemDate.text = formatDate(item.addedDate)
            binding.deleteButton.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhitelistViewHolder {
        return WhitelistViewHolder(
            ItemWhitelistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WhitelistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: MutableList<WhitelistItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("uk"))
        return sdf.format(Date(timestamp))
    }
}
