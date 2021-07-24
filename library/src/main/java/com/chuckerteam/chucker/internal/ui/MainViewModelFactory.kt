package com.chuckerteam.chucker.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.internal.data.repository.HttpTransactionRepository
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

internal class MainViewModelFactory : ViewModelProvider.Factory {
    private val transaction: HttpTransactionRepository = RepositoryProvider.transaction()
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(transaction) as T
    }
}
