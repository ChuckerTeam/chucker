package com.chuckerteam.chucker.internal.ui.transaction

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.test.core.app.ApplicationProvider
import com.chuckerteam.chucker.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TransactionBodyAdapterTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun bindCopyHolder(
        onCopy: () -> Unit = {},
        onMenu: (View) -> Unit = {},
    ): TransactionPayloadViewHolder.CopyViewHolder {
        val adapter = TransactionBodyAdapter(onCopy, onMenu)
        adapter.setItems(listOf(TransactionPayloadItem.CopyItem("copy")))
        val parent = FrameLayout(context)
        val holder =
            adapter.createViewHolder(parent, adapter.getItemViewType(0))
                as TransactionPayloadViewHolder.CopyViewHolder
        adapter.bindViewHolder(holder, 0)
        return holder
    }

    @Test
    fun `copy button becomes visible after binding a CopyItem`() {
        val holder = bindCopyHolder()
        val button = holder.itemView.findViewById<View>(R.id.responseCopy)

        assertThat(button.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun `tap on copy button invokes raw copy listener and not menu listener`() {
        var copyCount = 0
        var menuCount = 0
        val holder = bindCopyHolder({ copyCount++ }, { menuCount++ })
        val button = holder.itemView.findViewById<View>(R.id.responseCopy)

        button.performClick()

        assertThat(copyCount).isEqualTo(1)
        assertThat(menuCount).isEqualTo(0)
    }

    @Test
    fun `repeated taps each invoke raw copy listener`() {
        var copyCount = 0
        val holder = bindCopyHolder(onCopy = { copyCount++ })
        val button = holder.itemView.findViewById<View>(R.id.responseCopy)

        button.performClick()
        button.performClick()
        button.performClick()

        assertThat(copyCount).isEqualTo(3)
    }

    @Test
    fun `long press on copy button invokes menu listener with the button as anchor`() {
        var copyCount = 0
        val anchors = mutableListOf<View>()
        val holder = bindCopyHolder({ copyCount++ }, { anchors.add(it) })
        val button = holder.itemView.findViewById<View>(R.id.responseCopy)

        val handled = button.performLongClick()

        assertThat(handled).isTrue()
        assertThat(copyCount).isEqualTo(0)
        assertThat(anchors).containsExactly(button)
    }

    @Test
    fun `repeated long presses each invoke menu listener`() {
        var menuCount = 0
        val holder = bindCopyHolder(onMenu = { menuCount++ })
        val button = holder.itemView.findViewById<View>(R.id.responseCopy)

        button.performLongClick()
        button.performLongClick()

        assertThat(menuCount).isEqualTo(2)
    }

    @Test
    fun `copy payload menu exposes raw and formatted items`() {
        val popup = PopupMenu(context, View(context))
        popup.menuInflater.inflate(R.menu.chucker_copy_payload, popup.menu)
        val menu = popup.menu

        assertThat(menu.size()).isEqualTo(2)
        val rawItem = menu.findItem(R.id.copy_raw)
        val formattedItem = menu.findItem(R.id.copy_formatted)
        assertThat(rawItem.title.toString()).isEqualTo(context.getString(R.string.chucker_copy_raw))
        assertThat(formattedItem.title.toString())
            .isEqualTo(context.getString(R.string.chucker_copy_formatted))
    }
}
