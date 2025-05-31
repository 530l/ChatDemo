package com.chatapp.panel

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView

/**
 * 键盘和相关面板（表情、更多等）的辅助管理类。
 * 核心功能包括：
 * 1. 监听软键盘的显示/隐藏状态及高度。
 * 2. 管理输入面板（[CInputPanel]）、表情面板（[CExpressionPanel]）、更多面板（[CMorePanel]）的显示切换。
 * 3. 处理面板切换时的动画效果，使 RecyclerView 和相关面板平滑移动。
 */
class KeyboardHelper {

    private lateinit var context: Context
    private var rootLayout: ViewGroup? = null // Activity 的根布局，用于 PopupWindow 依附
    private var recyclerView: RecyclerView? = null // 聊天消息列表
    private var inputPanel: IInputPanel? = null // 输入面板实例
    private var expressionPanel: IPanel? = null // 表情面板实例
    private var morePanel: IPanel? = null // 更多功能面板实例
    private var keyboardStatePopupWindow: KeyboardStatePopupWindow? = null // 用于监听键盘状态的 PopupWindow
    private var scrollBodyLayout: Boolean = false // 是否在面板切换时滚动 RecyclerView

    companion object {
        var keyboardHeight = 0 // 全局记录的键盘高度
        var inputPanelHeight = 0 // 输入面板的高度 (通常指软键盘的高度或者等效的自定义面板高度)
        var expressionPanelHeight = 0 // 表情面板的实际高度
        var morePanelHeight = 0 // 更多面板的实际高度
    }

    /**
     * 初始化 KeyboardHelper。
     *
     * @param context 上下文，通常是 Activity。
     * @return KeyboardHelper 实例，支持链式调用。
     */
    fun init(context: Context): KeyboardHelper {
        this.context = context
        return this
    }

    /**
     * 重置所有面板状态，通常用于隐藏所有面板和软键盘。
     */
    fun reset() {
        inputPanel?.reset()
        expressionPanel?.reset()
        morePanel?.reset()
    }

    /**
     * 释放资源。
     * 在 Activity 销毁时调用，以避免内存泄漏。
     */
    fun release() {
        reset()
        inputPanel = null
        expressionPanel = null
        morePanel = null
        keyboardStatePopupWindow?.dismiss() // 关闭 PopupWindow
        keyboardStatePopupWindow?.release() // 释放 PopupWindow 资源
        keyboardStatePopupWindow = null
    }

    /**
     * 设置一个预设的键盘高度。
     * 如果无法通过 [KeyboardStatePopupWindow] 自动获取，可以手动设置一个默认值。
     *
     * @param keyboardHeight 键盘高度（像素值）。
     * @return KeyboardHelper 实例。
     */
    fun setKeyboardHeight(keyboardHeight: Int): KeyboardHelper {
        KeyboardHelper.keyboardHeight = keyboardHeight
        // 如果输入面板高度未初始化，则使用此键盘高度作为初始值
        if (inputPanelHeight == 0) {
            inputPanelHeight = keyboardHeight
        }
        return this
    }

    /**
     * 绑定 Activity 的根布局。
     * [KeyboardStatePopupWindow] 将依附于此布局来监听键盘状态。
     *
     * @param rootLayout Activity 的根 ViewGroup。
     * @return KeyboardHelper 实例。
     */
    fun bindRootLayout(rootLayout: ViewGroup): KeyboardHelper {
        this.rootLayout = rootLayout
        if (context is Activity && !(context as Activity).isFinishing) {
            keyboardStatePopupWindow = KeyboardStatePopupWindow(context, rootLayout)
            keyboardStatePopupWindow?.setOnKeyboardStateListener(object :
                KeyboardStatePopupWindow.OnKeyboardStateListener {
                override fun onOpened(keyboardHeight: Int) {
                    KeyboardHelper.keyboardHeight = keyboardHeight // 更新全局键盘高度
                    inputPanel?.onSoftKeyboardOpened() // 通知输入面板软键盘已打开
                    onKeyboardStateListener?.onOpened(keyboardHeight) // 回调外部监听器

                    // 更新各个面板的记录高度，如果它们已绑定
                    inputPanel?.apply {
                        inputPanelHeight = getPanelHeight() // 通常是软键盘高度
                    }
                    expressionPanel?.apply {
                        expressionPanelHeight = getPanelHeight()
                    }
                    morePanel?.apply {
                        morePanelHeight = getPanelHeight()
                    }
                }

                override fun onClosed() {
                    inputPanel?.onSoftKeyboardClosed() // 通知输入面板软键盘已关闭
                    onKeyboardStateListener?.onClosed() // 回调外部监听器
                }
            })
        }
        return this
    }

