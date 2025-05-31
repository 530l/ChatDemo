package com.chatapp.panel

/**
 * 输入面板状态改变监听器接口。
 * 用于通知外部组件当前显示的具体面板类型（如软键盘、表情面板等）。
 */
interface OnInputPanelStateChangedListener {
    /**
     * 当显示语音输入面板时调用。
     */
    fun onShowVoicePanel()

    /**
     * 当显示软键盘输入面板时调用。
     */
    fun onShowInputMethodPanel()

    /**
     * 当显示表情选择面板时调用。
     */
    fun onShowExpressionPanel()

    /**
     * 当显示更多功能面板时调用。
     */
    fun onShowMorePanel()
}