package com.tajmoti.libtvprovider.southpark.model

import kotlinx.serialization.Serializable

@Serializable
data class SouthParkEpisodeResponse(
    val items: List<Item>,
)

@Serializable
data class Item(
    val cardSubType: String,
    val entityType: String,
    val id: String,
    val itemType: String,
    val media: Media,
    val meta: Meta,
    val url: String
)

@Serializable
data class Media(
    val duration: String,
    val image: Image,
)

@Serializable
data class Meta(
    val description: String,
    val header: HeaderX,
    val label: String,
    val subHeader: String
)

@Serializable
data class Image(
    val height: Int,
    val ratio: String,
    val url: String,
    val width: Int
)

@Serializable
data class HeaderX(
    val title: String
)