package com.tajmoti.tulip.gui.dependencies

import java.awt.BorderLayout
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar
import kotlin.system.exitProcess

object DependenciesDialogFactory {

    fun showDownloadingDialog(steps: Int): Pair<JDialog, JProgressBar> {
        val dlg = JDialog(null as JFrame?, "Tulip Initialization")
        val dpb = JProgressBar(0, steps)
        dlg.add(BorderLayout.CENTER, dpb)
        dlg.add(BorderLayout.NORTH, JLabel("Downloading required files, please wait..."))
        dlg.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        dlg.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {

            }

            override fun windowClosing(e: WindowEvent) {
                exitProcess(2)
            }

            override fun windowClosed(e: WindowEvent) {

            }

            override fun windowIconified(e: WindowEvent) {

            }

            override fun windowDeiconified(e: WindowEvent) {

            }

            override fun windowActivated(e: WindowEvent) {

            }

            override fun windowDeactivated(e: WindowEvent) {

            }
        })
        dlg.setSize(300, 75)
        dlg.isVisible = true
        return dlg to dpb
    }
}