    /**
     * 绑定 RecyclerView 实例。
     * 当面板切换时，可能需要调整 RecyclerView 的位置或大小。
     *
     * @param recyclerView 聊天消息列表的 RecyclerView。
     * @return KeyboardHelper 实例。
     */
    fun bindRecyclerView(recyclerView: RecyclerView): KeyboardHelper {
        this.recyclerView = recyclerView
        return this
    }

    /**
     * 绑定语音输入面板 (如果单独实现)。
     * @param panel 实现了 [IPanel] 接口的语音面板实例。
     * @return KeyboardHelper 实例。
     */
    fun <P : IPanel> bindVoicePanel(panel: P): KeyboardHelper {
        // 当前示例中语音集成在 CInputPanel，此方法可能用于更独立的语音面板设计
        return this
    }

    /**
     * 绑定输入面板 ([CInputPanel] 或其接口实现)。
     *
     * @param panel 实现了 [IInputPanel] 接口的输入面板实例。
     * @return KeyboardHelper 实例。
     */
    fun <P : IInputPanel> bindInputPanel(panel: P): KeyboardHelper {
        this.inputPanel = panel
        // 获取并记录输入面板的初始高度
        inputPanelHeight = panel.getPanelHeight()
        // 设置输入面板状态变化监听器，用于在 KeyboardHelper 中响应面板切换事件
        panel.setOnInputStateChangedListener(object : OnInputPanelStateChangedListener {
            // 当输入面板请求显示语音面板时
            override fun onShowVoicePanel() {

                // 隐藏表情和更多面板
                (expressionPanel as? ViewGroup)?.visibility = View.GONE
                (morePanel as? ViewGroup)?.visibility = View.GONE
            }

            // 当输入面板请求显示软键盘时
            override fun onShowInputMethodPanel() {
                // 隐藏表情和更多面板
                (expressionPanel as? ViewGroup)?.visibility = View.GONE
                (morePanel as? ViewGroup)?.visibility = View.GONE
            }

            // 当输入面板请求显示表情面板时
            override fun onShowExpressionPanel() {
                (expressionPanel as? ViewGroup)?.visibility = View.VISIBLE // 显示表情面板
                (morePanel as? ViewGroup)?.visibility = View.GONE // 隐藏更多面板
            }

            // 当输入面板请求显示更多面板时
            override fun onShowMorePanel() {
                (morePanel as? ViewGroup)?.visibility = View.VISIBLE // 显示更多面板
                (expressionPanel as? ViewGroup)?.visibility = View.GONE // 隐藏表情面板
            }
        })
        // 设置布局动画处理器，当输入面板内部触发面板切换时，调用此处的动画方法
        panel.setOnLayoutAnimatorHandleListener { panelType, lastPanelType, fromValue, toValue ->
            handlePanelMoveAnimator(panelType, lastPanelType, fromValue, toValue)
        }
        return this
    }

    /**
     * 绑定表情面板。
     *
     * @param panel 实现了 [IPanel] 接口的表情面板实例。
     * @return KeyboardHelper 实例。
     */
    fun <P : IPanel> bindExpressionPanel(panel: P): KeyboardHelper {
        this.expressionPanel = panel
        expressionPanelHeight = panel.getPanelHeight() // 获取并记录表情面板的高度
        (panel as? ViewGroup)?.visibility = View.GONE // 初始时隐藏
        return this
    }

