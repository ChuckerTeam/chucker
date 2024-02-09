package com.chuckerteam.chucker.internal.support

import okhttp3.Response
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val IP_REGEX = "(?:\\d{1,3}\\.){3}\\d{1,3}"

public fun Response.getHostIp(): String? {
    val body = body?.source()?.readUtf8()
    val pattern: Pattern = Pattern.compile(IP_REGEX)
    val matcher: Matcher? = body?.let { pattern.matcher(it) }
    if (matcher?.find() == true) {
        return matcher.group()
    }
    return null
}
