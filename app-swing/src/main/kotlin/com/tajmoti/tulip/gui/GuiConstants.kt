package com.tajmoti.tulip.gui

import jiconfont.IconCode
import jiconfont.icons.font_awesome.FontAwesome
import jiconfont.swing.IconFontSwing
import java.awt.Color
import javax.swing.Icon
import javax.swing.UIManager

object GuiConstants {
    val COLOR_BACKGROUND: Color by lazy { UIManager.getColor("TextField.light") }
    val ICON_SEARCH: Icon by lazy { getIcon(FontAwesome.SEARCH) }
    val ICON_TV_SHOW: Icon by lazy { getIcon(FontAwesome.TELEVISION) }
    val ICON_CLOSE: Icon by lazy { getIcon(FontAwesome.TIMES) }

    private fun getIcon(code: IconCode): Icon {
        return IconFontSwing.buildIcon(code, 16f, UIManager.getColor("controlText"))
    }
}