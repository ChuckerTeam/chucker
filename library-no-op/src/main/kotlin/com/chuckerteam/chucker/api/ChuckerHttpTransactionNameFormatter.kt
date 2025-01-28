package com.chuckerteam.chucker.api

/**
 * No-op declaration.
 */
public fun interface ChuckerHttpTransactionNameFormatter {
    public fun provideTransactionDisplayName(transaction: ChuckerHttpTransaction): CharSequence?
}
