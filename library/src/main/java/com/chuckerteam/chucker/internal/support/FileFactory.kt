package com.chuckerteam.chucker.internal.support

import java.io.File

internal interface FileFactory {
    val exportFileName: String get() = "transactions.txt"
    fun create(): File
    fun create(filename: String): File
}
