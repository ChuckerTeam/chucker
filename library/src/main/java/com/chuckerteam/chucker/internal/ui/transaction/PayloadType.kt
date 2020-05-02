package com.chuckerteam.chucker.internal.ui.transaction

import java.io.Serializable

sealed class PayloadType : Serializable {
    object Request : PayloadType()
    object Response : PayloadType()
}
