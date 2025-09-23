package com.example.lifemaster.presentation.community.view

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityFreeBinding? = null
    private val binding get() = _binding!!

    private val vm: CommunityViewModel by activityViewModels()
    private lateinit var adapter: CommunityAdapter

    private var bestRecyclerView: RecyclerView? = null
    private var bestAdapter: CommunityAdapter? = null
    private var bestContainer: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCommunityFreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        adapter = CommunityAdapter { item -> onClickItem(item) }
        binding.recyclerviewBoard.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewBoard.adapter = adapter
        vm.items.observe(viewLifecycleOwner) { list -> adapter.submitList(list) }

        val auth = readAuthToken()
        if (auth != null) {
            vm.fetchFreePosts(auth) { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
        }

        bestRecyclerView = binding.root.findViewById(R.id.rv_best)
        bestContainer    = binding.root.findViewById(R.id.groupBest)

        bestRecyclerView?.let { rv ->
            bestAdapter = CommunityAdapter { item -> onClickItem(item) }.also { rv.adapter = it }
            rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rv.isNestedScrollingEnabled = false
            rv.setHasFixedSize(false)

            val spacing = dp(8)
            rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    val pos = parent.getChildAdapterPosition(view)
                    val last = (parent.adapter?.itemCount ?: 0) - 1
                    outRect.set(0, 0, 0, if (pos != last) spacing else 0)
                }
            })

            if (auth != null) {
                vm.fetchPopularPosts(token = auth) { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                bestContainer?.visibility = View.GONE
            }

            vm.bestItems.observe(viewLifecycleOwner) { best ->
                val topTwo = (best ?: emptyList()).take(2) // 인기글 2개만
                bestAdapter?.submitList(topTwo)
                bestContainer?.visibility = if (topTwo.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // 글쓰기 이동
        binding.communityWriteAdd.setOnClickListener {
            findNavController().navigate(R.id.communityWriteFragment)
        }

        // 개선 게시판 이동
        binding.root.findViewById<View>(R.id.tv_improve_board)?.setOnClickListener {
            findNavController().navigate(R.id.communityImproveFragment)
        }

        // 글 작성/수정 후 새로고침
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refresh_posts")
            ?.observe(viewLifecycleOwner) { need ->
                if (need == true) {
                    val a = readAuthToken()
                    if (a != null) {
                        vm.fetchFreePosts(a) { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
                        vm.fetchPopularPosts(a) { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
    }

    private fun onClickItem(item: CommunityItem) {
        val b = Bundle().apply {
            putString(CommunityPostFragment.ARG_ITEM_ID, item.id)
        }
        findNavController().navigate(R.id.communityPostFragment, b)
    }

    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bestAdapter = null
        bestRecyclerView = null
        bestContainer = null
    }
}