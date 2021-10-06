package com.tajmoti.tulip.ui.player

/**
 * The number of steps that the seek and progress bars have.
 */
val UI_PROGRESS_STEPS = 1_000

fun convertToUiProgress(realProgress: Float): Int {
    return (realProgress * UI_PROGRESS_STEPS).toInt()
}

fun convertFromUiProgress(uiProgress: Int): Float {
    return (uiProgress.toFloat() / UI_PROGRESS_STEPS)
}