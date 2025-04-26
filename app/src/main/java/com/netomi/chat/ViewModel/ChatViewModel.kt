package com.netomi.chat.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netomi.chat.Repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    val allChats = repository.allChats

    fun createNewChat() = viewModelScope.launch {
        repository.createNewChat()
    }

    fun sendMessage(chatId: String, content: String, isOnline: Boolean) = viewModelScope.launch {
        repository.sendMessage(chatId, content, isOnline)
    }

    fun handleIncomingMessage(chatId: String, message: String) = viewModelScope.launch {
        repository.handleIncomingMessage(chatId, message)
    }

    fun clearAllChats() {
        repository.clearAllChats(viewModelScope)
    }

    fun markChatAsRead(chatId: String) = viewModelScope.launch {
        repository.markChatAsRead(chatId)
    }

    fun getMessagesForChat(chatId: String) = repository.getMessagesForChat(chatId)
}