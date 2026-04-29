package com.example.lifemaster.presentation.community.view

import android.view.Gravity
import android.os.Build
import androidx.core.widget.PopupWindowCompat
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentCommunityFreeBinding
import com.example.lifemaster.presentation.community.adapter.CommunityAdapter
import com.example.lifemaster.presentation.community.model.CommunityItem
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel
import com.example.lifemaster.presentation.community.viewmodel.SortMode
import kotlin.math.roundToInt

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityFreeBinding? = null
    private val binding get() = _binding!!

    private val vm: CommunityViewModel by activityViewModels()

    private lateinit var mainAdapter: CommunityAdapter
    private var bestAdapter: CommunityAdapter? = null
    private var bestRecyclerView: RecyclerView? = null
    private var bestContainer: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityFreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        mainAdapter = CommunityAdapter { onClickItem(it) }
        binding.recyclerviewBoard.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewBoard.adapter = mainAdapter
        vm.items.observe(viewLifecycleOwner) { mainAdapter.submitList(it) }

        bestContainer = binding.root.findViewById(R.id.groupBest)
        bestRecyclerView = binding.root.findViewById(R.id.rv_best)
        bestRecyclerView?.let { rv ->
            bestAdapter = CommunityAdapter { onClickItem(it) }.also { rv.adapter = it }
            rv.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv.isNestedScrollingEnabled = false
            rv.setHasFixedSize(false)
            val spacing = dp(8)
            rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val pos = parent.getChildAdapterPosition(view)
                    val last = (parent.adapter?.itemCount ?: 0) - 1
                    outRect.set(0, 0, 0, if (pos != last) spacing else 0)
                }
            })
            vm.bestItems.observe(viewLifecycleOwner) { list ->
                val topTwo = (list ?: emptyList()).take(2)
                bestAdapter?.submitList(topTwo)
                bestContainer?.visibility = if (topTwo.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        val sortContainer = binding.root.findViewById<View>(R.id.btn_sort_toggle)
        val sortLabel = binding.root.findViewById<TextView>(R.id.tv_sort_label)
        val sortArrow = binding.root.findViewById<View>(R.id.btn_sort_arrow)

        val openSort: (View) -> Unit = { showSortMenu(it) }
        sortContainer?.setOnClickListener(openSort)
        sortLabel?.setOnClickListener(openSort)
        sortArrow?.setOnClickListener(openSort)

        vm.sortMode.observe(viewLifecycleOwner) { mode ->
            sortLabel?.text = if (mode == SortMode.LATEST) "최신 순" else "좋아요 순"
        }

        val token = readAuthToken()
        if (token != null) {
            vm.setSortAndRefresh(token, vm.sortMode.value ?: SortMode.LATEST) { toast(it) } // 전체글(정렬은 프론트)
            vm.fetchPopularPosts(token) { toast(it) }                                        // Best 영역만
        }

        binding.communityWriteAdd.setOnClickListener {
            findNavController().navigate(R.id.communityWriteFragment)
        }
        binding.root.findViewById<View>(R.id.tv_improve_board)?.setOnClickListener {
            findNavController().navigate(R.id.communityImproveFragment)
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refresh_posts")
            ?.observe(viewLifecycleOwner) { need ->
                if (need == true) {
                    val a = readAuthToken()
                    if (a != null) {
                        vm.setSortAndRefresh(a, vm.sortMode.value ?: SortMode.LATEST) { toast(it) }
                        vm.fetchPopularPosts(a) { toast(it) }
                    }
                }
            }
    }

    private fun showSortMenu(anchor: View) {
        val content = layoutInflater.inflate(R.layout.dialog_community_sort, null)

        val btnLatest = content.findViewById<TextView>(R.id.btn_sort_latest)
        val btnLike   = content.findViewById<TextView>(R.id.btn_sort_like)
        val chkLatest = content.findViewById<ImageView>(R.id.iv_check_latest)
        val chkLike   = content.findViewById<ImageView>(R.id.iv_check_like)

        // 체크 표시
        when (vm.sortMode.value ?: SortMode.LATEST) {
            SortMode.LATEST -> { chkLatest.visibility = View.VISIBLE;  chkLike.visibility = View.INVISIBLE }
            SortMode.LIKES  -> { chkLatest.visibility = View.INVISIBLE; chkLike.visibility = View.VISIBLE  }
        }

        val popup = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            elevation = dp(6).toFloat()
        }

        fun apply(mode: SortMode) {
            val token = readAuthToken() ?: return
            vm.setSortAndRefresh(token, mode) { toast(it) }
            popup.dismiss()
        }

        content.findViewById<View?>(R.id.row_latest)?.setOnClickListener { apply(SortMode.LATEST) }
        content.findViewById<View?>(R.id.row_like)?.setOnClickListener   { apply(SortMode.LIKES)  }
        btnLatest.setOnClickListener { apply(SortMode.LATEST) }
        btnLike.setOnClickListener   { apply(SortMode.LIKES)  }

        val arrow   = binding.root.findViewById<View>(R.id.btn_sort_arrow) ?: anchor
        val xAdjust = -dp(20)
        val yOff    = dp(10)

        PopupWindowCompat.showAsDropDown(popup, arrow, xAdjust, yOff, Gravity.END)
    }

    private fun onClickItem(item: CommunityItem) {
        val b = Bundle().apply {
            putString(CommunityPostFragment.ARG_ITEM_ID, item.id)
        }
        findNavController().navigate(R.id.communityPostFragment, b)
    }

    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0)
            .getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    private fun toast(msg: String?) {
        if (!msg.isNullOrBlank())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bestAdapter = null
        bestRecyclerView = null
        bestContainer = null
    }
}