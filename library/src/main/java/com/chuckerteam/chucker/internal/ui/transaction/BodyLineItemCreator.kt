package com.chuckerteam.chucker.internal.ui.transaction

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import com.chuckerteam.chucker.internal.support.Logger

internal class BodyLineItemCreator(private val bodyLine: String, private val onUrlClicked: (String) -> Unit) {

    fun create(): SpannableStringBuilder =
        SpannableStringBuilder().apply {
            append(bodyLine)
            checkUrl { url, range ->
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onUrlClicked(url)
                    }
                }

                setSpan(clickableSpan, range.first, range.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

    private fun checkUrl(link: (String, IntRange) -> Unit) {
        val matchGroup = getValue() ?: return
        try {
            if (URL_MATCHER.toRegex().matches(matchGroup.value)) {
                link.invoke(matchGroup.value, matchGroup.range)
            }
        } catch (ignored: IllegalStateException) {
            Logger.info("regex not matched for bodyLine $bodyLine")
        }
    }

    private fun getValue(): MatchGroup? = KEY_VALUE_SEPARATOR.toRegex().matchEntire(bodyLine)?.groups?.get(2)

    companion object {

        private const val KEY_VALUE_SEPARATOR = "(.*)?\".*\":\\s\"(.*)\"(.*)?"
        private const val URL_MATCHER = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    }
}
