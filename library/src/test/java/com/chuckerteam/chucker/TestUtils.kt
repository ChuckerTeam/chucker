package com.chuckerteam.chucker

import java.io.File
import okio.Buffer
import okio.Okio

fun getResourceFile(file: String): Buffer {
    return Buffer().apply {
        writeAll(Okio.buffer(Okio.source(File("./src/test/resources/$file"))))
    }
}
