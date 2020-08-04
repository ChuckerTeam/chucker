package com.chuckerteam.chucker.internal.support

import okhttp3.HttpUrl

internal class FormattedUrl private constructor(
    val scheme: String,
    val host: String,
    val port: Int,
    val path: String,
    val query: String
) {
    val pathWithQuery: String
        get() = if (query.isBlank()) {
            path
        } else {
            "$path?$query"
        }

    val url: String
        get() {
            return if (shouldShowPort()) {
                "$scheme://$host:$port$pathWithQuery"
            } else {
                "$scheme://$host$pathWithQuery"
            }
        }

    private fun shouldShowPort(): Boolean {
        if (scheme == "https" && port == 443) {
            return false
        }
        if (scheme == "http" && port == 80) {
            return false
        }
        return true
    }

    companion object {
        fun fromHttpUrl(httpUrl: HttpUrl, encoded: Boolean): FormattedUrl {
            return if (encoded) {
                encodedUrl(httpUrl)
            } else {
                decodedUrl(httpUrl)
            }
        }

        private fun encodedUrl(httpUrl: HttpUrl): FormattedUrl {
            val path = httpUrl.encodedPathSegments().joinToString("/")
            return FormattedUrl(
                httpUrl.scheme(),
                httpUrl.host(),
                httpUrl.port(),
                if (path.isNotBlank()) "/$path" else "",
                httpUrl.encodedQuery().orEmpty()
            )
        }

        private fun decodedUrl(httpUrl: HttpUrl): FormattedUrl {
            val path = httpUrl.pathSegments().joinToString("/")
            return FormattedUrl(
                httpUrl.scheme(),
                httpUrl.host(),
                httpUrl.port(),
                if (path.isNotBlank()) "/$path" else "",
                httpUrl.query().orEmpty()
            )
        }
    }
}
