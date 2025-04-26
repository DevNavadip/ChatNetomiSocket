package com.netomi.chat.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.netomi.chat.DataModels.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(chat: Chat)

    @Update
    suspend fun update(chat: Chat)

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<Chat>>

    @Query("DELETE FROM chats")
    suspend fun clearAllChats()

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): Chat?

    @Query("UPDATE chats SET isUnread = :isUnread WHERE id = :chatId")
    suspend fun updateUnreadStatus(chatId: String, isUnread: Boolean)
}