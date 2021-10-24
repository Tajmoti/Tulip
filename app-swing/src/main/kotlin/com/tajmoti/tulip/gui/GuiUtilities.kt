package com.tajmoti.tulip.gui

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane

fun JTabbedPane.addCloseButtonAt(index: Int, onClose: () -> Unit): JLabel {
    val origTitle = getTitleAt(index)
    val origIcon = getIconAt(index)
    val component = getComponentAt(index)

    val pnlTab = JPanel(GridBagLayout())
    pnlTab.isOpaque = false

    val gbc = GridBagConstraints()
    gbc.gridy = 0
    gbc.weightx = 1.0

    // Icon
    val icon = JLabel(origIcon)
    gbc.gridx = 0
    gbc.insets = Insets(0, 0, 0, 6)
    pnlTab.add(icon, gbc)

    // Label
    val lblTitle = JLabel(origTitle)
    gbc.gridx++
    gbc.gridy = 0
    gbc.insets = Insets(2, 0, 0, 0)
    pnlTab.add(lblTitle, gbc)

    // Close button
    val btnClose = JButton(GuiConstants.ICON_CLOSE)
    btnClose.horizontalAlignment = JButton.RIGHT
    btnClose.verticalAlignment = JButton.TOP
    gbc.gridx++
    gbc.insets = Insets(0, 16, 0, 0)
    pnlTab.add(btnClose, gbc)

    setTabComponentAt(index, pnlTab)
    btnClose.addActionListener {
        remove(component)
        onClose()
    }
    return lblTitle
}