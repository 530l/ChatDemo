package com.chatapp.model.entity

/**
 *  表情类型。
 * 包含表情类型的资源 ID 和表情列表。
 */
data class ExpressionType(var resId: Int, var expressionList: ArrayList<Expression>) {

    override fun toString(): String {
        return "ExpressionType(resId=$resId, expressionList=$expressionList)"
    }
}