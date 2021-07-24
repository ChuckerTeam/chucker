package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.Group

internal object GroupSingleton {
    internal lateinit var groups: MutableList<Group>

    fun getMergedGroups(others: List<Group>): List<Group> {
        others.forEach { other -> 
            val group = groups.find { it.id == other.id }!!
            groups.updateItem(group, other)
        }
        return groups
    }

    private fun <T> MutableList<T>.updateItem(item: T, newItem: T) {
        val index = this.indexOf(item)
        this[index] = newItem
    }
}
