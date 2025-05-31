package com.chatapp.panel

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.chatapp.R
import com.chatapp.databinding.LayoutInputPanelBinding
import com.chatapp.utils.UIUtil
import com.chatapp.utils.DensityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 聊天输入面板视图。
 * 包含文本输入框、发送按钮、以及切换到语音、表情、更多功能面板的按钮。
 * 管理这些面板之间的切换逻辑和动画。
 */
class CInputPanel : LinearLayout, IInputPanel {

    private var binding: LayoutInputPanelBinding
    private var panelType = PanelType.NONE // 当前显示的面板类型
    private var lastPanelType = panelType // 上一次显示的面板类型
    private var isKeyboardOpened = false // 软键盘是否打开的标记

    // 发送消息的高阶函数回调
    private var onSendMessage: ((String) -> Unit)? = null

    companion object {
        const val TAG = "CInputPanel"
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        // 使用 ViewBinding 来加载布局
        binding = LayoutInputPanelBinding.inflate(LayoutInflater.from(context), this)
        init()
        setListeners()
    }

    private var isActive = false // 面板是否处于活动状态（例如，正在进行切换动画）

    /**
     * 初始化输入面板的基本属性和视图。
     */

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        orientation = HORIZONTAL
        setPadding(
            DensityUtil.dp2px(context, 10.0f),
            DensityUtil.dp2px(context, 6.0f),
            DensityUtil.dp2px(context, 10.0f),
            DensityUtil.dp2px(context, 6.0f)
        )
        gravity = Gravity.BOTTOM
        setBackgroundColor(ContextCompat.getColor(context, R.color.c_cbcbcb))
        // 初始时禁
        binding.etContent.inputType = InputType.TYPE_NULL
        // 为输入框设置触摸监听，用于在非键盘状态下点击时弹出键盘
        binding.etContent.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (!isKeyboardOpened) {// 如果键盘未打开
                    MainScope().launch(Dispatchers.Main) {
                        delay(100)
                        // 请求焦点
                        UIUtil.requestFocus(binding.etContent)
                        // 显示软键盘
                        UIUtil.showSoftInput(context, binding.etContent)
                    }
                    // 恢复输入框的正常输入类型
                    binding.etContent.resetInputType()
                    // 重置表情按钮图标为默认状态
                    binding.btnExpression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
                    binding.btnExpression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
                    // 处理切换到输入法面板的动画
                    handleAnimator(PanelType.INPUT_MOTHOD)
                    // 通知监听器显示输入法面板
                    onInputPanelStateChangedListener?.onShowInputMethodPanel()
                }
                return@setOnTouchListener true
            }
            false
        }
    }

    /**
     * 设置发送消息的监听器。
     * @param listener 当用户发送消息时调用的回调函数，参数为消息文本。
     */
    fun setOnSendMessageListener(listener: (String) -> Unit) {
        this.onSendMessage = listener
    }

    /**
     * 为输入面板中的各个按钮设置点击监听器。
     */
    private fun setListeners() {
        // 语音按钮点击事件
        binding.btnVoice.setOnClickListener {
            binding.btnExpression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
            binding.btnExpression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
            if (lastPanelType == PanelType.VOICE) { // 如果当前已是语音面板，则切换回输入法
                binding.btnVoicePressed.visibility = GONE
                binding.etContent.visibility = VISIBLE
                UIUtil.requestFocus(binding.etContent)
                UIUtil.showSoftInput(context, binding.etContent)
                handleAnimator(PanelType.INPUT_MOTHOD)
                binding.etContent.resetInputType()
            } else { // 否则，切换到语音面板
                binding.btnVoicePressed.visibility = VISIBLE
                binding.etContent.visibility = GONE
                UIUtil.loseFocus(binding.etContent)
                UIUtil.hideSoftInput(context, binding.etContent)
                handleAnimator(PanelType.VOICE)
                onInputPanelStateChangedListener?.onShowVoicePanel()
            }
        }
        // 表情按钮点击事件
        binding.btnExpression.setOnClickListener {
            binding.btnVoicePressed.visibility = GONE
            binding.etContent.visibility = VISIBLE
            if (lastPanelType == PanelType.EXPRESSION) {// 如果当前已是表情面板，则切换回输入法
                binding.btnExpression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
                binding.btnExpression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
                UIUtil.requestFocus(binding.etContent)
                UIUtil.showSoftInput(context, binding.etContent)
                handleAnimator(PanelType.INPUT_MOTHOD)
                binding.etContent.resetInputType()
            } else {
                binding.btnExpression.setNormalImageResId(R.drawable.ic_chat_keyboard_normal)
                binding.btnExpression.setPressedImageResId(R.drawable.ic_chat_keyboard_pressed)
                UIUtil.loseFocus(binding.etContent)
                UIUtil.hideSoftInput(context, binding.etContent)
                handleAnimator(PanelType.EXPRESSION)
                onInputPanelStateChangedListener?.onShowExpressionPanel()
            }
        }
        // 更多按钮点击事件
        binding.btnMore.setOnClickListener {
            binding.btnExpression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
            binding.btnExpression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
            binding.btnVoicePressed.visibility = GONE
            binding.etContent.visibility = VISIBLE
            if (lastPanelType == PanelType.MORE) {// 如果当前已是更多面板，则切换回输入法
                UIUtil.requestFocus(binding.etContent)
                UIUtil.showSoftInput(context, binding.etContent)
                handleAnimator(PanelType.INPUT_MOTHOD)
                binding.etContent.resetInputType()
            } else {
                UIUtil.loseFocus(binding.etContent)
                UIUtil.hideSoftInput(context, binding.etContent)
                handleAnimator(PanelType.MORE)
                onInputPanelStateChangedListener?.onShowMorePanel()
            }
        }
        // 输入框编辑器操作监听（例如发送按钮）
        binding.etContent.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val messageText = binding.etContent.text.toString().trim()
                if (messageText.isNotEmpty()) {
                    onSendMessage?.invoke(messageText)
                    binding.etContent.setText("") // 清空输入框
                } else {
                    Toast.makeText(context, "消息不能为空", Toast.LENGTH_SHORT).show()
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }


    /**
     * 处理面板切换时的动画逻辑。
     *
     * @param panelType 要切换到的目标面板类型。
     */
    private fun handleAnimator(panelType: PanelType) {
        Log.d(TAG, "lastPanelType = $lastPanelType\tpanelType = $panelType")
        if (lastPanelType == panelType) {
            // 如果目标类型与当前类型相同（且非输入法），则不作处理
            // 若重复点击输入法，则允许重新触发，以便键盘的显隐
            return
        }
        isActive = true // 标记面板为活动状态
        Log.d(TAG, "isActive = $isActive")
        this.panelType = panelType
        var fromValue = 0.0f // 动画起始值
        var toValue = 0.0f // 动画结束值

        // 根据上一个面板类型和目标面板类型，计算动画的起止平移值
        // 这里的逻辑是基于面板从底部向上平移进入或向下平移移出屏幕
        // 具体值依赖于 KeyboardHelper 中记录的各个面板的高度
        when (panelType) {
            // 语音面板
            PanelType.VOICE -> {
                when (lastPanelType) {
                    PanelType.INPUT_MOTHOD -> {
                        fromValue = -KeyboardHelper.inputPanelHeight.toFloat()
                        toValue = 0.0f
                    }

                    PanelType.EXPRESSION -> {
                        fromValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                        toValue = 0.0f
                    }

                    PanelType.MORE -> {
                        fromValue = -KeyboardHelper.morePanelHeight.toFloat()
                        toValue = 0.0f
                    }

                    PanelType.NONE -> {
                        fromValue = 0.0f
                        toValue = 0.0f
                    }

                    else -> {
                    }
                }
            }

            // 输入法面板 (软键盘)
            PanelType.INPUT_MOTHOD ->
                when (lastPanelType) {
                    PanelType.VOICE -> {
                        fromValue = 0.0f
                        toValue = -KeyboardHelper.inputPanelHeight.toFloat()
                    }

                    PanelType.EXPRESSION -> {
                        fromValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                        toValue = -KeyboardHelper.inputPanelHeight.toFloat()
                    }

                    PanelType.MORE -> {
                        fromValue = -KeyboardHelper.morePanelHeight.toFloat()
                        toValue = -KeyboardHelper.inputPanelHeight.toFloat()
                    }

                    PanelType.NONE -> {
                        fromValue = 0.0f
                        toValue = -KeyboardHelper.inputPanelHeight.toFloat()
                    }

                    else -> {
                    }
                }

            // 表情面板
            PanelType.EXPRESSION ->
                when (lastPanelType) {
                    PanelType.INPUT_MOTHOD -> {
                        fromValue = -KeyboardHelper.inputPanelHeight.toFloat()
                        toValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                    }

                    PanelType.VOICE -> {
                        fromValue = 0.0f
                        toValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                    }

                    PanelType.MORE -> {
                        fromValue = -KeyboardHelper.morePanelHeight.toFloat()
                        toValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                    }

                    PanelType.NONE -> {
                        fromValue = 0.0f
                        toValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                    }

                    else -> {
                    }
                }

            //更多面板
            PanelType.MORE ->
                when (lastPanelType) {
                    PanelType.INPUT_MOTHOD -> {
                        fromValue = -KeyboardHelper.inputPanelHeight.toFloat()
                        toValue = -KeyboardHelper.morePanelHeight.toFloat()
                    }

                    PanelType.VOICE -> {
                        fromValue = 0.0f
                        toValue = -KeyboardHelper.morePanelHeight.toFloat()
                    }

                    PanelType.EXPRESSION -> {
                        fromValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                        toValue = -KeyboardHelper.morePanelHeight.toFloat()
                    }

                    PanelType.NONE -> {
                        fromValue = 0.0f
                        toValue = -KeyboardHelper.morePanelHeight.toFloat()
                    }

                    else -> {
                    }
                }

            // 无特殊面板 (通常是隐藏所有面板)
            PanelType.NONE ->
                when (lastPanelType) {
                    PanelType.VOICE -> {
                        // from 0.0f to 0.0f
                    }

                    PanelType.INPUT_MOTHOD -> {
                        fromValue = -KeyboardHelper.inputPanelHeight.toFloat()
                        toValue = 0.0f
                    }

                    PanelType.EXPRESSION -> {
                        fromValue = -KeyboardHelper.expressionPanelHeight.toFloat()
                        toValue = 0.0f
                    }

                    PanelType.MORE -> {
                        fromValue = -KeyboardHelper.morePanelHeight.toFloat()
                        toValue = 0.0f
                    }

                    else -> {
                    }
                }
        }

        // 调用布局动画处理监听器，执行实际的动画
        onLayoutAnimatorHandleListener?.invoke(panelType, lastPanelType, fromValue, toValue)
        lastPanelType = panelType
    }

    private var onLayoutAnimatorHandleListener:
            ((panelType: PanelType, lastPanelType: PanelType, fromValue: Float, toValue: Float) -> Unit)? =
        null

    private var onInputPanelStateChangedListener: OnInputPanelStateChangedListener? = null


    override fun onSoftKeyboardOpened() {
        isKeyboardOpened = true
        binding.etContent.resetInputType()
    }

    override fun onSoftKeyboardClosed() {
        isKeyboardOpened = false
        binding.etContent.inputType = InputType.TYPE_NULL
        if (lastPanelType == PanelType.INPUT_MOTHOD) {
            UIUtil.loseFocus(binding.etContent)
            UIUtil.hideSoftInput(context, binding.etContent)
            handleAnimator(PanelType.NONE)
        }
    }

    override fun setOnLayoutAnimatorHandleListener(
        listener: ((
            panelType: PanelType,
            lastPanelType: PanelType,
            fromValue: Float,
            toValue: Float
        ) -> Unit)?
    ) {
        this.onLayoutAnimatorHandleListener = listener
    }

    override fun setOnInputStateChangedListener(listener: OnInputPanelStateChangedListener?) {
        this.onInputPanelStateChangedListener = listener
    }

    override fun reset() {
        if (!isActive) {
            return
        }
        Log.d(TAG, "reset()")
        UIUtil.loseFocus(binding.etContent)
        UIUtil.hideSoftInput(context, binding.etContent)
        binding.btnExpression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
        binding.btnExpression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
        MainScope().launch(Dispatchers.Main) {
            delay(100)
            handleAnimator(PanelType.NONE)
        }
        isActive = false
    }

    override fun getPanelHeight(): Int {
        return KeyboardHelper.keyboardHeight
    }

    fun getInputText(): String {
        return binding.etContent.text.toString()
    }

    fun setInputText(text: String) {
        binding.etContent.setText(text)
    }

    fun setCursorToEnd() {
        binding.etContent.setSelection(binding.etContent.text.toString().length)
    }

    fun getInputEditText(): android.widget.EditText {
        return binding.etContent
    }
}