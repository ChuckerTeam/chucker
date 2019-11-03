package com.chuckerteam.chucker.api.config

import android.content.Context
import com.chuckerteam.chucker.api.internal.EmptyFragment

class ErrorsFeature(
    override var enabled: Boolean,
    var showNotification: Boolean
) : TabFeature {
    override val name: Int = 0

    override val id: Int = 0

    override fun newFragment() = EmptyFragment()

    override fun dismissNotification(context: Context) {
        // Empty method for the library-no-op artifact
    }
}
