package com.chuckerteam.chucker.util

import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.internal.support.Logger
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

internal class NoLoggerRule : BeforeAllCallback, AfterAllCallback {
    private val defaultLogger = Chucker.logger

    override fun beforeAll(context: ExtensionContext) {
        Chucker.logger = object : Logger {
            override fun info(message: String, throwable: Throwable?) = Unit

            override fun warn(message: String, throwable: Throwable?) = Unit

            override fun error(message: String, throwable: Throwable?) = Unit
        }
    }

    override fun afterAll(context: ExtensionContext) {
        Chucker.logger = defaultLogger
    }
}
