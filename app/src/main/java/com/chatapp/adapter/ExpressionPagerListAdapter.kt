package com.chatapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chatapp.model.entity.ExpressionType
import com.chatapp.panel.NormalExpressionPagerFragment


class ExpressionPagerListAdapter(activity: FragmentActivity, private var expressionTypeList: ArrayList<ExpressionType>) : FragmentStateAdapter(activity) {

    // 表情选择的回调
    var onExpressionSelectedInPanel: ((String) -> Unit)? = null

    override fun getItemCount(): Int {
        return if (expressionTypeList.isEmpty()) 0 else expressionTypeList.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                val fragment = NormalExpressionPagerFragment.newInstance(
                    expressionTypeList[position].expressionList
                )
                // 将 CExpressionPanel 的回调设置给 Fragment
                fragment.onExpressionSelected = {
                    onExpressionSelectedInPanel?.invoke(it)
                }
                return fragment
            }
        }

        return Fragment()
    }
}