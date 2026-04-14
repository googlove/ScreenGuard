package com.screenguard.protector.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.screenguard.protector.adapter.AlertHistoryAdapter
import com.screenguard.protector.data.AlertRepository
import com.screenguard.protector.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var alertRepository: AlertRepository
    private lateinit var adapter: AlertHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alertRepository = AlertRepository(this)
        setupUI()
        loadHistory()
    }

    private fun setupUI() {
        adapter = AlertHistoryAdapter { alert ->
            showDeleteDialog(alert)
        }
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.clearHistoryButton.setOnClickListener {
            clearAllHistory()
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                alertRepository.getAllAlerts().collect { alerts ->
                    adapter.submitList(alerts)
                    binding.emptyState.text = if (alerts.isEmpty()) {
                        "Немає записів історії"
                    } else {
                        ""
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Помилка при завантаженні історії",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDeleteDialog(alert: com.screenguard.protector.data.Alert) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Видалити запис?")
            .setMessage("Цю дію неможливо скасувати")
            .setPositiveButton("Видалити") { _, _ ->
                deleteAlert(alert)
            }
            .setNegativeButton("Скасувати", null)
            .create()
        dialog.show()
    }

    private fun deleteAlert(alert: com.screenguard.protector.data.Alert) {
        lifecycleScope.launch {
            try {
                alertRepository.deleteAlert(alert)
                Toast.makeText(
                    this@HistoryActivity,
                    "Запис видалено",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@HistoryActivity,
                    "Помилка при видаленні",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearAllHistory() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Видалити всю історію?")
            .setMessage("Цю дію неможливо скасувати")
            .setPositiveButton("Видалити") { _, _ ->
                lifecycleScope.launch {
                    try {
                        alertRepository.clearAll()
                        Toast.makeText(
                            this@HistoryActivity,
                            "Історія очищена",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@HistoryActivity,
                            "Помилка при очищенні",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Скасувати", null)
            .create()
        dialog.show()
    }
}
