package com.chatapp.model.entity

import java.io.Serializable


data class Expression(var resId: Int, var unique: String?) : Serializable {

    override fun toString(): String {
        return "Expression(resId=$resId, unique='$unique')"
    }
}