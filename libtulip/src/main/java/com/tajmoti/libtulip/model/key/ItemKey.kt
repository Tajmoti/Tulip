package com.tajmoti.libtulip.model.key

import java.io.Serializable

interface ItemKey : Serializable {
    sealed interface Hosted : ItemKey
    sealed interface Tmdb : ItemKey
}