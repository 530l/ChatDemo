package com.chatapp.panel

/**
 * 输入面板接口，定义了输入区域特有的行为。
 * 输入区域通常包括文本输入框以及切换到其他面板（如表情、更多功能）的按钮。
 */
interface IInputPanel : IPanel {

    /**
     * 当软键盘打开时调用。
     */
    fun onSoftKeyboardOpened()

    /**
     * 当软键盘关闭时调用。
     */
    fun onSoftKeyboardClosed()

    /**
     * 设置布局动画处理监听器。
     * 当面板切换导致布局需要动画过渡时，通过此监听器执行动画逻辑。
     *
     * @param listener 一个 lambda 表达式，接收当前面板类型、上一个面板类型、动画起始值和结束值。
     */
    fun setOnLayoutAnimatorHandleListener(listener: ((panelType: PanelType,
                                                      lastPanelType: PanelType,
                                                      fromValue: Float, toValue: Float) -> Unit)?)

    /**
     * 设置输入面板状态改变监听器。
     * 用于监听输入面板（包括软键盘、表情、更多等）的显示状态变化。
     *
     * @param listener 状态变化监听器实例。
     */
    fun setOnInputStateChangedListener(listener: OnInputPanelStateChangedListener?)
}