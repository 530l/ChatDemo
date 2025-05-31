package com.chatapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatapp.App
import com.chatapp.R
import com.chatapp.adapter.MessageAdapter
import com.chatapp.databinding.ActivityChat2Binding
import com.chatapp.panel.KeyboardHelper
import com.chatapp.vm.ChatViewModel
import com.chatapp.utils.DensityUtil
import kotlin.getValue

class ChatActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityChat2Binding
    private lateinit var keyboardHelper: KeyboardHelper

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChat2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        observeViewModel()
        Log.d("ChatActivity2", "键盘高度：${App.Companion.instance.keyboardHeight}")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        binding.recyclerView.setHasFixedSize(true)
        messageAdapter = MessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity2)
            adapter = messageAdapter
        }

        // 设置发送消息的回调
        binding.chatInputPanel.setOnSendMessageListener {
            viewModel.sendMessage(it)
        }

        // 新增：设置 CExpressionPanel 的表情选择回调
        binding.expressionPanel.onExpressionPicked = {
            val currentText = binding.chatInputPanel.getInputText()
            binding.chatInputPanel.setInputText(currentText + it)
            // 可选：将光标移动到末尾
            binding.chatInputPanel.setCursorToEnd()
            // 可选：如果希望选择表情后直接显示键盘，可以取消下面这行注释
            // keyboardHelper.showSoftInput(binding.chatInputPanel.getInputEditText())
        }

        keyboardHelper = KeyboardHelper()
        keyboardHelper.init(this)
            .bindRootLayout(binding.layoutMain)
            .bindRecyclerView(binding.recyclerView)
            .bindInputPanel(binding.chatInputPanel)
            .bindExpressionPanel(binding.expressionPanel)
            .bindMorePanel(binding.morePanel)
            .setScrollBodyLayout(true)
            .setKeyboardHeight(
                if (App.Companion.instance.keyboardHeight == 0)
                    DensityUtil.getScreenHeight(applicationContext) / 5 * 2
                else
                    App.Companion.instance.keyboardHeight
            )
            .setOnKeyboardStateListener(object : KeyboardHelper.OnKeyboardStateListener {
                override fun onOpened(keyboardHeight: Int) {
                    App.Companion.instance.keyboardHeight = keyboardHeight
                }

                override fun onClosed() {
                }
            })


        scrollToBottom()
        binding.recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                keyboardHelper.reset()
            }
            false
        }
    }


    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            messageAdapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.recyclerView.post {
                        scrollToBottom()
                    }
                }
            }
        }
    }

    private fun scrollToBottom() {
        binding.recyclerView.adapter?.itemCount?.minus(1)
            ?.let { binding.recyclerView.scrollToPosition(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHelper.release()
    }


}