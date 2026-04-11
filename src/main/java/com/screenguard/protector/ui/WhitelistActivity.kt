package com.screenguard.protector.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.screenguard.protector.adapter.WhitelistAdapter
import com.screenguard.protector.data.WhitelistItem
import com.screenguard.protector.data.WhitelistRepository
import com.screenguard.protector.databinding.ActivityWhitelistBinding
import com.screenguard.protector.utils.PreferencesManager
import kotlinx.coroutines.launch

class WhitelistActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWhitelistBinding
    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: WhitelistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhitelistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        whitelistRepository = WhitelistRepository(this)
        preferencesManager = PreferencesManager(this)
        
        setupUI()
        loadWhitelist()
    }

    private fun setupUI() {
        adapter = WhitelistAdapter(mutableListOf()) { item ->
            removeFromWhitelist(item)
        }
        
        binding.whitelistRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WhitelistActivity)
            adapter = this@WhitelistActivity.adapter
        }

        binding.addWhitelistButton.setOnClickListener {
            showAddWhitelistDialog()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadWhitelist() {
        lifecycleScope.launch {
            whitelistRepository.getAllItems().collect { items ->
                adapter.updateList(items.toMutableList())
                binding.emptyState.text = if (items.isEmpty()) {
                    "Немає доданих контактів"
                } else {
                    ""
                }
            }
        }
    }

    private fun showAddWhitelistDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Додати контакт")
            .setMessage("Введіть ім'я контакту")
            .setView(
                android.widget.EditText(this).apply {
                    hint = "Ім'я"
                }
            )
            .setPositiveButton("Додати") { dialog, _ ->
                val name = (dialog as androidx.appcompat.app.AlertDialog).findViewById<android.widget.EditText>(android.R.id.input)?.text.toString()
                if (name.isNotEmpty()) {
                    addToWhitelist(name)
                } else {
                    Toast.makeText(this, "Введіть ім'я", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Скасувати", null)
            .create()
        dialog.show()
    }

    private fun addToWhitelist(name: String) {
        lifecycleScope.launch {
            val item = WhitelistItem(
                id = 0,
                name = name,
                addedDate = System.currentTimeMillis(),
                isActive = true
            )
            whitelistRepository.insertItem(item)
            Toast.makeText(this@WhitelistActivity, "Контакт додано", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromWhitelist(item: WhitelistItem) {
        lifecycleScope.launch {
            whitelistRepository.deleteItem(item)
            Toast.makeText(this@WhitelistActivity, "Контакт видалено", Toast.LENGTH_SHORT).show()
        }
    }
}
