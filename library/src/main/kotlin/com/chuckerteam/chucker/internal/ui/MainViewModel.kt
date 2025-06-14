package com.chuckerteam.chucker.internal.ui

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import com.chuckerteam.chucker.internal.support.NotificationHelper
import kotlinx.coroutines.launch

internal class MainViewModel : ViewModel() {
    /**
     * Holds the current search filter used to filter transactions.
     */
    private val currentFilter = MutableLiveData("")

    /**
     * Holds the list of selected transaction IDs.
     */
    private val mutableSelectedItemIds = MutableLiveData<List<Long>>(emptyList())

    /**
     * Indicates whether any transaction is currently selected.
     */
    private val mutableIsItemSelected = MutableLiveData(false)

    internal val transactions: LiveData<List<HttpTransactionTuple>> =
        currentFilter.switchMap { searchQuery ->
            with(RepositoryProvider.transaction()) {
                when {
                    searchQuery.isNullOrBlank() -> getSortedTransactionTuples()
                    searchQuery.isDigitsOnly() -> getFilteredTransactionTuples(searchQuery, "")
                    else -> getFilteredTransactionTuples("", searchQuery)
                }
            }
        }

    /**
     * LiveData indicating whether any items are currently selected.
     * Observers are notified only when the value changes.
     */
    internal val isItemSelected: LiveData<Boolean> = mutableIsItemSelected.distinctUntilChanged()

    /**
     * Returns either all transactions or only the selected ones,
     * depending on whether items are selected.
     */
    suspend fun getTransactions(): List<HttpTransaction> {
        val ids = mutableSelectedItemIds.value.orEmpty()
        return if (mutableIsItemSelected.value == true && ids.isNotEmpty()) {
            RepositoryProvider.transaction().getSelectedTransactions(ids)
        } else {
            RepositoryProvider.transaction().getAllTransactions()
        }
    }

    /**
     * Toggles the selection state of the given transaction ID.
     * If the ID is already selected, it will be removed from the selection.
     * If not selected, it will be added to the selection list.
     *
     * Also updates the selection mode state accordingly.
     * This is typically used on regular tap events when selection mode is active.
     *
     * @param itemId The transaction ID to toggle.
     */
    internal fun toggleSelection(itemId: Long) {
        val current = mutableSelectedItemIds.value.orEmpty().toMutableList()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        mutableSelectedItemIds.value = current
        mutableIsItemSelected.value = current.isNotEmpty()
    }

    /**
     * Starts the selection mode with the given transaction ID.
     * If already in selection mode, behaves the same as [toggleSelection] to allow toggling.
     * If not in selection mode, this marks the item as the first selected and enters selection mode.
     *
     * This is typically triggered via a long-press interaction.
     *
     * @param itemId The transaction ID to start selection with.
     */
    internal fun startSelection(itemId: Long) {
        if (mutableIsItemSelected.value == true) {
            toggleSelection(itemId)
        } else {
            mutableSelectedItemIds.value = listOf(itemId)
            mutableIsItemSelected.value = true
        }
    }

    /**
     * Updates the transaction filter string to trigger filtering of transactions.
     *
     * @param searchQuery The new filter text value.
     */
    internal fun updateItemsFilter(searchQuery: String) {
        currentFilter.value = searchQuery
    }

    /**
     * Returns the list of currently selected transaction IDs.
     *
     * @return A list of selected transaction IDs, or an empty list if none are selected.
     */
    internal fun getSelectedIds(): List<Long> = mutableSelectedItemIds.value.orEmpty()

    /**
     * Restores a previously saved selection state by setting the provided list of transaction IDs.
     * Also updates the selection mode based on whether the list is empty or not.
     *
     * This is typically used when restoring selection after configuration changes,
     * such as screen rotation or process recreation.
     *
     * @param ids The list of transaction IDs to restore as selected.
     */
    internal fun restoreSelection(ids: List<Long>) {
        mutableSelectedItemIds.value = ids
        mutableIsItemSelected.value = ids.isNotEmpty()
    }

    /**
     * Clears all transactions or only selected ones, based on the selection state.
     * Also resets selection state and clears notification buffer.
     */
    internal fun clearTransactions() =
        viewModelScope.launch {
            val ids = mutableSelectedItemIds.value.orEmpty()
            if (mutableIsItemSelected.value == true && ids.isNotEmpty()) {
                RepositoryProvider.transaction().deleteSelectedTransactions(ids)
            } else {
                RepositoryProvider.transaction().deleteAllTransactions()
            }
            mutableSelectedItemIds.value = emptyList()
            mutableIsItemSelected.value = false
            NotificationHelper.clearBuffer()
        }
}
