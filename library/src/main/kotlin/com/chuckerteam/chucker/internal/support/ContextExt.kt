package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.internal.data.model.DialogData
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal fun Context.showDialog(
    dialogData: DialogData,
    onPositiveClick: (() -> Unit)?,
    onNegativeClick: (() -> Unit)?
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(dialogData.title)
        .setMessage(dialogData.message)
        .setPositiveButton(dialogData.positiveButtonText) { _, _ ->
            onPositiveClick?.invoke()
        }
        .setNegativeButton(dialogData.negativeButtonText) { _, _ ->
            onNegativeClick?.invoke()
        }
        .show()
}
