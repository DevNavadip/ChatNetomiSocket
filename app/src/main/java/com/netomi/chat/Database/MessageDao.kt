package com.netomi.chat.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.netomi.chat.DataModels.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<Message>>

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("SELECT * FROM messages WHERE isSent = 0")
    suspend fun getPendingMessages(): List<Message>

    @Query("UPDATE messages SET isSent = 1, isDelivered = 1 WHERE id = :messageId")
    suspend fun markMessageAsDelivered(messageId: String)
}