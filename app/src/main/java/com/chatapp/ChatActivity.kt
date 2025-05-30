package com.chatapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.chatapp.databinding.ActivityChatBinding
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 使用 WindowInsetsCompat 处理系统栏和键盘内边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.updatePadding(
                left = systemBarsInsets.left,
                right = systemBarsInsets.right,
                top = systemBarsInsets.top
            )


            val inputLayoutHeight = binding.inputLayout.height
            binding.recyclerView.updatePadding(
                bottom = imeInsets.bottom + inputLayoutHeight
            )

            WindowInsetsCompat.CONSUMED
        }

        setupToolbar()
        setupRecyclerView()
        setupInputArea()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false) // Hide default title
        }
        binding.centeredToolbarTitle.text = "国志"
        // Set status bar color (using toolbar background color)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.toolbar_background_color)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // Use this flag to make status bar icons dark for light backgrounds
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupInputArea() {
        binding.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.plusButton.visibility = if (s.isNullOrEmpty()) View.VISIBLE else View.GONE
                binding.sendButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                // 切换输入模式下的加号按钮可见性
                binding.plusButton.visibility = if (s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.emojiButton.setOnClickListener {
            Toast.makeText(this, "表情按钮点击", Toast.LENGTH_SHORT).show()
        }

        binding.plusButton.setOnClickListener {
            Toast.makeText(this, "加号按钮点击", Toast.LENGTH_SHORT).show()
        }


        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        binding.recyclerView.setOnTouchListener { v, event ->
            v.clearFocus()
            false
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            viewModel.sendMessage(messageText)
            binding.messageInput.text.clear()
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            messageAdapter.submitList(messages)
            binding.recyclerView.post {
                binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.action_more_options -> {
                Toast.makeText(this, "更多选项点击", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
} 