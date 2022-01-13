package com.tajmoti.tulip.ui.utils

import android.os.Handler
import android.os.Message

class EasyHandler(
    private val handlers: Map<Int, (Message) -> Unit>
) : Handler.Callback {
    override fun handleMessage(msg: Message): Boolean {
        val handler = handlers[msg.what]
        return if (handler != null) {
            handler(msg)
            true
        } else {
            false
        }
    }
}