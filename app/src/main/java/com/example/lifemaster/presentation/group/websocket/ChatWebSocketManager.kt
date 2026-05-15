package com.example.lifemaster.presentation.group.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.lifemaster.presentation.group.model.GroupChatMessage
import com.example.lifemaster.presentation.group.model.GroupChatReadEvent
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ChatWebSocketManager(
    private val baseHttpUrl: String,
    private val authToken: String?
) {
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnecting = false
    private var isConnected = false
    private var closedByClient = false

    private var onConnected: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    private val chatHandlers = mutableMapOf<Long, (GroupChatMessage) -> Unit>()
    private val readHandlers = mutableMapOf<Long, (GroupChatReadEvent) -> Unit>()

    private val pendingChatSubscriptions = mutableListOf<Long>()
    private val pendingReadSubscriptions = mutableListOf<Long>()

    fun connect(
        onConnected: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        if (isConnecting || isConnected) return

        this.onConnected = onConnected
        this.onError = onError
        this.closedByClient = false
        this.isConnecting = true

        Thread {
            try {
                val infoUrl = buildSockJsInfoUrl(baseHttpUrl)
                Log.d("ChatSocket", "sockjs info url=$infoUrl")

                val infoBuilder = Request.Builder()
                    .url(infoUrl)
                    .get()

                if (!authToken.isNullOrBlank()) {
                    infoBuilder.addHeader("Authorization", authToken)
                }

                client.newCall(infoBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("SockJS info failed: ${response.code}")
                    }
                }

                val serverId = Random.nextInt(100, 999).toString()
                val sessionId = UUID.randomUUID().toString().replace("-", "")
                val wsUrl = buildSockJsWebSocketUrl(baseHttpUrl, serverId, sessionId)

                Log.d("ChatSocket", "sockjs websocket url=$wsUrl")

                val wsBuilder = Request.Builder()
                    .url(wsUrl)

                if (!authToken.isNullOrBlank()) {
                    wsBuilder.addHeader("Authorization", authToken)
                }

                webSocket = client.newWebSocket(
                    wsBuilder.build(),
                    object : WebSocketListener() {
                        override fun onOpen(webSocket: WebSocket, response: Response) {
                            Log.d("ChatSocket", "websocket opened")
                        }

                        override fun onMessage(webSocket: WebSocket, text: String) {
                            handleSockJsFrame(text)
                        }

                        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                            Log.d("ChatSocket", "closing code=$code reason=$reason")
                            isConnecting = false
                            isConnected = false
                            webSocket.close(code, reason)
                        }

                        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                            Log.d("ChatSocket", "closed code=$code reason=$reason")
                            isConnecting = false
                            isConnected = false
                        }

                        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                            Log.e("ChatSocket", "failure code=${response?.code} message=${response?.message}", t)
                            isConnecting = false
                            isConnected = false
                            if (!closedByClient) {
                                mainHandler.post { this@ChatWebSocketManager.onError?.invoke(t) }
                            }
                        }
                    }
                )
            } catch (t: Throwable) {
                Log.e("ChatSocket", "connect exception", t)
                isConnecting = false
                isConnected = false
                mainHandler.post { this.onError?.invoke(t) }
            }
        }.start()
    }

    fun subscribeChat(
        groupId: Long,
        onMessageReceived: (GroupChatMessage) -> Unit
    ) {
        chatHandlers[groupId] = onMessageReceived
        if (isConnected) {
            sendSubscribe("/topic/chat/$groupId")
        } else if (!pendingChatSubscriptions.contains(groupId)) {
            pendingChatSubscriptions.add(groupId)
        }
    }

    fun subscribeRead(
        groupId: Long,
        onReadEventReceived: (GroupChatReadEvent) -> Unit
    ) {
        readHandlers[groupId] = onReadEventReceived
        if (isConnected) {
            sendSubscribe("/topic/chat/read/$groupId")
        } else if (!pendingReadSubscriptions.contains(groupId)) {
            pendingReadSubscriptions.add(groupId)
        }
    }

    fun sendChat(message: GroupChatMessage) {
        // content / message 둘 다 넣어서 서버 DTO 차이 방어
        val payload = gson.toJson(
            linkedMapOf(
                "groupId" to message.groupId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "content" to message.content,
                "message" to message.content,
                "type" to message.type
            )
        )

        sendStompFrame(
            command = "SEND",
            headers = linkedMapOf(
                "destination" to "/app/chat/send",
                "content-type" to "application/json",
                "receipt" to UUID.randomUUID().toString()
            ),
            body = payload
        )
    }

    fun sendRead(event: GroupChatReadEvent) {
        val payload = gson.toJson(event)

        sendStompFrame(
            command = "SEND",
            headers = linkedMapOf(
                "destination" to "/app/chat/read",
                "content-type" to "application/json",
                "receipt" to UUID.randomUUID().toString()
            ),
            body = payload
        )
    }

    fun disconnect() {
        closedByClient = true
        isConnecting = false
        isConnected = false
        webSocket?.close(1000, "client disconnect")
        webSocket = null
    }

    private fun handleSockJsFrame(raw: String) {
        Log.d("ChatSocket", "sockjs raw=$raw")

        when {
            raw == "o" -> sendConnectFrame()
            raw == "h" -> Log.d("ChatSocket", "sockjs heartbeat")
            raw.startsWith("a") -> {
                val arrayText = raw.substring(1)
                runCatching {
                    val arr = JSONArray(arrayText)
                    for (i in 0 until arr.length()) {
                        handleStompFrame(arr.getString(i))
                    }
                }.onFailure {
                    Log.e("ChatSocket", "sockjs parse error", it)
                    mainHandler.post { onError?.invoke(it) }
                }
            }
            raw.startsWith("c") -> {
                Log.e("ChatSocket", "sockjs close frame=$raw")
                isConnecting = false
                isConnected = false
            }
        }
    }

    private fun handleStompFrame(frame: String) {
        Log.d("ChatSocket", "stomp in=$frame")

        val normalized = frame.replace("\r\n", "\n")
        val command = normalized.substringBefore('\n').trim()
        val headerAndBody = normalized.substringAfter('\n', "")
        val splitIndex = headerAndBody.indexOf("\n\n")

        val headersPart = if (splitIndex >= 0) {
            headerAndBody.substring(0, splitIndex)
        } else {
            headerAndBody
        }

        val bodyPart = if (splitIndex >= 0) {
            headerAndBody.substring(splitIndex + 2)
        } else {
            ""
        }.removeSuffix("\u0000")

        val headers = headersPart
            .lines()
            .filter { it.contains(":") }
            .associate {
                val idx = it.indexOf(':')
                it.substring(0, idx) to it.substring(idx + 1)
            }

        when (command) {
            "CONNECTED" -> {
                isConnecting = false
                isConnected = true
                flushPendingSubscriptions()
                mainHandler.post { onConnected?.invoke() }
            }

            "MESSAGE" -> {
                val destination = headers["destination"].orEmpty()
                Log.d("ChatSocket", "message destination=$destination")
                Log.d("ChatSocket", "message raw body=$bodyPart")

                if (destination.startsWith("/topic/chat/read/")) {
                    runCatching {
                        gson.fromJson(bodyPart, GroupChatReadEvent::class.java)
                    }.onSuccess { event ->
                        readHandlers[event.groupId]?.let { handler ->
                            mainHandler.post { handler(event) }
                        }
                    }.onFailure {
                        Log.e("ChatSocket", "read parse error body=$bodyPart", it)
                    }
                } else if (destination.startsWith("/topic/chat/")) {
                    runCatching {
                        gson.fromJson(bodyPart, GroupChatMessage::class.java)
                    }.onSuccess { message ->
                        chatHandlers[message.groupId]?.let { handler ->
                            mainHandler.post { handler(message) }
                        }
                    }.onFailure {
                        Log.e("ChatSocket", "chat parse error body=$bodyPart", it)
                    }
                }
            }

            "RECEIPT" -> {
                Log.d("ChatSocket", "receipt headers=$headers")
            }

            "ERROR" -> {
                Log.e("ChatSocket", "stomp error body=$bodyPart headers=$headers")
                mainHandler.post {
                    onError?.invoke(IllegalStateException("STOMP ERROR: $bodyPart"))
                }
            }
        }
    }

    private fun sendConnectFrame() {
        sendStompFrame(
            command = "CONNECT",
            headers = linkedMapOf(
                "accept-version" to "1.1,1.2",
                "heart-beat" to "0,0"
            ),
            body = ""
        )
    }

    private fun flushPendingSubscriptions() {
        pendingChatSubscriptions.forEach { groupId ->
            sendSubscribe("/topic/chat/$groupId")
        }
        pendingChatSubscriptions.clear()

        pendingReadSubscriptions.forEach { groupId ->
            sendSubscribe("/topic/chat/read/$groupId")
        }
        pendingReadSubscriptions.clear()
    }

    private fun sendSubscribe(destination: String) {
        sendStompFrame(
            command = "SUBSCRIBE",
            headers = linkedMapOf(
                "id" to UUID.randomUUID().toString(),
                "destination" to destination,
                "receipt" to UUID.randomUUID().toString()
            ),
            body = ""
        )
    }

    private fun sendStompFrame(
        command: String,
        headers: Map<String, String>,
        body: String
    ) {
        val socket = webSocket ?: return

        val frame = buildString {
            append(command).append('\n')
            headers.forEach { (k, v) ->
                append(k).append(':').append(v).append('\n')
            }
            append('\n')
            append(body)
            append('\u0000')
        }

        val outgoing = JSONArray().put(frame).toString()

        Log.d("ChatSocket", "stomp out=$frame")
        Log.d("ChatSocket", "sockjs out=$outgoing")

        socket.send(outgoing)
    }

    private fun buildSockJsInfoUrl(base: String): String {
        val trimmed = base.trim().removeSuffix("/")
        return when {
            trimmed.startsWith("https://", true) -> "$trimmed/ws/chat/info"
            trimmed.startsWith("http://", true) -> "$trimmed/ws/chat/info"
            trimmed.startsWith("wss://", true) ->
                trimmed.replaceFirst("wss://", "https://") + "/ws/chat/info"
            trimmed.startsWith("ws://", true) ->
                trimmed.replaceFirst("ws://", "http://") + "/ws/chat/info"
            else -> "https://$trimmed/ws/chat/info"
        }
    }

    private fun buildSockJsWebSocketUrl(
        base: String,
        serverId: String,
        sessionId: String
    ): String {
        val trimmed = base.trim().removeSuffix("/")
        return when {
            trimmed.startsWith("https://", true) ->
                trimmed.replaceFirst("https://", "wss://") + "/ws/chat/$serverId/$sessionId/websocket"
            trimmed.startsWith("http://", true) ->
                trimmed.replaceFirst("http://", "ws://") + "/ws/chat/$serverId/$sessionId/websocket"
            trimmed.startsWith("wss://", true) ->
                "$trimmed/ws/chat/$serverId/$sessionId/websocket"
            trimmed.startsWith("ws://", true) ->
                "$trimmed/ws/chat/$serverId/$sessionId/websocket"
            else -> "wss://$trimmed/ws/chat/$serverId/$sessionId/websocket"
        }
    }
}