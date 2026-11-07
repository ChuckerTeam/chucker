package com.chuckerteam.chucker.internal.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.chuckerteam.chucker.internal.data.repository.RepositoryProvider

internal abstract class BaseChuckerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.initialize(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
    }

    companion object {
        var isInForeground: Boolean = false
            private set
    }

    /** Lays out [root] edge to edge, keeping [appBar] and the content clear of the system bars and keyboard. */
    protected fun applySystemBarInsets(
        root: View,
        appBar: View,
    ) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            appBar.updatePadding(top = insets.top)
            view.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    fun showToast(
        message: String,
        toastDuration: Int = Toast.LENGTH_SHORT,
    ) {
        Toast.makeText(this.applicationContext, message, toastDuration).show()
    }
}
