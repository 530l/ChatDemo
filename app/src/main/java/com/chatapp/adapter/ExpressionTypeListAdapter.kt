package com.chatapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.chatapp.R
import com.chatapp.model.entity.ExpressionType

/**
 * 表情类型列表适配器。
 */
class ExpressionTypeListAdapter(private val context: Context, private var expressionTypeList: ArrayList<ExpressionType>)
    : RecyclerView.Adapter<ExpressionTypeListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_expression_type, parent, false))
    }

    override fun getItemCount(): Int {
        return if (expressionTypeList.isNullOrEmpty()) 0 else expressionTypeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.expressionIconImageView.setImageResource(expressionTypeList[position].resId)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expressionIconImageView: ImageView = itemView.findViewById(R.id.iv_expression_icon)
    }
}