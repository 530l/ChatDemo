package com.chatapp.panel

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.PopupWindow
import com.chatapp.utils.DensityUtil
import androidx.core.graphics.drawable.toDrawable

/**
 * 一个 PopupWindow，用于监听软键盘的显示状态和高度。
 * 通过监听全局布局变化，计算可见显示区域来判断软键盘的开启和关闭，并获取其高度。
 *
 * @param context 上下文环境。
 * @param anchorView PopupWindow 依附的视图，通常是 Activity 的根视图。
 */
class KeyboardStatePopupWindow(var context: Context, anchorView: View) : PopupWindow(),
    ViewTreeObserver.OnGlobalLayoutListener {

    init {
        val contentView = View(context)
        setContentView(contentView)
        width = 0 // 设为0，使其不可见但仍能接收事件
        height = ViewGroup.LayoutParams.MATCH_PARENT
        setBackgroundDrawable(Color.TRANSPARENT.toDrawable()) // 透明背景
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE // 确保窗口调整以适应软键盘
        inputMethodMode = INPUT_METHOD_NEEDED // 需要输入法
        contentView.viewTreeObserver.addOnGlobalLayoutListener(this) // 添加布局变化监听

        // 延迟显示 PopupWindow，确保 anchorView 已经布局完成
        anchorView.post {
            showAtLocation(
                anchorView,
                Gravity.NO_GRAVITY,
                0,
                0
            )
        }
    }

    private var maxHeight = 0 // 记录屏幕内容区域的最大高度
    private var isSoftKeyboardOpened = false // 标记软键盘是否已打开

    /**
     * 全局布局变化回调。
     * 当布局发生变化时（例如软键盘弹出或收起），此方法被调用。
     */
    override fun onGlobalLayout() {
        val rect = Rect()
        contentView.getWindowVisibleDisplayFrame(rect) // 获取当前窗口可见显示区域
        if (rect.bottom > maxHeight) { // 初始化或屏幕旋转后更新 maxHeight
            maxHeight = rect.bottom
        }
        val screenHeight: Int = DensityUtil.getScreenHeight(context) // 获取屏幕总高度
        // 键盘的高度 = 内容区域最大高度 - 当前可见区域底部
        val keyboardHeight = maxHeight - rect.bottom
        // 判断键盘是否可见，通常认为键盘高度大于屏幕1/4时为可见
        val visible = keyboardHeight > screenHeight / 4
        if (!isSoftKeyboardOpened && visible) { // 键盘从关闭到打开
            isSoftKeyboardOpened = true
            onKeyboardStateListener?.onOpened(keyboardHeight) // 回调键盘打开事件，并传递高度
            KeyboardHelper.keyboardHeight = keyboardHeight // 更新全局键盘高度记录
        } else if (isSoftKeyboardOpened && !visible) { // 键盘从打开到关闭
            isSoftKeyboardOpened = false
            onKeyboardStateListener?.onClosed() // 回调键盘关闭事件
        }
    }

    /**
     * 释放资源，移除布局监听器。
     * 在 Activity 或 Fragment 销毁时调用，防止内存泄漏。
     */
    fun release() {
        contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    private var onKeyboardStateListener: OnKeyboardStateListener? = null

    /**
     * 设置软键盘状态监听器。
     *
     * @param listener 监听器实例。
     */
    fun setOnKeyboardStateListener(listener: OnKeyboardStateListener?) {
        this.onKeyboardStateListener = listener
    }

    /**
     * 软键盘状态监听器接口。
     */
    interface OnKeyboardStateListener {
        /**
         * 当软键盘打开时调用。
         *
         * @param keyboardHeight 软键盘的实际高度（像素值）。
         */
        fun onOpened(keyboardHeight: Int)

        /**
         * 当软键盘关闭时调用。
         */
        fun onClosed()
    }
}