    /**
     * 绑定更多功能面板。
     *
     * @param panel 实现了 [IPanel] 接口的更多面板实例。
     * @return KeyboardHelper 实例。
     */
    fun <P : IPanel> bindMorePanel(panel: P): KeyboardHelper {
        this.morePanel = panel
        morePanelHeight = panel.getPanelHeight() // 获取并记录更多面板的高度
        (panel as? ViewGroup)?.visibility = View.GONE // 初始时隐藏
        return this
    }

    /**
     * 设置在面板切换时是否需要滚动 RecyclerView。
     *
     * @param scrollBodyLayout true 表示需要滚动，false 表示不需要。
     * @return KeyboardHelper 实例。
     */
    fun setScrollBodyLayout(scrollBodyLayout: Boolean): KeyboardHelper {
        this.scrollBodyLayout = scrollBodyLayout
        return this
    }

    /**
     * 处理面板切换时的平移动画。
     *
     * @param panelType 目标面板类型。
     * @param lastPanelType 上一个面板类型。
     * @param fromValue 动画起始平移值。
     * @param toValue 动画结束平移值。
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun handlePanelMoveAnimator(
        panelType: PanelType,
        lastPanelType: PanelType,
        fromValue: Float,
        toValue: Float
    ) {
        Log.d(
            "KeyboardHelper",
            "handlePanelMoveAnimator:" +
                    " panelType = $panelType, lastPanelType = $lastPanelType, from = $fromValue, to = $toValue"
        )

        // 创建 RecyclerView 和输入面板的平移动画
        val recyclerViewTranslationYAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(recyclerView, "translationY", fromValue, toValue)
        val inputPanelTranslationYAnimator: ObjectAnimator =
            ObjectAnimator.ofFloat(inputPanel as? View, "translationY", fromValue, toValue)

        var panelTranslationYAnimator: ObjectAnimator? = null // 目标面板（表情/更多）的平移动画

        // 根据目标面板类型，重置其他面板状态并创建目标面板的动画
        when (panelType) {
            PanelType.INPUT_MOTHOD, PanelType.VOICE -> { // 切换到输入法或语音
                expressionPanel?.reset() // 隐藏表情面板
                morePanel?.reset() // 隐藏更多面板
            }

            PanelType.EXPRESSION -> { // 切换到表情面板
                morePanel?.reset() // 隐藏更多面板
                panelTranslationYAnimator = ObjectAnimator.ofFloat(
                    expressionPanel as? View,
                    "translationY",
                    fromValue,
                    toValue
                )
            }

            PanelType.MORE -> { // 切换到更多面板
                expressionPanel?.reset() // 隐藏表情面板
                panelTranslationYAnimator =
                    ObjectAnimator.ofFloat(morePanel as? View, "translationY", fromValue, toValue)
            }

            PanelType.NONE -> { // 隐藏所有特殊面板
                expressionPanel?.reset()
                morePanel?.reset()
            }
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = 250
        animatorSet.interpolator = DecelerateInterpolator()

        val animators = mutableListOf<Animator>()
        animators.add(inputPanelTranslationYAnimator)
        if (scrollBodyLayout && recyclerView != null) {
            animators.add(recyclerViewTranslationYAnimator)
        }
        panelTranslationYAnimator?.let { animators.add(it) }

        animatorSet.playTogether(animators)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // 动画结束后，请求重新布局以确保视图位置正确
                recyclerView?.requestLayout()
                (expressionPanel as? ViewGroup)?.requestLayout()
                (morePanel as? ViewGroup)?.requestLayout()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()
    }

    private var onKeyboardStateListener: OnKeyboardStateListener? = null

    /**
     * 设置外部的键盘状态监听器。
     *
     * @param listener 监听器实例。
     * @return KeyboardHelper 实例。
     */
    fun setOnKeyboardStateListener(listener: OnKeyboardStateListener?): KeyboardHelper {
        this.onKeyboardStateListener = listener
        return this
    }

    /**
     * 外部键盘状态监听器接口定义。
     * 与 [KeyboardStatePopupWindow.OnKeyboardStateListener] 结构相同，用于将事件传递给外部。
     */
    interface OnKeyboardStateListener {
        fun onOpened(keyboardHeight: Int)
        fun onClosed()
    }
}