package com.chuckerteam.chucker.internal.support

import java.io.File

internal interface FileFactory {
    fun create(): File
    fun create(filename: String): File
}
