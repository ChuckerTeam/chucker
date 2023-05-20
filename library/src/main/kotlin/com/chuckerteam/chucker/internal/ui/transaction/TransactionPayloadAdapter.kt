package com.chuckerteam.chucker.internal.ui.transaction

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.getSpans
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemBodyLineBinding
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemHeadersBinding
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemImageBinding
import com.chuckerteam.chucker.internal.support.ChessboardDrawable
import com.chuckerteam.chucker.internal.support.SpanTextUtil
import com.chuckerteam.chucker.internal.support.highlightWithDefinedColors
import com.chuckerteam.chucker.internal.support.highlightWithDefinedColorsSubstring
import com.chuckerteam.chucker.internal.support.indicesOf

/**
 * Adapter responsible of showing the content of the Transaction Request/Response body.
 * We're using a [RecyclerView] to show the content of the body line by line to do not affect
 * performances when loading big payloads.
 */
internal class TransactionBodyAdapter : RecyclerView.Adapter<TransactionPayloadViewHolder>() {

    private val items = arrayListOf<TransactionPayloadItem>()

    fun setItems(bodyItems: List<TransactionPayloadItem>) {
        val previousItemCount = items.size
        items.clear()
        items.addAll(bodyItems)
        notifyItemRangeRemoved(0, previousItemCount)
        notifyItemRangeInserted(0, items.size)
    }

    override fun onBindViewHolder(holder: TransactionPayloadViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionPayloadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADERS -> {
                val headersItemBinding = ChuckerTransactionItemHeadersBinding.inflate(inflater, parent, false)
                TransactionPayloadViewHolder.HeaderViewHolder(headersItemBinding)
            }

            TYPE_BODY_LINE -> {
                val bodyItemBinding = ChuckerTransactionItemBodyLineBinding.inflate(inflater, parent, false)
                TransactionPayloadViewHolder.BodyLineViewHolder(bodyItemBinding)
            }

            else -> {
                val imageItemBinding = ChuckerTransactionItemImageBinding.inflate(inflater, parent, false)
                TransactionPayloadViewHolder.ImageViewHolder(imageItemBinding)
            }
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionPayloadItem.HeaderItem -> TYPE_HEADERS
            is TransactionPayloadItem.BodyLineItem -> TYPE_BODY_LINE
            is TransactionPayloadItem.ImageItem -> TYPE_IMAGE
        }
    }

    internal fun highlightQueryWithColors(
        newText: String,
        backgroundColor: Int,
        foregroundColor: Int
    ): List<SearchItemBodyLine> {
        val listOfSearchItems = arrayListOf<SearchItemBodyLine>()
        items.filterIsInstance<TransactionPayloadItem.BodyLineItem>()
            .withIndex()
            .forEach { (index, item) ->
                val listOfOccurrences = item.line.indicesOf(newText)
                if (listOfOccurrences.isNotEmpty()) {
                    // storing the occurrences and their positions
                    listOfOccurrences.forEach {
                        listOfSearchItems.add(
                            SearchItemBodyLine(
                                indexBodyLine = index + 1,
                                indexStartOfQuerySubString = it
                            )
                        )
                    }

                    // highlighting the occurrences
                    item.line.clearHighlightSpans()
                    item.line = item.line.highlightWithDefinedColors(
                        newText,
                        listOfOccurrences,
                        backgroundColor,
                        foregroundColor
                    )
                    notifyItemChanged(index + 1)
                } else {
                    // Let's clear the spans if we haven't found the query string.
                    val removedSpansCount = item.line.clearHighlightSpans()
                    if (removedSpansCount > 0) {
                        notifyItemChanged(index + 1)
                    }
                }
            }
        return listOfSearchItems
    }

    internal fun highlightItemWithColorOnPosition(
        position: Int,
        queryStartPosition: Int,
        queryText: String,
        backgroundColor: Int,
        foregroundColor: Int
    ) {
        val item = items.getOrNull(position) as? TransactionPayloadItem.BodyLineItem
        if (item != null) {
            item.line = item.line.highlightWithDefinedColorsSubstring(
                queryText,
                queryStartPosition,
                backgroundColor,
                foregroundColor
            )
            notifyItemChanged(position)
        }
    }

    internal fun resetHighlight() {
        items.filterIsInstance<TransactionPayloadItem.BodyLineItem>()
            .withIndex()
            .forEach { (index, item) ->
                val removedSpansCount = item.line.clearHighlightSpans()
                if (removedSpansCount > 0) {
                    notifyItemChanged(index + 1)
                }
            }
    }

    companion object {
        private const val TYPE_HEADERS = 1
        private const val TYPE_BODY_LINE = 2
        private const val TYPE_IMAGE = 3
    }

    /**
     * Clear span that created during search process
     * @return Number of spans that removed.
     */
    private fun SpannableStringBuilder.clearHighlightSpans(): Int {
        var removedSpansCount = 0
        val spanList = getSpans<Any>(0, length)
        for (span in spanList)
            if (span !is SpanTextUtil.ChuckerForegroundColorSpan) {
                removeSpan(span)
                removedSpansCount++
            }
        return removedSpansCount
    }

    internal data class SearchItemBodyLine(
        val indexBodyLine: Int,
        val indexStartOfQuerySubString: Int
    )
}

internal sealed class TransactionPayloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: TransactionPayloadItem)

    internal class HeaderViewHolder(
        private val headerBinding: ChuckerTransactionItemHeadersBinding
    ) : TransactionPayloadViewHolder(headerBinding.root) {
        override fun bind(item: TransactionPayloadItem) {
            if (item is TransactionPayloadItem.HeaderItem) {
                headerBinding.responseHeaders.text = item.headers
            }
        }
    }

    internal class BodyLineViewHolder(
        private val bodyBinding: ChuckerTransactionItemBodyLineBinding
    ) : TransactionPayloadViewHolder(bodyBinding.root) {
        override fun bind(item: TransactionPayloadItem) {
            if (item is TransactionPayloadItem.BodyLineItem) {
                bodyBinding.bodyLine.text = item.line
            }
        }
    }

    internal class ImageViewHolder(
        private val imageBinding: ChuckerTransactionItemImageBinding
    ) : TransactionPayloadViewHolder(imageBinding.root) {

        override fun bind(item: TransactionPayloadItem) {
            if (item is TransactionPayloadItem.ImageItem) {
                imageBinding.binaryData.setImageBitmap(item.image)
                imageBinding.root.background = createContrastingBackground(item.luminance)
            }
        }

        private fun createContrastingBackground(luminance: Double?): Drawable? {
            if (luminance == null) return null

            return if (luminance < LUMINANCE_THRESHOLD) {
                ChessboardDrawable.createPattern(
                    itemView.context,
                    R.color.chucker_chessboard_even_square_light,
                    R.color.chucker_chessboard_odd_square_light,
                    R.dimen.chucker_half_grid
                )
            } else {
                ChessboardDrawable.createPattern(
                    itemView.context,
                    R.color.chucker_chessboard_even_square_dark,
                    R.color.chucker_chessboard_odd_square_dark,
                    R.dimen.chucker_half_grid
                )
            }
        }

        private companion object {
            const val LUMINANCE_THRESHOLD = 0.25
        }
    }
}

internal sealed class TransactionPayloadItem {
    internal class HeaderItem(val headers: Spanned) : TransactionPayloadItem()
    internal class BodyLineItem(var line: SpannableStringBuilder) : TransactionPayloadItem()
    internal class ImageItem(val image: Bitmap, val luminance: Double?) : TransactionPayloadItem()
}
