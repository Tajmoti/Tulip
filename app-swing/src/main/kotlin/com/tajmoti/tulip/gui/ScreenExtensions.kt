package com.tajmoti.tulip.gui

import javax.swing.JFrame

fun <T : JFrame> Screen<T>.closeWindow() {
    cleanup()
    root.dispose()
}