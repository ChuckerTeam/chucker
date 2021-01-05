package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.Chucker

internal interface Logger {
    fun info(message: String)

    fun warn(message: String)

    fun error(message: String, throwable: Throwable? = null)

    companion object : Logger {
        override fun info(message: String) {
            Chucker.logger.info(message)
        }

        override fun warn(message: String) {
            Chucker.logger.warn(message)
        }

        override fun error(message: String, throwable: Throwable?) {
            Chucker.logger.error(message, throwable)
        }
    }
}
