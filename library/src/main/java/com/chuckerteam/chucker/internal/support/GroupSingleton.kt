package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.Group

internal object GroupSingleton {
    internal var groups: MutableList<Group> = mutableListOf()
}
