package com.chuckerteam.chucker.api

import java.io.Serializable
import java.util.UUID

public data class Group(val name: String, val urls: List<String>) : Serializable {
    internal val id = UUID.randomUUID().toString()
    internal var isChecked: Boolean = false

    public companion object {
        public const val serialVersionUID: Long = 1
    }
}
