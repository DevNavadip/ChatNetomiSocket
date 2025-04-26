package com.netomi.chat.Repository

import androidx.lifecycle.asLiveData
import com.netomi.chat.DataModels.Chat
import com.netomi.chat.DataModels.Message
import com.netomi.chat.Database.ChatDao
import com.netomi.chat.Database.MessageDao
import com.netomi.chat.SocketIO.SocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val socketService: SocketService
) {
    val allChats = chatDao.getAllChats().asLiveData()

    fun getMessagesForChat(chatId: String) = messageDao.getMessagesForChat(chatId).asLiveData()

    suspend fun createNewChat(): Chat {
        val chat = Chat()
        chatDao.insert(chat)
        return chat
    }

    suspend fun sendMessage(chatId: String, content: String, isOnline: Boolean) {
        val message = Message(
            chatId = chatId,
            content = content,
            isSent = isOnline,
            isDelivered = isOnline
        )

        messageDao.insert(message)

        // Update chat last message
        val chat = chatDao.getChatById(chatId) ?: return
        val updatedChat = chat.copy(
            lastMessage = content,
            timestamp = System.currentTimeMillis(),
            isUnread = false
        )
        chatDao.update(updatedChat)

        if (isOnline) {
            try {
                val jsonObject = JSONObject().apply {
                    put("chatId", chatId)
                    put("message", content)
                    put("messageId", message.id)
                }
                socketService.sendMessage(jsonObject)
            } catch (e: Exception) {
                // Mark message as not sent if socket fails
                messageDao.markMessageAsDelivered(message.id)
            }
        }
    }

    suspend fun handleIncomingMessage(chatId: String, message: String) {
        val msg = Message(
            chatId = chatId,
            content = message,
            isSent = true,
            isDelivered = true,
            isFromServer = true
        )

        messageDao.insert(msg)

        // Update chat last message and mark as unread
        val chat = chatDao.getChatById(chatId) ?: return
        val updatedChat = chat.copy(
            lastMessage = message,
            timestamp = System.currentTimeMillis(),
            isUnread = true
        )
        chatDao.update(updatedChat)
    }

    fun clearAllChats(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            chatDao.clearAllChats()
        }
    }

    suspend fun retryPendingMessages() {
        val pendingMessages = messageDao.getPendingMessages()
        pendingMessages.forEach { message ->
            sendMessage(message.chatId, message.content, true)
        }
    }

    suspend fun markChatAsRead(chatId: String) {
        chatDao.updateUnreadStatus(chatId, false)
    }
}