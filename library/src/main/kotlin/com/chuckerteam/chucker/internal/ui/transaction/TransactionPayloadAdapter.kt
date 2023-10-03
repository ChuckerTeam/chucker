package com.chuckerteam.chucker.internal.ui.transaction

import android.animation.Animator
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
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemBodyCollapsableBinding
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemBodyLineBinding
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemHeadersBinding
import com.chuckerteam.chucker.databinding.ChuckerTransactionItemImageBinding
import com.chuckerteam.chucker.internal.support.ChessboardDrawable
import com.chuckerteam.chucker.internal.support.SpanTextUtil
import com.chuckerteam.chucker.internal.support.highlightWithDefinedColors
import com.chuckerteam.chucker.internal.support.highlightWithDefinedColorsSubstring
import com.chuckerteam.chucker.internal.support.indicesOf
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

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

            TYPE_BODY_COLLAPSABLE -> {
                val bodyItemBinding =
                    ChuckerTransactionItemBodyCollapsableBinding.inflate(inflater, parent, false)
                TransactionPayloadViewHolder.BodyJsonViewHolder(bodyItemBinding)
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
            is TransactionPayloadItem.BodyCollapsableItem -> TYPE_BODY_COLLAPSABLE
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
        private const val TYPE_BODY_COLLAPSABLE = 3
        private const val TYPE_IMAGE = 4
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

    internal class BodyJsonViewHolder(
        private val bodyBinding: ChuckerTransactionItemBodyCollapsableBinding
    ) : TransactionPayloadViewHolder(bodyBinding.root) {

        override fun bind(item: TransactionPayloadItem) {
            if (item !is TransactionPayloadItem.BodyCollapsableItem) return

            if (item.jsonElement == null) {
                bodyBinding.clRoot.visibility = View.GONE
                return
            }

            val body = item.jsonElement

            when {
                body.isJsonPrimitive -> {
                    bodyBinding.imgExpand.visibility = View.GONE
                    bodyBinding.rvSectionData.visibility = View.GONE
                    bodyBinding.txtStartValue.text = body.asString.plus(",")
                }

                body.isJsonObject -> body.asJsonObject.showObjects()
                body.isJsonArray -> body.asJsonArray.showArrayObjects()
                else -> Unit
            }
        }

        private fun JsonObject.showProperties() {
            val attrList = mutableListOf<TransactionPayloadItem.BodyCollapsableItem>()

            for ((key, value) in entrySet()) {
                JsonObject().also {
                    it.add(key, value)
                    attrList.add(TransactionPayloadItem.BodyCollapsableItem(jsonElement = it))
                }
            }

            bodyBinding.rvSectionData.visibility = View.VISIBLE
            bodyBinding.rvSectionData.adapter = TransactionBodyAdapter().also { adapter ->
                adapter.setItems(attrList)
            }
        }

        private fun JsonObject.showObjects() {
            val obj = this
            val keys = obj.keySet()

            if (keys.size == 0) return

            // { "key" : "value" }
            if (keys.size == 1) {
                val key = obj.keySet().first()
                val value: JsonElement = obj.get(key) ?: return
                val keyText = "\"" + key + "\""

                bodyBinding.imgExpand.visibility = View.GONE
                bodyBinding.txtKey.text = keyText

                when {
                    value.isJsonPrimitive || value.isJsonNull -> {
                        val text = if (value.isJsonNull) "null" else "\"${value.asString}\""

                        bodyBinding.txtStartValue.text = text.plus(",")
                        bodyBinding.txtEndValue.visibility = View.GONE
                    }

                    value.isJsonObject -> {
                        if (value.asJsonObject.isEmpty) {
                            bodyBinding.rvSectionData.visibility = View.GONE
                            bodyBinding.txtStartValue.text = "{},"
                            bodyBinding.txtEndValue.visibility = View.GONE
                        } else {
                            bodyBinding.root.setClickForValue(element = value)
                        }
                    }

                    value.isJsonArray -> {
                        bodyBinding.root.setClickForValue(element = value)
                    }
                }
            } else {
                // { "key1" : "value1", "key2" : "value2" }
                bodyBinding.imgExpand.visibility = View.GONE
                bodyBinding.txtKey.visibility = View.GONE
                bodyBinding.txtDivider.visibility = View.GONE
                bodyBinding.txtStartValue.visibility = View.GONE
                bodyBinding.txtEndValue.visibility = View.GONE
                obj.showProperties()
            }
        }

        private fun JsonArray.showArrayObjects() {
            map {
                TransactionPayloadItem.BodyCollapsableItem(jsonElement = it.asJsonObject)
            }.also { list ->
                with(bodyBinding) {
                    imgExpand.visibility = View.GONE
                    txtKey.visibility = View.GONE
                    txtDivider.visibility = View.GONE
                    txtStartValue.visibility = View.GONE
                    txtEndValue.visibility = View.GONE
                    rvSectionData.visibility = View.VISIBLE
                    rvSectionData.adapter = TransactionBodyAdapter().also { adapter ->
                        adapter.setItems(list)
                    }
                }
            }
        }

        private fun View.setClickForValue(element: JsonElement) = with(bodyBinding) {
            var isOpen = false

            imgExpand.visibility = View.VISIBLE
            txtStartValue.text = if (element.isJsonObject) "{...}" else "[...]"
            txtEndValue.visibility = View.GONE

            setOnClickListener { view ->
                isOpen = isOpen.not()

                imgExpand.animate()
                    .rotationBy(if (isOpen) OPEN_ROTATION_VALUE else CLOSE_ROTATION_VALUE)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(p0: Animator) {
                            view.isClickable = false
                        }

                        override fun onAnimationEnd(p0: Animator) {
                            view.isClickable = true

                            if (isOpen) {
                                rvSectionData.visibility = View.VISIBLE
                                txtStartValue.text = if (element.isJsonObject) "{" else "["
                                txtEndValue.visibility = View.VISIBLE
                                txtEndValue.text = if (element.isJsonObject) "}," else "],"
                            } else {
                                rvSectionData.visibility = View.GONE
                                txtStartValue.text =
                                    if (element.isJsonObject) "{...}," else "[...],"
                                txtEndValue.visibility = View.GONE
                            }

                            rvSectionData.adapter = TransactionBodyAdapter().also { adapter ->
                                adapter.setItems(
                                    listOf(TransactionPayloadItem.BodyCollapsableItem(jsonElement = element))
                                )
                            }
                        }

                        @Suppress("EmptyFunctionBlock")
                        override fun onAnimationCancel(p0: Animator) {
                        }

                        @Suppress("EmptyFunctionBlock")
                        override fun onAnimationRepeat(p0: Animator) {
                        }
                    })
            }
        }

        internal companion object {
            const val OPEN_ROTATION_VALUE = 180f
            const val CLOSE_ROTATION_VALUE = -180f
        }
    }
}

internal sealed class TransactionPayloadItem {
    internal class HeaderItem(val headers: Spanned) : TransactionPayloadItem()
    internal class BodyLineItem(var line: SpannableStringBuilder) : TransactionPayloadItem()
    internal class BodyCollapsableItem(val jsonElement: JsonElement?) : TransactionPayloadItem()
    internal class ImageItem(val image: Bitmap, val luminance: Double?) : TransactionPayloadItem()
}
