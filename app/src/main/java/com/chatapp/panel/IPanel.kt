package com.chatapp.panel

/**
 * 通用面板接口，定义了面板的基本行为。
 */
interface IPanel {

    /**
     * 重置面板状态，通常用于隐藏面板或恢复到初始状态。
     */
    fun reset()

    /**
     * 获取面板的高度。
     *
     * @return 面板的实际高度（像素值）。
     */
    fun getPanelHeight(): Int
}