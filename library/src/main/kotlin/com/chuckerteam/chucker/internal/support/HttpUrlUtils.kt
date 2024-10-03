package com.chuckerteam.chucker.internal.support

import okhttp3.HttpUrl

private const val PATH_SEGMENTS_DELIMITER = "/"

public fun HttpUrl.Builder.addNonBlankPathSegments(candidatePath: String): HttpUrl.Builder =
    apply {
        candidatePath.split(PATH_SEGMENTS_DELIMITER).filter { it.isNotBlank() }
            .forEach { item -> addPathSegment(item) }
    }
