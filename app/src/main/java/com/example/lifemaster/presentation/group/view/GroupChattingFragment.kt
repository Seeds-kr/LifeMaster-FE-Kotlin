package com.example.lifemaster.presentation.group.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.BuildConfig
import com.example.lifemaster.R
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenProvider
import com.example.lifemaster.presentation.group.adapter.GroupChatAdapter
import com.example.lifemaster.presentation.group.model.GroupChatMessage
import com.example.lifemaster.presentation.group.model.GroupChatReadEvent
import com.example.lifemaster.presentation.group.websocket.ChatWebSocketManager
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class GroupChattingFragment : Fragment(R.layout.fragment_group_chatting) {

    private val args: GroupChattingFragmentArgs by navArgs()

    @Inject
    lateinit var networkService: NetworkService

    private lateinit var tvTitle: TextView
    private lateinit var tvMemberCount: TextView
    private lateinit var tvChatDate: TextView
    private lateinit var tvEmptyChat: TextView
    private lateinit var rvChatList: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: MaterialCardView
    private lateinit var includeBackButton: View
    private lateinit var btnChatClose: View

    private lateinit var chatAdapter: GroupChatAdapter
    private lateinit var socketManager: ChatWebSocketManager

    private val chatItems = mutableListOf<GroupChatMessage>()

    private var myMemberId: Long = -1L
    private var groupId: Long = -1L
    private var isSocketReady = false
    private var nextLocalMessageId = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = args.groupId
        myMemberId = TokenProvider.getMemberId(requireContext()) ?: -1L
        val token = TokenProvider.getBearerToken(requireContext())

        includeBackButton = view.findViewById(R.id.include_back_button)
        tvTitle = view.findViewById(R.id.tv_group_name)
        tvMemberCount = view.findViewById(R.id.tv_group_member_count)
        tvChatDate = view.findViewById(R.id.tv_chat_date)
        tvEmptyChat = view.findViewById(R.id.tv_empty_chat)
        rvChatList = view.findViewById(R.id.rv_chat_list)
        etMessage = view.findViewById(R.id.et_message)
        btnSend = view.findViewById(R.id.btn_send)
        btnChatClose = view.findViewById(R.id.btn_chat_close)

        tvTitle.text = args.groupName
        tvMemberCount.text = if (args.memberCount > 0) "${args.memberCount}명 참여 중" else ""
        tvChatDate.text = buildTodayLabel()

        includeBackButton = view.findViewById(R.id.include_back_button)

        includeBackButton.setOnClickListener {
            if (!findNavController().popBackStack()) {
                findNavController().navigate(R.id.groupFragment)
            }
        }

        includeBackButton.findViewById<View>(R.id.btn_back)?.setOnClickListener {
            if (!findNavController().popBackStack()) {
                findNavController().navigate(R.id.groupFragment)
            }
        }

        btnChatClose.setOnClickListener {
            findNavController().popBackStack()
        }

        chatAdapter = GroupChatAdapter(myUserId = myMemberId)

        rvChatList.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        rvChatList.adapter = chatAdapter
        rvChatList.itemAnimator = null

        btnSend.isEnabled = false
        btnSend.alpha = 0.5f

        etMessage.doAfterTextChanged {
            val enabled = !it.isNullOrBlank()
            btnSend.isEnabled = enabled
            btnSend.alpha = if (enabled) 1f else 0.5f
        }

        socketManager = ChatWebSocketManager(
            baseHttpUrl = BuildConfig.BASE_URL,
            authToken = token
        )

        btnSend.setOnClickListener {
            val text = etMessage.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) return@setOnClickListener

            if (myMemberId <= 0L) {
                Toast.makeText(requireContext(), "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val localMessage = GroupChatMessage(
                id = nextLocalMessageId--,
                groupId = groupId,
                senderId = myMemberId,
                senderName = null,
                content = text,
                type = "CHAT",
                timestamp = Instant.now().toString()
            )

            addLocalMessage(localMessage)
            etMessage.setText("")

            if (isSocketReady) {
                Log.d("GroupChat", "send payload groupId=$groupId senderId=$myMemberId text=$text")
                socketManager.sendChat(
                    GroupChatMessage(
                        groupId = groupId,
                        senderId = myMemberId,
                        senderName = null,
                        content = text,
                        type = "CHAT"
                    )
                )
            } else {
                Toast.makeText(requireContext(), "채팅 서버 연결 중입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        connectAndLoad()
    }

    private fun connectAndLoad() {
        socketManager.connect(
            onConnected = {
                Log.d("GroupChat", "socket connected, groupId=$groupId")
                isSocketReady = true

                socketManager.subscribeChat(groupId) { message ->
                    handleIncomingMessage(message)
                }

                socketManager.subscribeRead(groupId) { readEvent ->
                    handleReadEvent(readEvent)
                }

                loadInitialChats()
            },
            onError = { error ->
                Log.e("GroupChat", "socket error", error)
                isSocketReady = false
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "채팅 연결 실패: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun loadInitialChats() {
        val token = TokenProvider.getBearerToken(requireContext())
        if (token.isNullOrBlank()) {
            bindEmptyState()
            return
        }

        lifecycleScope.launch {
            val resp = withContext(Dispatchers.IO) {
                runCatching {
                    networkService.getGroupChats(
                        token = token,
                        groupId = groupId
                    )
                }.getOrNull()
            }

            if (resp?.isSuccessful != true) {
                Log.e("GroupChat", "loadInitialChats fail code=${resp?.code()}")
                bindEmptyState()
                return@launch
            }

            val body = resp.body().orEmpty()
            Log.d("GroupChat", "loadInitialChats size=${body.size} body=$body")

            val list = body.sortedWith(
                compareBy<GroupChatMessage> { parseTimeForSort(it.timestamp) ?: OffsetDateTime.MIN }
                    .thenBy { it.id ?: Long.MAX_VALUE }
            )

            mergeServerMessages(list)
            bindEmptyState()
            scrollToBottom()

            val lastMessageId = list.lastOrNull()?.id
            if (lastMessageId != null && myMemberId > 0L && isSocketReady) {
                socketManager.sendRead(
                    GroupChatReadEvent(
                        groupId = groupId,
                        messageId = lastMessageId,
                        readerId = myMemberId
                    )
                )
            }
        }
    }

    private fun handleIncomingMessage(message: GroupChatMessage) {
        if (message.groupId != groupId) return

        Log.d("GroupChat", "incoming message=$message")

        val exists = message.id != null && chatItems.any { it.id == message.id }
        if (exists) return

        val replaced = replaceLocalPendingMessageIfMatched(message)
        if (!replaced) {
            chatItems.add(message)
        }

        sortMessages()
        submitMessages()
        bindEmptyState()
        scrollToBottom()

        if (message.senderId != myMemberId && message.id != null && myMemberId > 0L && isSocketReady) {
            socketManager.sendRead(
                GroupChatReadEvent(
                    groupId = groupId,
                    messageId = message.id,
                    readerId = myMemberId
                )
            )
        }
    }

    private fun handleReadEvent(event: GroupChatReadEvent) {
        if (event.groupId != groupId) return
        Log.d("GroupChat", "read event=$event")
    }

    private fun addLocalMessage(message: GroupChatMessage) {
        chatItems.add(message)
        sortMessages()
        submitMessages()
        bindEmptyState()
        scrollToBottom()
    }

    private fun mergeServerMessages(serverMessages: List<GroupChatMessage>) {
        val merged = mutableListOf<GroupChatMessage>()
        merged.addAll(chatItems.filter { (it.id ?: 0L) < 0L })

        serverMessages.forEach { serverMessage ->
            val replaced = replaceLocalPendingMessageInList(merged, serverMessage)
            if (!replaced) {
                val exists = merged.any { old ->
                    old.id != null && serverMessage.id != null && old.id == serverMessage.id
                }
                if (!exists) merged.add(serverMessage)
            }
        }

        chatItems.clear()
        chatItems.addAll(merged)
        sortMessages()
        submitMessages()
    }

    private fun replaceLocalPendingMessageIfMatched(serverMessage: GroupChatMessage): Boolean {
        return replaceLocalPendingMessageInList(chatItems, serverMessage)
    }

    private fun replaceLocalPendingMessageInList(
        list: MutableList<GroupChatMessage>,
        serverMessage: GroupChatMessage
    ): Boolean {
        if (serverMessage.senderId != myMemberId) return false

        val serverTime = parseTimeForSort(serverMessage.timestamp)

        val index = list.indexOfFirst { local ->
            val isLocalTemp = (local.id ?: 0L) < 0L
            if (!isLocalTemp) return@indexOfFirst false
            if (local.senderId != serverMessage.senderId) return@indexOfFirst false
            if (local.content != serverMessage.content) return@indexOfFirst false

            val localTime = parseTimeForSort(local.timestamp)
            if (localTime == null || serverTime == null) return@indexOfFirst true

            kotlin.math.abs(Duration.between(localTime, serverTime).seconds) <= 10
        }

        if (index == -1) return false

        list[index] = serverMessage
        return true
    }

    private fun sortMessages() {
        chatItems.sortWith(
            compareBy<GroupChatMessage> { parseTimeForSort(it.timestamp) ?: OffsetDateTime.MIN }
                .thenBy { it.id ?: Long.MAX_VALUE }
        )
    }

    private fun submitMessages() {
        chatAdapter.submitList(chatItems.toList())
    }

    private fun scrollToBottom() {
        rvChatList.post {
            if (chatItems.isNotEmpty()) {
                rvChatList.scrollToPosition(chatItems.lastIndex)
            }
        }
    }

    private fun bindEmptyState() {
        val empty = chatItems.isEmpty()
        tvEmptyChat.visibility = if (empty) View.VISIBLE else View.GONE
        rvChatList.visibility = if (empty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isSocketReady = false
        socketManager.disconnect()
    }

    private fun buildTodayLabel(): String {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val formatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREA)
        return today.format(formatter)
    }

    private fun parseTimeForSort(timestamp: String?): OffsetDateTime? {
        if (timestamp.isNullOrBlank()) return null

        return runCatching {
            OffsetDateTime.parse(timestamp)
        }.recoverCatching {
            Instant.parse(timestamp).atOffset(ZoneOffset.UTC)
        }.recoverCatching {
            LocalDateTime.parse(timestamp).atOffset(ZoneOffset.UTC)
        }.getOrNull()
    }
}