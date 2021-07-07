package com.chuckerteam.chucker.internal.data.model

internal data class RequestTagFilterItem(
    val name: String?,
    val isSelected: Boolean = false,
    val isNoTagItem: Boolean = false
)

internal val DEFAULT_REQUEST_TAG_FILTER =
    RequestTagFilterItem(name = "(No Request Tag)", isSelected = true, isNoTagItem = true)
