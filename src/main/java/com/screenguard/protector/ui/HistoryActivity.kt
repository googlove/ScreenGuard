package com.screenguard.protector.ui

import android.os.Bundle
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
        adapter = AlertHistoryAdapter(mutableListOf())
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.clearHistoryButton.setOnClickListener {
            clearHistory()
        }
    }

    private fun loadHistory() {
        lifecycleScope.launch {
            alertRepository.getAllAlerts().collect { alerts ->
                adapter.updateList(alerts.toMutableList())
                binding.emptyState.text = if (alerts.isEmpty()) {
                    "Немає записів"
                } else {
                    ""
                }
            }
        }
    }

    private fun clearHistory() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Видалити історію?")
            .setMessage("Ви впевнені?")
            .setPositiveButton("Видалити") { _, _ ->
                lifecycleScope.launch {
                    alertRepository.clearAll()
                }
            }
            .setNegativeButton("Скасувати", null)
            .create()
        dialog.show()
    }
}
