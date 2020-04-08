package com.chuckerteam.chucker.internal.support

import android.content.Context
import com.chuckerteam.chucker.internal.data.model.DialogData
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal object DialogUtils{
    fun showDialog(context: Context, dialogData: DialogData, positiveButtonHandler: (()->Unit)?, negativeButtonHandler: (() -> Unit)?){
        MaterialAlertDialogBuilder(context)
                .setTitle(dialogData.title)
                .setMessage(dialogData.message)
                .setPositiveButton(dialogData.postiveButtonText) { _, _ ->
                    positiveButtonHandler?.invoke()
                }
                .setNegativeButton(dialogData.negativeButtonText) {_, _ ->
                    negativeButtonHandler?.invoke()
                }
                .show()
    }
}