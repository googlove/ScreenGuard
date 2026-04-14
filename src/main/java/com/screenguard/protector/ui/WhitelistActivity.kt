package com.screenguard.protector.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.screenguard.protector.adapter.WhitelistAdapter
import com.screenguard.protector.data.WhitelistItem
import com.screenguard.protector.data.WhitelistRepository
import com.screenguard.protector.databinding.ActivityWhitelistBinding
import kotlinx.coroutines.launch

class WhitelistActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWhitelistBinding
    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var adapter: WhitelistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhitelistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        whitelistRepository = WhitelistRepository(this)
        
        setupUI()
        loadWhitelist()
    }

    private fun setupUI() {
        adapter = WhitelistAdapter { item ->
            removeFromWhitelist(item)
        }
        
        binding.whitelistRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WhitelistActivity)
            adapter = this@WhitelistAdapter
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
            try {
                whitelistRepository.getAllItems().collect { items ->
                    adapter.submitList(items)
                    binding.emptyState.text = if (items.isEmpty()) {
                        "Немає доданих контактів"
                    } else {
                        ""
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@WhitelistActivity,
                    "Помилка при завантаженні списку",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAddWhitelistDialog() {
        val editText = EditText(this).apply {
            hint = "Введіть ім'я контакту"
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Додати контакт у білий список")
            .setMessage("Назва контакту (до 50 символів)")
            .setView(editText)
            .setPositiveButton("Додати") { dialog, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (name.length > 50) {
                        Toast.makeText(
                            this,
                            "Ім'я занадто довге",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        addToWhitelist(name)
                    }
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
            try {
                val item = WhitelistItem(
                    id = 0,
                    name = name,
                    addedDate = System.currentTimeMillis(),
                    isActive = true
                )
                whitelistRepository.insertItem(item)
                Toast.makeText(
                    this@WhitelistActivity,
                    "Контакт додано",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@WhitelistActivity,
                    "Помилка при додаванні: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun removeFromWhitelist(item: WhitelistItem) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Видалити контакт?")
            .setMessage("${item.name} буде видалено з білого списку")
            .setPositiveButton("Видалити") { _, _ ->
                lifecycleScope.launch {
                    try {
                        whitelistRepository.deleteItem(item)
                        Toast.makeText(
                            this@WhitelistActivity,
                            "Контакт видалено",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@WhitelistActivity,
                            "Помилка при видаленні",
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
