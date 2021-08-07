package com.tajmoti.libtvprovider

interface Episode : Streamable, Comparable<Episode> {
    /**
     * Episode number or null if it's a special
     */
    val number: Int?

    /**
     * Name of the episode or null in case it's unknown
     */
    val name: String?

    data class Info(
        override val key: String,
        val number: Int?,
        val name: String?
    ) : Marshallable

    override fun compareTo(other: Episode): Int {
        val otherNum = other.number
        otherNum ?: return 0
        return number?.compareTo(otherNum) ?: 0
    }
}