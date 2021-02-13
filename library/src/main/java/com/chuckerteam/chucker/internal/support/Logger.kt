package com.chuckerteam.chucker.internal.support

import com.chuckerteam.chucker.api.Chucker

internal interface Logger {
    fun info(message: String, throwable: Throwable? = null)

    fun warn(message: String, throwable: Throwable? = null)

    fun error(message: String, throwable: Throwable? = null)

    companion object : Logger {
        override fun info(message: String, throwable: Throwable?) {
            Chucker.logger.info(message, throwable)
        }

        override fun warn(message: String, throwable: Throwable?) {
            Chucker.logger.warn(message, throwable)
        }

        override fun error(message: String, throwable: Throwable?) {
            Chucker.logger.error(message, throwable)
        }
    }
}
