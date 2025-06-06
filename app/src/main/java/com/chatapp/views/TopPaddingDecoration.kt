package com.chatapp.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class TopPaddingDecoration(private val padding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position =
            (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        val layoutManager = parent.layoutManager
        if(layoutManager is GridLayoutManager) {
            val spanCount = layoutManager.spanCount
            val itemCount = layoutManager.itemCount
            val startPos = 0
            val endPos = if(itemCount >= spanCount) {
                spanCount
            }else {
                itemCount
            }
            if(position in startPos until endPos) {
                outRect.set(0, padding, 0, 0)
            }
        }else if(layoutManager is LinearLayoutManager) {
            if (position == 0) {
                outRect.set(0, padding, 0, 0)
            }
        }
    }
}