package com.chuckerteam.chucker.internal.data.entity

internal data class HttpCookie(val name: String, val value: String) {
    companion object {
        fun fromRequestHeader(cookieHeader: String?): List<HttpCookie> =
            mutableListOf<HttpCookie>().apply {
                if (!cookieHeader.isNullOrEmpty()) {
                    cookieHeader.split(";").forEach { c ->
                        val (name, value) = c.split("=")
                        add(HttpCookie(name.trim(), value.trim()))
                    }
                }
            }

        fun fromResponseHeaders(headers: List<HttpHeader>?): List<HttpCookie> =
            mutableListOf<HttpCookie>().apply {
                headers?.forEach { h ->
                    h.value.split(",").forEach { c ->
                        val cookieParts = c.split(";")
                        val (name, value) = cookieParts[0].split("=")
                        add(HttpCookie(name.trim(), value.trim()))
                    }
                }
            }
    }
}
