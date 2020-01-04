package com.chuckerteam.chucker.internal.ui.transaction

import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chuckerteam.chucker.R
import com.chuckerteam.chucker.internal.support.highlightWithDefinedColors

/**
 * Adapter responsible of showing the content of the Transaction Request/Response body.
 * We're using a [RecyclerView] to show the content of the body line by line to do not affect
 * performances when loading big payloads.
 */
internal class TransactionBodyAdapter(
    private val bodyItems: List<TransactionPayloadItem>
) : RecyclerView.Adapter<TransactionPayloadViewHolder>() {

    override fun onBindViewHolder(holder: TransactionPayloadViewHolder, position: Int) {
        holder.bind(bodyItems[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionPayloadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADERS -> {
                val view = inflater.inflate(R.layout.chucker_transaction_item_headers, parent, false)
                TransactionPayloadViewHolder.HeaderViewHolder(view)
            }
            TYPE_BODY_LINE -> {
                val view = inflater.inflate(R.layout.chucker_transaction_item_body_line, parent, false)
                TransactionPayloadViewHolder.BodyLineViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.chucker_transaction_item_image, parent, false)
                TransactionPayloadViewHolder.ImageViewHolder(view)
            }
        }
    }

    override fun getItemCount() = bodyItems.size

    override fun getItemViewType(position: Int): Int {
        return when (bodyItems[position]) {
            is TransactionPayloadItem.HeaderItem -> TYPE_HEADERS
            is TransactionPayloadItem.BodyLineItem -> TYPE_BODY_LINE
            is TransactionPayloadItem.ImageItem -> TYPE_IMAGE
        }
    }

    internal fun highlightQueryWithColors(newText: String, backgroundColor: Int, foregroundColor: Int) {
        bodyItems.filterIsInstance<TransactionPayloadItem.BodyLineItem>()
            .withIndex()
            .forEach { (index, item) ->
                if (newText in item.line) {
                    item.line.clearSpans()
                    item.line = item.line.toString()
                        .highlightWithDefinedColors(newText, backgroundColor, foregroundColor)
                    notifyItemChanged(index + 1)
                } else {
                    // Let's clear the spans if we haven't found the query string.
                    val spans = item.line.getSpans(0, item.line.length - 1, Any::class.java)
                    if (spans.isNotEmpty()) {
                        item.line.clearSpans()
                        notifyItemChanged(index + 1)
                    }
                }
            }
    }

    internal fun resetHighlight() {
        bodyItems.filterIsInstance<TransactionPayloadItem.BodyLineItem>()
            .withIndex()
            .forEach { (index, item) ->
                val spans = item.line.getSpans(0, item.line.length - 1, Any::class.java)
                if (spans.isNotEmpty()) {
                    item.line.clearSpans()
                    notifyItemChanged(index + 1)
                }
            }
    }

    companion object {
        private const val TYPE_HEADERS = 1
        private const val TYPE_BODY_LINE = 2
        private const val TYPE_IMAGE = 3
    }
}

internal sealed class TransactionPayloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: TransactionPayloadItem)

    internal class HeaderViewHolder(view: View) : TransactionPayloadViewHolder(view) {
        private val headersView: TextView = view.findViewById(R.id.headers)
        override fun bind(item: TransactionPayloadItem) {
            if (item is TransactionPayloadItem.HeaderItem) {
                headersView.text = item.headers
            }
        }
    }

    internal class BodyLineViewHolder(view: View) : TransactionPayloadViewHolder(view) {
        private val bodyLineView: TextView = view.findViewById(R.id.body_line)
        override fun bind(item: TransactionPayloadItem) {
            if (item is TransactionPayloadItem.BodyLineItem) {
                bodyLineView.text = item.line
            }
        }
    }

    internal class ImageViewHolder(view: View) : TransactionPayloadViewHolder(view) {
        private val binaryDataView: ImageView = view.findViewById(R.id.binary_data)
        override fun bind(item: TransactionPayloadItem) {
            if (item is TransactionPayloadItem.ImageItem) {
                binaryDataView.setImageBitmap(item.image)
            }
        }
    }
}

internal sealed class TransactionPayloadItem {
    internal class HeaderItem(val headers: Spanned) : TransactionPayloadItem()
    internal class BodyLineItem(var line: SpannableStringBuilder) : TransactionPayloadItem()
    internal class ImageItem(val image: Bitmap) : TransactionPayloadItem()
}
