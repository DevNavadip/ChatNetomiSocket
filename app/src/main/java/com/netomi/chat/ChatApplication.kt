package com.netomi.chat

import android.app.Application
import android.net.Uri
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URISyntaxException

class ChatApplication : Application() {
    private var socket: Socket? = null

    fun getSocket(): Socket {
        if (socket == null || !socket!!.connected()) {
            try {
                val options = IO.Options()
                options.reconnection = true
                options.reconnectionAttempts = Integer.MAX_VALUE
                options.transports = arrayOf(WebSocket.NAME)
                options.secure = true
                options.forceNew = true

                // For PieHost specifically:
                /*options.path = "/v3/1" // Your channel path
                options.query = "api_key=UwRRSqZoyuNHZirpvzglFDaAgQ5qxrukfK6jIymK"
                options.query = "notify_self=1"*/
                socket = IO.socket(
                    //"wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self",
                    //"wss://free.blr2.piesocket.com/v3/1?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self",
                    // "https://s14537.blr1.piesocket.com",
                    "wss://s14537.blr1.piesocket.com/v3/1?api_key=UwRRSqZoyuNHZirpvzglFDaAgQ5qxrukfK6jIymK&notify_self=1",
                    options
                )
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }
        return socket!!
    }

    override fun onTerminate() {
        super.onTerminate()
        socket?.disconnect()
    }
}