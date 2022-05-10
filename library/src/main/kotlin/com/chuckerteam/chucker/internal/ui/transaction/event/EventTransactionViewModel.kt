package com.chuckerteam.chucker.internal.ui.transaction.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.entity.EventTransaction
import com.chuckerteam.chucker.internal.ui.transaction.TransactionViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class EventTransactionViewModel(
    private val sharedViewModel: TransactionViewModel
) : ViewModel() {
    val transaction: StateFlow<EventTransaction?>
        get() = sharedViewModel.transaction
            .map { it as EventTransaction? }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

}

internal class EventTransactionViewModelFactory(
    private val sharedViewModel: TransactionViewModel
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == EventTransactionViewModel::class.java) { "Cannot create $modelClass" }
        @Suppress("UNCHECKED_CAST")
        return EventTransactionViewModel(sharedViewModel) as T
    }
}
