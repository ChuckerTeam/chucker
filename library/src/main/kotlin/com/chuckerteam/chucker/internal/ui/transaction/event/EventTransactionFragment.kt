package com.chuckerteam.chucker.internal.ui.transaction.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.chuckerteam.chucker.databinding.ChuckerFragmentTransactionEventBinding
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModel
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class EventTransactionFragment : Fragment() {
    private val sharedViewModel: TransactionViewModel by activityViewModels { TransactionViewModelFactory() }
    private val viewModel: EventTransactionViewModel by viewModels {
        EventTransactionViewModelFactory(
            sharedViewModel
        )
    }
    private lateinit var viewBinding: ChuckerFragmentTransactionEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = ChuckerFragmentTransactionEventBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            viewModel.transaction.collect {
                viewBinding.toolbarTitle.text = it?.title ?: "No Title"
                viewBinding.payloadText.text = it?.payload ?: "No Payload"
            }
        }
    }


}
