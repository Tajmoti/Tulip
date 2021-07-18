package com.tajmoti.libprimewiretvprovider

import java.io.Serializable

data class PrimewireStreamableId(
    val name: String,
    val streamPageUrl: String,
) : Serializable