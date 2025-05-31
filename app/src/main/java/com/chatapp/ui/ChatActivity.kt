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
import com.chatapp.databinding.ActivityChatBinding
import com.chatapp.panel.KeyboardHelper
import com.chatapp.vm.ChatViewModel
import com.chatapp.utils.DensityUtil
import kotlin.getValue

/**
 * 聊天界面
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var keyboardHelper: KeyboardHelper

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
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
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }

        // 设置发送消息的回调
        binding.chatInputPanel.setOnSendMessageListener {
            viewModel.sendMessage(it)
        }

        // 设置表情选择的回调
        binding.expressionPanel.onExpressionPicked = {
            //todo 关于表情包可以再这里转移一下。后续优化
            val currentText = binding.chatInputPanel.getInputText()
            binding.chatInputPanel.setInputText(currentText + it)
            binding.chatInputPanel.setCursorToEnd()
        }

        // 初始化键盘监听器
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