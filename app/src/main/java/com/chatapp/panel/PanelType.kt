package com.chatapp.panel

/**
 * 定义了聊天界面中可能出现的各种面板类型。
 */
enum class PanelType {

    /**
     * 面板类型：软键盘输入法。
     */
    INPUT_MOTHOD,

    /**
     * 面板类型：语音输入。
     */
    VOICE,

    /**
     * 面板类型：表情选择。
     */
    EXPRESSION,

    /**
     * 面板类型：更多功能（如发送图片、文件等）。
     */
    MORE,

    /**
     * 面板类型：无，表示当前没有特殊面板显示，或者只有文本输入框。
     */
    NONE
}