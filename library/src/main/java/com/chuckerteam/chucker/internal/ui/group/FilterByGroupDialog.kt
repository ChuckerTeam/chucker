package com.chuckerteam.chucker.internal.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chuckerteam.chucker.databinding.ChuckerFilterByGroupDialogBinding
import com.chuckerteam.chucker.internal.support.GroupSingleton
import com.chuckerteam.chucker.internal.ui.MainViewModel
import com.chuckerteam.chucker.internal.ui.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class FilterByGroupDialog : BottomSheetDialogFragment() {

    private lateinit var binding: ChuckerFilterByGroupDialogBinding
    private lateinit var groupAdapter: GroupAdapter

    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChuckerFilterByGroupDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        with(binding) {
            this.groupRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
                adapter = groupAdapter
            }
        }
    }

    private fun setupAdapter() {
        groupAdapter = GroupAdapter {
            it.isChecked = !it.isChecked
            if (it.isChecked) {
                viewModel.addGroup(it)
            } else {
                viewModel.removeGroup(it)
            }
        }
        groupAdapter.submitList(GroupSingleton.groups)
    }

    companion object {
        fun newInstance() = FilterByGroupDialog()
    }
}
