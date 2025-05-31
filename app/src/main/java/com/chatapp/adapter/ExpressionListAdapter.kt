package com.chatapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.chatapp.R
import com.chatapp.model.entity.Expression

/**
 * 表情列表适配器。
 */
class ExpressionListAdapter(private val context: Context, var expressionList: ArrayList<Expression>) : RecyclerView.Adapter<ExpressionListAdapter.ViewHolder>() {

    var onExpressionClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_expression, parent, false))
    }

    override fun getItemCount(): Int {
        return if (expressionList.isNullOrEmpty()) 0 else expressionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expression = expressionList[position]
        holder.expressionImageView.setImageResource(expression.resId)
        holder.itemView.setOnClickListener {
            expression.unique?.let {
                onExpressionClick?.invoke(it)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expressionImageView: ImageView = itemView.findViewById(R.id.iv_expression)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}