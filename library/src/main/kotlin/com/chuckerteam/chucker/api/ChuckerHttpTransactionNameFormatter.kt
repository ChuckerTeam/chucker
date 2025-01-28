package com.chuckerteam.chucker.api

/**
 * Chucker custom display name formatter for a http transaction.
 */
public fun interface ChuckerHttpTransactionNameFormatter {
    /**
     * Transforms [transaction] data into custom transaction display name that appears in the list of transactions.
     *
     * @param transaction - http transaction
     * @return custom display name. null value falls back to default display name formatting, i.e. methods and path
     */
    public fun provideTransactionDisplayName(transaction: ChuckerHttpTransaction): CharSequence?
}
