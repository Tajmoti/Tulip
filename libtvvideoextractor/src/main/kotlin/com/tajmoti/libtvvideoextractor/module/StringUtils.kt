package com.tajmoti.libtvvideoextractor.module

fun String.substringBetween(startSequence: String, endSequence: String): String {
    val start = indexOf(startSequence) + startSequence.length
    val end = indexOf(endSequence, start + 1)
    return substring(start, end)
}