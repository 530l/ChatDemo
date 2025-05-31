package com.chatapp.panel

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatapp.adapter.ExpressionListAdapter
import com.chatapp.databinding.FragmentNormalExpressionPagerBinding
import com.chatapp.model.locadata.ExpressionSource
import com.chatapp.model.entity.Expression
import com.chatapp.views.TopPaddingDecoration
import com.chatapp.utils.DensityUtil
import java.io.Serializable

/**
 * 表情面板 vp 页面
 */
@Suppress("UNCHECKED_CAST")
class NormalExpressionPagerFragment : Fragment() {

    private var _binding: FragmentNormalExpressionPagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var layoutManager: GridLayoutManager

    // 表情点击的回调
    var onExpressionSelected: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "NormalExpressionPagerFragment"
        private const val KEY_EXPRESSION_LIST = "key_expression_list"

        fun newInstance(expressionList: ArrayList<Expression>): NormalExpressionPagerFragment {
            val args = Bundle()
            args.putSerializable(
                KEY_EXPRESSION_LIST,
                expressionList as Serializable?
            )
            val fragment = NormalExpressionPagerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var expressionList: ArrayList<Expression>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNormalExpressionPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
    }

    private fun init() {
        val bundle = requireArguments()
        expressionList = bundle.getSerializable(KEY_EXPRESSION_LIST) as ArrayList<Expression>
        binding.recyclerView.setHasFixedSize(true)
        layoutManager = GridLayoutManager(activity, ExpressionSource.Companion.NORMAL_COUNT_BY_ROW)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(
            TopPaddingDecoration(
                DensityUtil.dp2px(requireContext(), 24.0f)
            )
        )
        val adapter =
            ExpressionListAdapter(
                requireActivity(),
                expressionList!!
            )
        // 设置 Adapter 的回调
        adapter.onExpressionClick = {
            onExpressionSelected?.invoke(it) // 将 unique 值通过 Fragment 的回调传出
        }
        binding.recyclerView.adapter = adapter
    }

    private var currentVisiblePercent = 0

    private fun setListeners() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("LongLogTag")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                try {
                    val spanCount: Int = layoutManager.spanCount
                    val lastVisibleItemPosition: Int = layoutManager.findLastVisibleItemPosition()
                    if ((lastVisibleItemPosition + 1) % spanCount == 0 || (lastVisibleItemPosition + 2) % spanCount == 0) {
                        var view: View?
                        if ((lastVisibleItemPosition + 1) % spanCount == 0) {
                            view = layoutManager.findViewByPosition(lastVisibleItemPosition)
                            view?.visibility = View.GONE
                        }
                        view = layoutManager.findViewByPosition(lastVisibleItemPosition - 1)
                        view?.visibility = View.GONE
                        view = layoutManager.findViewByPosition(lastVisibleItemPosition - spanCount)
                        view?.visibility = View.GONE
                        view = layoutManager.findViewByPosition(lastVisibleItemPosition - spanCount - 1)
                        view?.visibility = View.GONE
                    }
                    val lastCompletelyVisibleItemPosition: Int = layoutManager.findLastCompletelyVisibleItemPosition()
                    if ((lastCompletelyVisibleItemPosition + 1) % spanCount == 0 || (lastCompletelyVisibleItemPosition + 2) % spanCount == 0) {
                        var view: View?
                        if ((lastCompletelyVisibleItemPosition + 1) % spanCount == 0) {
                            view = layoutManager.findViewByPosition(lastVisibleItemPosition - spanCount * 2)
                            view?.visibility = View.VISIBLE
                        }
                        view = layoutManager.findViewByPosition(lastVisibleItemPosition - spanCount * 2 - 1)
                        view?.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}