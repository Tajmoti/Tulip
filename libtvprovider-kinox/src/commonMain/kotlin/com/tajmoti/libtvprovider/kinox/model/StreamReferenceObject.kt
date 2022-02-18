@file:OptIn(ExperimentalSerializationApi::class)

package com.tajmoti.libtvprovider.kinox.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class StreamReferenceObject(
    @JsonNames("Stream")
    val stream: String,
    @JsonNames("HosterName")
    val hosterName: String,
    @JsonNames("HosterHome")
    val hosterHome: String,
)