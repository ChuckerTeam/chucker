package com.chuckerteam.chucker.internal.ui.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chuckerteam.chucker.internal.data.model.DEFAULT_REQUEST_TAG_FILTER
import com.chuckerteam.chucker.internal.data.model.RequestTagFilterItem
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider
import kotlinx.coroutines.launch

internal class FilterViewModel : ViewModel() {

    private val _requestTagFilters = MutableLiveData(listOf(DEFAULT_REQUEST_TAG_FILTER))

    val requestTagFilters: LiveData<List<RequestTagFilterItem>> = _requestTagFilters

    init {
        viewModelScope.launch {
            _requestTagFilters.value = RepositoryProvider.transaction().getAllRequestTags()
                .map { RequestTagFilterItem(name = it, isSelected = true) }.toMutableList().apply {
                    add(DEFAULT_REQUEST_TAG_FILTER)
                }
        }
    }

    fun requestTagAction(requestTag: RequestTagFilterItem) {
        val index = requestTagFilters.value!!.indexOf(requestTag)

        _requestTagFilters.value = _requestTagFilters.value!!.toMutableList().apply {
            removeAt(index)
            add(index, requestTag.copy(isSelected = requestTag.isSelected.not()))
        }
    }

    fun getSelectedRequestTags(): Array<out String?> {
        return if (requestTagFilters.value?.all { it.isSelected } == true) {
            emptyArray()
        } else {
            requestTagFilters.value?.asSequence()
                ?.filter { it.isSelected }
                ?.map { if (it.isNoTagItem && it.isSelected) it.copy(name = null) else it }?.map { it.name }?.toList()
                ?.toTypedArray().orEmpty()
        }
    }

    fun selectAll() {
        if (requestTagFilters.value?.any { it.isSelected } == true) {
            _requestTagFilters.value = requestTagFilters.value?.map { it.copy(isSelected = false) }
        } else {
            _requestTagFilters.value = requestTagFilters.value?.map { it.copy(isSelected = true) }
        }
    }
}
