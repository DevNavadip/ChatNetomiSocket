package com.netomi.chat.DataModels

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String = generateChatId(),
    val title: String = "Chat $id",
    val lastMessage: String = "",
    val timestamp: Long = Date().time,
    val isUnread: Boolean = false,
    val isSent: Boolean = true
) {
    companion object {
        fun generateChatId(): String = "chat_${System.currentTimeMillis()}"
    }
}