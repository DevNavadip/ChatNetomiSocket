package com.netomi.chat.SocketIO

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.netomi.chat.ChatApplication
import com.netomi.chat.Database.AppDatabase
import com.netomi.chat.Repository.ChatRepository
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class SocketService : Service() {
    private lateinit var socket: Socket
    private lateinit var repository: ChatRepository

    override fun onCreate() {
        super.onCreate()
        val app = application as ChatApplication
        socket = app.getSocket()
        repository = ChatRepository(
            AppDatabase.getDatabase(this).chatDao(),
            AppDatabase.getDatabase(this).messageDao(),
            this
        )

        setupSocketListeners()
        connectSocket()
    }

    private fun setupSocketListeners() {
        socket.on(Socket.EVENT_CONNECT) {
            Log.d("SocketService", "Connected to PieSocket")
            CoroutineScope(Dispatchers.IO).launch {
                repository.retryPendingMessages()
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketService", "Disconnected from PieSocket")
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e("SocketService", "Connection error: ${args[0]}")
        }

        socket.on("new_message") { args ->
            val data = args[0] as JSONObject
            val chatId = data.getString("chatId")
            val message = data.getString("message")

            CoroutineScope(Dispatchers.IO).launch {
                repository.handleIncomingMessage(chatId, message)
            }
        }

        socket.on("message_delivered") { args ->
            val data = args[0] as JSONObject
            val messageId = data.getString("messageId")

            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(this@SocketService)
                    .messageDao()
                    .markMessageAsDelivered(messageId)
            }
        }
    }

    fun sendMessage(data: JSONObject) {
        if (socket.connected()) {
            socket.emit("new_message", data)
        }
    }

    private fun connectSocket() {
        if (!socket.connected()) {
            socket.connect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }
}