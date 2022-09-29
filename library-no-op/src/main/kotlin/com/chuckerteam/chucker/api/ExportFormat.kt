package com.chuckerteam.chucker.api

/**
 * The supported export format for transactions file exports.
 */
public enum class ExportFormat(public val extension: String) {
    /** LOG Format with txt extension */
    LOG("txt"),

    /** HAR format with har extension */
    HAR("har")
}
