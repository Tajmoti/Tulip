package com.tajmoti.tulip.gui.player

import uk.co.caprica.vlcj.player.base.MediaPlayer
import java.util.concurrent.CountDownLatch

fun MediaPlayer.submitOn(task: MediaPlayer.() -> Unit) {
    submit { task(this) }
}

fun <T : Any> MediaPlayer.submitGet(task: MediaPlayer.() -> T): T {
    lateinit var returnValue: T
    val latch = CountDownLatch(1)
    submit {
        returnValue = task(this)
        latch.countDown()
    }
    latch.await()
    return returnValue
}