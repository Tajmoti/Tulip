package com.tajmoti.libtvprovider.show

import com.tajmoti.libtvprovider.NamedItem
import com.tajmoti.libtvprovider.stream.Streamable

interface Episode: NamedItem, Streamable {
    data class Info(
        val key: String,
        val name: String
    )
}