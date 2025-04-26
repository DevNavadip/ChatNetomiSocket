package com.netomi.chat

/*class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompazt.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}*/

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netomi.chat.databinding.ActivityMainBinding
import com.netomi.chat.Adapter.ChatAdapter
import com.netomi.chat.Database.AppDatabase
import com.netomi.chat.Repository.ChatRepository
import com.netomi.chat.SocketIO.SocketService
import com.netomi.chat.ViewModel.ChatViewModel
import com.netomi.chat.ViewModel.ChatViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatAdapter: ChatAdapter
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(
            ChatRepository(
                AppDatabase.getDatabase(this).chatDao(),
                AppDatabase.getDatabase(this).messageDao(),
                SocketService()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start socket service
        startService(Intent(this, SocketService::class.java))

        setupRecyclerView()
        observeChats()
        setupClickListeners()
        checkNetworkConnection()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            onChatClicked = { chatId ->
                viewModel.markChatAsRead(chatId)
                showMessagesForChat(chatId)
            }
        )

        binding.chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }
    }

    private fun observeChats() {
        viewModel.allChats.observe(this) { chats ->
            if (chats.isEmpty()) {
                binding.emptyStateView.visibility = View.VISIBLE
                binding.chatsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateView.visibility = View.GONE
                binding.chatsRecyclerView.visibility = View.VISIBLE
                chatAdapter.submitList(chats)

                // Show the first chat by default
                chats.firstOrNull()?.let { chat ->
                    showMessagesForChat(chat.id)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabNewChat.setOnClickListener {
            viewModel.createNewChat()
        }

        binding.fabSendMessage.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.allChats.value?.firstOrNull()?.let { chat ->
                    val isOnline = isNetworkAvailable()
                    viewModel.sendMessage(chat.id, message, isOnline)
                    binding.messageInput.text?.clear()

                    if (!isOnline) {
                        showAlert("Message queued", "Your message will be sent when you're back online")
                    }
                }
            }
        }
    }

    private fun showMessagesForChat(chatId: String) {
        viewModel.getMessagesForChat(chatId).observe(this) { messages ->
            // Update UI with messages for this chat
            binding.messagesContainer.removeAllViews()

            messages.forEach { message ->
                val messageView = layoutInflater.inflate(
                    if (message.isFromServer) R.layout.item_message_received else R.layout.item_message_sent,
                    binding.messagesContainer,
                    false
                )

                val messageText = if (message.isFromServer) {
                    messageView.findViewById<android.widget.TextView>(R.id.received_message_text)
                } else {
                    messageView.findViewById<android.widget.TextView>(R.id.sent_message_text)
                }

                messageText.text = message.content
                binding.messagesContainer.addView(messageView)
            }

            // Scroll to bottom
            binding.messagesContainer.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun checkNetworkConnection() {
        if (!isNetworkAvailable()) {
            showAlert("Offline Mode", "You're currently offline. Messages will be sent when you reconnect.")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearAllChats()
    }
}