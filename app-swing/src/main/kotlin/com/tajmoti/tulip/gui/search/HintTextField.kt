package com.tajmoti.tulip.gui.search

import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextField

internal class HintTextField(private val hint: String) : JTextField(hint), FocusListener {
    private val textColor = foreground
    private var showingHint = true

    init {
        focusLost(null)
    }

    override fun focusGained(e: FocusEvent?) {
        foreground = textColor
        if (this.text.isEmpty()) {
            super.setText("")
            showingHint = false
        }
    }

    override fun focusLost(e: FocusEvent?) {
        foreground = disabledTextColor
        if (this.text.isEmpty()) {
            super.setText(hint)
            showingHint = true
        }
    }

    override fun getText(): String {
        return if (showingHint) "" else super.getText()
    }

    init {
        super.addFocusListener(this)
    }
}