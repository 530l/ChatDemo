package com.chatapp.model.entity

data class Message(
    val sender: String, // 发送者，可以是 "me" 或 "other"
    val content: String? = null, // 文本消息内容，可能为空，因为有图片消息
    val timestamp: Long,
    val imageResId: Int? = null, // 图片资源的 ID，可能为空
    var showSenderInfo: Boolean = true, // 是否显示发送者信息（头像和昵称）
    var showTimestamp: Boolean = true // 是否显示时间戳
)