package com.chuckerteam.chucker.internal.ui.filter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentFilterDialogBinding

internal class FilterDialogFragment : DialogFragment() {

    private val viewModel: FilterViewModel by activityViewModels()

    private lateinit var binding: ChuckerFragmentFilterDialogBinding
    private lateinit var filterItemsAdapter: FilterItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireDialog().window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requireDialog().window?.requestFeature(Window.FEATURE_NO_TITLE)

        binding = ChuckerFragmentFilterDialogBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterItemsAdapter = FilterItemsAdapter(viewModel::requestTagAction).also {
            binding.requestTagRecyclerView.adapter = it
        }
        binding.buttonSelectAll.setOnClickListener {
            viewModel.selectAll()
        }
        binding.buttonFilterRequests.setOnClickListener {
            setFragmentResult(
                REQUEST_TAGS_REQUEST_KEY,
                bundleOf(KEY_REQUEST_TAGS to viewModel.getSelectedRequestTags())
            )
            dismiss()
        }

        viewModel.requestTagFilters.observe(viewLifecycleOwner) { requestTags ->
            filterItemsAdapter.submitList(requestTags)
        }
    }

    companion object {

        private const val REQUEST_TAGS_REQUEST_KEY = "chucker_request_tags"
        private const val KEY_REQUEST_TAGS = "chucker_key_request_tags"
        private const val TAG = "ChuckerFilterDialogFragment"

        fun show(
            fragmentManager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            onRequestTagFilterResult: (List<String?>) -> Unit
        ) {
            FilterDialogFragment().apply {
                setStyle(STYLE_NO_FRAME, R.style.Chucker_Theme_FilterDialog)
                fragmentManager.setFragmentResultListener(REQUEST_TAGS_REQUEST_KEY, lifecycleOwner) { _, bundle ->
                    onRequestTagFilterResult.invoke(bundle.getStringArray(KEY_REQUEST_TAGS)!!.toList())
                }
            }.show(fragmentManager, TAG)
        }
    }
}
