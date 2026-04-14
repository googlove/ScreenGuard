package com.screenguard.protector.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.screenguard.protector.data.Alert
import com.screenguard.protector.data.WhitelistItem
import com.screenguard.protector.databinding.ItemAlertBinding
import com.screenguard.protector.databinding.ItemWhitelistBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Alert History Adapter with DiffUtil for better performance
class AlertHistoryAdapter(
    private val onDeleteClick: (Alert) -> Unit = {}
) : ListAdapter<Alert, AlertHistoryAdapter.AlertViewHolder>(AlertDiffCallback()) {

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(alert: Alert) {
            binding.apply {
                alertDescription.text = alert.description
                alertTime.text = formatTime(alert.timestamp)
                alertIcon.text = if (alert.isUnknown) "⚠️" else "✓"
                
                // Long press to delete
                root.setOnLongClickListener {
                    onDeleteClick(alert)
                    true
                }
            }
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
        holder.bind(getItem(position))
    }

    private fun formatTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("uk"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Помилка формату"
        }
    }

    class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem
        }
    }
}

// Whitelist Adapter with DiffUtil
class WhitelistAdapter(
    private val onDelete: (WhitelistItem) -> Unit = {}
) : ListAdapter<WhitelistItem, WhitelistAdapter.WhitelistViewHolder>(WhitelistDiffCallback()) {

    inner class WhitelistViewHolder(private val binding: ItemWhitelistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WhitelistItem) {
            binding.apply {
                itemName.text = item.name
                itemDate.text = formatDate(item.addedDate)
                
                deleteButton.setOnClickListener {
                    onDelete(item)
                }
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
        holder.bind(getItem(position))
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("uk"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Помилка формату"
        }
    }

    class WhitelistDiffCallback : DiffUtil.ItemCallback<WhitelistItem>() {
        override fun areItemsTheSame(oldItem: WhitelistItem, newItem: WhitelistItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WhitelistItem, newItem: WhitelistItem): Boolean {
            return oldItem == newItem
        }
    }
}

// Captures Grid Adapter
class CapturesAdapter(
    private val onItemClick: (java.io.File) -> Unit = {}
) : ListAdapter<java.io.File, CapturesAdapter.CaptureViewHolder>(CaptureDiffCallback()) {

    inner class CaptureViewHolder(private val binding: com.screenguard.protector.databinding.ItemCaptureBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: java.io.File) {
            binding.apply {
                captureImage.setImageURI(android.net.Uri.fromFile(file))
                captureFileName.text = file.name
                captureDatetime.text = formatDate(file.lastModified())
                
                // Long click → опції управління
                root.setOnLongClickListener {
                    onItemClick(file)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptureViewHolder {
        return CaptureViewHolder(
            com.screenguard.protector.databinding.ItemCaptureBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CaptureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("uk"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Помилка формату"
        }
    }

    class CaptureDiffCallback : DiffUtil.ItemCallback<java.io.File>() {
        override fun areItemsTheSame(oldItem: java.io.File, newItem: java.io.File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

        override fun areContentsTheSame(oldItem: java.io.File, newItem: java.io.File): Boolean {
            return oldItem.lastModified() == newItem.lastModified()
        }
    }
}
