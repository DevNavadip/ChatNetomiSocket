package com.netomi.chat.DataModels

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = Chat::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("chatId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Message(
    @PrimaryKey val id: String = "msg_${System.currentTimeMillis()}",
    val chatId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = false,
    val isDelivered: Boolean = false,
    val isFromServer: Boolean = false
)