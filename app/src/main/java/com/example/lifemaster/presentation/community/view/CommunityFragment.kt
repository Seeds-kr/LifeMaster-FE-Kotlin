package com.example.lifemaster.presentation.community.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.community.model.CommunityItem
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel

class CommunityFragment : Fragment() {

    private val vm: CommunityViewModel by activityViewModels()
    private lateinit var adapter: CommunityAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_community_free, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerview_board)
        adapter = CommunityAdapter { item: CommunityItem ->
            val args = Bundle().apply {
                putString(CommunityPostFragment.ARG_ITEM_ID, item.id)
                putBoolean(CommunityPostFragment.ARG_SHARE_CALENDAR, false)
            }
            findNavController().navigate(R.id.communityPostFragment, args)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        rv.setHasFixedSize(false)

        vm.items.observe(viewLifecycleOwner) { list -> adapter.submitList(list) }

        view.findViewById<ImageView>(R.id.community_write_add).setOnClickListener {
            findNavController().navigate(R.id.action_communityFragment_to_communityWriteFragment)
        }
    }
}