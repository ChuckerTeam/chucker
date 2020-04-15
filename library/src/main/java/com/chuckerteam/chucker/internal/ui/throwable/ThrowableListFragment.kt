package com.chuckerteam.chucker.internal.ui.throwable

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerFragmentThrowableListBinding
import com.chuckerteam.chucker.internal.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal class ThrowableListFragment : Fragment(), ThrowableAdapter.ThrowableClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var errorsBinding: ChuckerFragmentThrowableListBinding
    private lateinit var errorsAdapter: ThrowableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        errorsBinding = ChuckerFragmentThrowableListBinding.inflate(inflater, container, false)
        errorsAdapter = ThrowableAdapter(this)

        with(errorsBinding) {
            tutorialLink.movementMethod = LinkMovementMethod.getInstance()
            errorsRecyclerView.apply {
                setHasFixedSize(true)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = errorsAdapter
            }
        }

        return errorsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.throwables.observe(
            viewLifecycleOwner,
            Observer { throwables ->
                errorsAdapter.setData(throwables)
                errorsBinding.tutorialView.visibility = if (throwables.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chucker_throwables_list, menu)
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.chucker_clear)
            .setMessage(R.string.chucker_clear_throwable_confirmation)
            .setPositiveButton(R.string.chucker_clear) { _, _ ->
                viewModel.clearThrowables()
            }
            .setNegativeButton(R.string.chucker_cancel, null)
            .show()
    }

    override fun onThrowableClick(throwableId: Long, position: Int) {
        ThrowableActivity.start(requireActivity(), throwableId)
    }

    companion object {
        fun newInstance() = ThrowableListFragment()
    }
}
