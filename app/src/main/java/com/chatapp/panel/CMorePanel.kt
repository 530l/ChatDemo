package com.chatapp.panel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.chatapp.App
import com.chatapp.R
import com.chatapp.utils.DensityUtil

/**
 * "更多"功能面板视图。
 * 用于展示如发送图片、文件、位置等扩展功能的入口。
 */
class CMorePanel : FrameLayout, IPanel {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // 加载 "更多" 面板的布局文件
        LayoutInflater.from(context).inflate(R.layout.layout_more_panel, this, true)
        init()
    }

    /**
     * 当视图的可见性发生变化时调用。
     * 用于在面板显示时根据键盘高度调整自身高度。
     */
    override fun onVisibilityChanged(
        changedView: View,
        visibility: Int
    ) {
        super.onVisibilityChanged(changedView, visibility)
        val layoutParams = layoutParams
        layoutParams.width = LayoutParams.MATCH_PARENT
        layoutParams.height = getPanelHeight() // 设置面板高度
        setLayoutParams(layoutParams)
    }

    private fun init() {
        // 可以在这里初始化面板内的具体功能按钮等
    }

    // 用于延迟隐藏面板的 Runnable
    private val mMorePanelInvisibleRunnable =
        Runnable { visibility = GONE }

    /**
     * 重置"更多"功能面板状态，使其隐藏。
     */
    override fun reset() {
        postDelayed(mMorePanelInvisibleRunnable, 0)
    }

    /**
     * 获取"更多"功能面板的期望高度。
     * 高度通常基于已记录的键盘高度进行计算，并可能减去一些固定高度（如输入框本身的高度）。
     *
     * @return 面板的期望高度（像素值）。
     */
    override fun getPanelHeight(): Int {
        // 示例：键盘高度减去一个大致的输入框高度
        return App.instance.keyboardHeight - DensityUtil.dp2px(context, 56.0f)
    }
}