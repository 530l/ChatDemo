package com.chatapp.panel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatapp.App
import com.chatapp.R
import com.chatapp.adapter.ExpressionPagerListAdapter
import com.chatapp.adapter.ExpressionTypeListAdapter
import com.chatapp.databinding.LayoutExpressionPanelBinding
import com.chatapp.model.datas.ExpressionManager
import com.chatapp.model.entity.ExpressionType
import com.chatapp.utils.DensityUtil

/**
 * 表情面板视图。
 * 用于展示可供用户选择的表情列表，通常包含表情分类和表情分页显示。
 */
@Suppress("UNREACHABLE_CODE")
class CExpressionPanel : LinearLayout, IPanel {

    private var binding: LayoutExpressionPanelBinding
    private var expressionTypeList = arrayListOf<ExpressionType>()

    // 新增：表情选择的回调，传递给 Activity
    var onExpressionPicked: ((String) -> Unit)? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = LayoutExpressionPanelBinding.inflate(LayoutInflater.from(context), this)
        init()
    }

    /**
     * 当视图的可见性发生变化时调用。
     * 用于在面板显示时根据键盘高度调整自身高度。
     */
    override fun onVisibilityChanged(
        changedView: View,
        visibility: Int
    ) {
        super.onVisibilityChanged(changedView, visibility)
        val layoutParams = layoutParams
        layoutParams.width = LayoutParams.MATCH_PARENT
        layoutParams.height = getPanelHeight() // 设置面板高度
        setLayoutParams(layoutParams)
    }

    private fun init() {
        orientation = VERTICAL
        initData()
        initRecyclerView()
        initViewPager()
    }

    /**
     * 初始化表情数据。
     * 目前只添加了普通表情类型。
     */
    private fun initData() {
        expressionTypeList.add(
            ExpressionType(
                R.drawable.ic_expression_panel_tab_normal, // 表情类型图标
                ExpressionManager.instance.getNormalExpressionList() // 获取普通表情列表
            )
        )
    }

    /**
     * 初始化表情类型选择的 RecyclerView。
     * 用于横向展示不同的表情包 Tab。
     */
    private fun initRecyclerView() {
        val expressionTypeListAdapter =
            ExpressionTypeListAdapter(
                context,
                expressionTypeList
            )
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = expressionTypeListAdapter
    }

    /**
     * 初始化表情内容展示的 ViewPager2。
     * 用于分页展示选定表情类型下的所有表情。
     */
    private fun initViewPager() {
        val expressionPagerListAdapter =
            ExpressionPagerListAdapter(
                context as AppCompatActivity, // 需要 AppCompatActivity 来管理 FragmentStateAdapter
                expressionTypeList
            )
        // 设置 ViewPager Adapter 的回调
        expressionPagerListAdapter.onExpressionSelectedInPanel = {
            onExpressionPicked?.invoke(it) // 将 unique 值通过 CExpressionPanel 的回调传出
        }
        binding.viewPager.adapter = expressionPagerListAdapter
        binding.viewPager.isUserInputEnabled = true // 允许用户滑动切换页面
    }

    // 用于延迟隐藏面板的 Runnable
    private val mExpressionPanelInvisibleRunnable =
        Runnable { visibility = GONE }

    /**
     * 重置表情面板状态，使其隐藏。
     */
    override fun reset() {
        postDelayed(mExpressionPanelInvisibleRunnable, 0)
    }

    /**
     * 获取表情面板的期望高度。
     * 高度通常基于已记录的键盘高度进行计算，并可能加上一些额外的固定高度（如表情类型Tab栏的高度）。
     *
     * @return 面板的期望高度（像素值）。
     */
    override fun getPanelHeight(): Int {
        val keyboardHeight =
            if (App.instance.keyboardHeight == 0)
                DensityUtil.getScreenHeight(context) / 5 * 2
            else
                App.instance.keyboardHeight
        return keyboardHeight + DensityUtil.dp2px(context, 36.0f) // 键盘高度 + Tab栏高度
    }
}