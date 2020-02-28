package com.chuckerteam.chucker.internal.ui.error

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentErrorListBinding
import com.chuckerteam.chucker.internal.ui.MainViewModel

internal class ErrorListFragment : Fragment(), ErrorAdapter.ErrorClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var errorsBinding: ChuckerFragmentErrorListBinding
    private lateinit var errorsAdapter: ErrorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        errorsBinding = ChuckerFragmentErrorListBinding.inflate(inflater, container, false)

        errorsAdapter = ErrorAdapter(this)

        with(errorsBinding) {
            tutorialLink.movementMethod = LinkMovementMethod.getInstance()
            errorsRecyclerView.apply {
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = errorsAdapter
            }
        }

        return errorsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.errors.observe(
            viewLifecycleOwner,
            Observer { errors ->
                errorsAdapter.setData(errors)
                errorsBinding.tutorialView.visibility = if (errors.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chucker_errors_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.clear) {
            askForConfirmation()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun askForConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.chucker_clear)
            .setMessage(R.string.chucker_clear_error_confirmation)
            .setPositiveButton(R.string.chucker_clear) { _, _ ->
                viewModel.clearErrors()
            }
            .setNegativeButton(R.string.chucker_cancel, null)
            .show()
    }

    companion object {
        fun newInstance() = ErrorListFragment()
    }

    override fun onErrorClick(throwableId: Long, position: Int) {
        ErrorActivity.start(requireActivity(), throwableId)
    }
}
