package com.chatapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val messageList = mutableListOf<Message>()
    private val TIMESTAMP_THRESHOLD_MILLIS = 5 * 60 * 1000 // 5 minutes

    init {
        loadDummyMessages()
    }

    fun sendMessage(content: String) {
        if (content.isNotEmpty()) {
            val newMessage = Message("me", content, Date().time)
            updateMessageVisibility(newMessage)
            messageList.add(newMessage)
            _messages.value = messageList.toList()
        }
    }

    private fun updateMessageVisibility(newMessage: Message) {
        if (messageList.isNotEmpty()) {
            // 检查是否需要显示发送者信息
            newMessage.showSenderInfo = messageList.last().sender != newMessage.sender
            
            // 检查是否需要显示时间戳
            newMessage.showTimestamp = newMessage.timestamp - messageList.last().timestamp >= TIMESTAMP_THRESHOLD_MILLIS
            
            // 如果新消息显示时间戳，检查前一条消息是否需要更新时间戳
            if (newMessage.showTimestamp && !messageList.last().showTimestamp) {
                messageList.last().showTimestamp = true
            }
        }
    }

    private fun loadDummyMessages() {
        messageList.clear()
        
        // 添加示例消息
        addMessage(Message("other", "你好！", Date().time - 120000))
        addMessage(Message("me", "你好！", Date().time - 110000))
        addMessage(Message("other", "今天天气不错。", Date().time - 100000))
        addMessage(Message("me", "是的，很适合出去玩。", Date().time - 90000))
        addMessage(Message("other", null, Date().time - 80000, R.drawable.sym_def_app_icon))
        addMessage(Message("me", "你去哪儿玩了？", Date().time - 70000))
        addMessage(Message("me", null, Date().time - 60000, R.drawable.btn_star_big_on))
        addMessage(Message("other", "拍了几张照片。", Date().time - 50000))
        addMessage(Message("other", null, Date().time - 40000, R.drawable.ic_menu_gallery))
        addMessage(Message("me", "这些照片真棒！", Date().time - 30000))
        addMessage(Message("other", "谢谢！", Date().time - 20000))
        addMessage(Message("me", "不客气。", Date().time - 10000))
        addMessage(Message("other", "", Date().time - 5000, R.drawable.ic_dialog_info))

        updateTimestampVisibility()
        _messages.value = messageList.toList()
    }

    private fun addMessage(message: Message) {
        if (messageList.isNotEmpty() && messageList.last().sender == message.sender) {
            message.showSenderInfo = false
        }
        messageList.add(message)
    }

    private fun updateTimestampVisibility() {
        if (messageList.isEmpty()) return

        messageList[0].showTimestamp = true

        for (i in 1 until messageList.size) {
            val currentTime = messageList[i].timestamp
            val previousTime = messageList[i - 1].timestamp
            messageList[i].showTimestamp = (currentTime - previousTime > TIMESTAMP_THRESHOLD_MILLIS)
        }
    }
} 