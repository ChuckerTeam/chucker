package com.chuckerteam.chucker.internal.ui.throwable

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.ui.MainViewModel

internal class ThrowableListFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ThrowableAdapter
    private lateinit var listener: ThrowableAdapter.ThrowableClickListListener
    private lateinit var tutorialView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chucker_fragment_throwable_list, container, false).apply {
            tutorialView = findViewById(R.id.chuckerThrowableTutorialView)
            findViewById<TextView>(R.id.chuckerThrowableTutorialLink).movementMethod = LinkMovementMethod.getInstance()

            val recyclerView = findViewById<RecyclerView>(R.id.chuckerThrowableRecyclerView)
            recyclerView.addItemDecoration(DividerItemDecoration(context, VERTICAL))
            adapter = ThrowableAdapter(listener)
            recyclerView.adapter = adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.throwables.observe(
            viewLifecycleOwner,
            Observer { throwables ->
                adapter.setData(throwables)
                tutorialView.visibility = if (throwables.isNullOrEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        require(context is ThrowableAdapter.ThrowableClickListListener) {
            "Context must implement the listener."
        }
        listener = context
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
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.chucker_clear)
            .setMessage(R.string.chucker_clear_throwable_confirmation)
            .setPositiveButton(R.string.chucker_clear) { _, _ ->
                viewModel.clearThrowables()
            }
            .setNegativeButton(R.string.chucker_cancel, null)
            .show()
    }

    companion object {
        fun newInstance() = ThrowableListFragment()
    }
}
