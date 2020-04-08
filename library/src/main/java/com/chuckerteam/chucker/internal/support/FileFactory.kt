package com.chuckerteam.chucker.internal.support

import java.io.File

internal interface FileFactory {
    fun createFileForResponseBody(): File
    fun createFileForExport(): File
}
