package com.chatapp.adapter

import android.content.Context 
import android.graphics.drawable.Drawable 
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat 
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chatapp.databinding.ItemMessageReceivedBinding
import com.chatapp.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import com.chatapp.R
import com.chatapp.model.locadata.ExpressionSource
import com.chatapp.model.entity.Message
import java.util.regex.Pattern 

/**
 * 消息适配器
 */
class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == "me") {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }

        // 调整消息间距
        val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
        if (position > 0) {
            val previousMessage = getItem(position - 1)
            if (message.sender == previousMessage.sender && !message.showTimestamp) {
                layoutParams.topMargin = 0
            } else {
                layoutParams.topMargin = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.message_group_top_margin)
            }
        } else {
            layoutParams.topMargin = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.message_group_top_margin)
        }
        holder.itemView.layoutParams = layoutParams
    }

    private val expressionPattern = Pattern.compile("\\\$ne#(\\d+)\\^")

    private fun spannableStringWithExpressions(context: Context,
                                               content: String,
                                               textView: TextView): SpannableString {
        val spannableString = SpannableString(content)
        val matcher = expressionPattern.matcher(content)
        val expressionSource = ExpressionSource.instance

        while (matcher.find()) {
            val uniqueTag = matcher.group(0)
            uniqueTag?.let {
                expressionSource.getExpressionByUnique(it)?.let { expression ->
                    val drawable: Drawable? = ContextCompat.getDrawable(context, expression.resId)
                    drawable?.setBounds(0, 0, (textView.lineHeight * 1.2).toInt(), (textView.lineHeight * 1.2).toInt())
                    val imageSpan = ImageSpan(drawable!!, ImageSpan.ALIGN_BOTTOM)
                    spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return spannableString
    }

    inner class SentMessageViewHolder(
        private val binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            if (message.content != null) {
                binding.textMessageBody.text = spannableStringWithExpressions(itemView.context, message.content, binding.textMessageBody)
                binding.textMessageBody.visibility = ViewGroup.VISIBLE
                binding.imageMessageBody.visibility = ViewGroup.GONE
            } else if (message.imageResId != null) {
                binding.imageMessageBody.setImageResource(message.imageResId)
                binding.imageMessageBody.visibility = ViewGroup.VISIBLE
                binding.textMessageBody.visibility = ViewGroup.GONE
            } else {
                binding.textMessageBody.visibility = ViewGroup.GONE
                binding.imageMessageBody.visibility = ViewGroup.GONE
            }

            binding.textMessageTime.text = timeFormat.format(Date(message.timestamp))
            binding.textMessageTime.visibility = if (message.showTimestamp) ViewGroup.VISIBLE else ViewGroup.GONE
            binding.imageMessageProfile.visibility = if (message.showSenderInfo) ViewGroup.VISIBLE else ViewGroup.INVISIBLE
        }
    }

    inner class ReceivedMessageViewHolder(
        private val binding: ItemMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(message: Message) {
            if (message.content != null) {
                binding.textMessageBody.text = spannableStringWithExpressions(itemView.context, message.content, binding.textMessageBody)
                binding.textMessageBody.visibility = ViewGroup.VISIBLE
                binding.imageMessageBody.visibility = ViewGroup.GONE
            } else if (message.imageResId != null) {
                binding.imageMessageBody.setImageResource(message.imageResId)
                binding.imageMessageBody.visibility = ViewGroup.VISIBLE
                binding.textMessageBody.visibility = ViewGroup.GONE
            } else {
                binding.textMessageBody.visibility = ViewGroup.GONE
                binding.imageMessageBody.visibility = ViewGroup.GONE
            }

            binding.textMessageTime.text = timeFormat.format(Date(message.timestamp))
            binding.textMessageTime.visibility = if (message.showTimestamp) ViewGroup.VISIBLE else ViewGroup.GONE
            binding.imageMessageProfile.visibility = if (message.showSenderInfo) ViewGroup.VISIBLE else ViewGroup.INVISIBLE
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.timestamp == newItem.timestamp && oldItem.sender == newItem.sender
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}