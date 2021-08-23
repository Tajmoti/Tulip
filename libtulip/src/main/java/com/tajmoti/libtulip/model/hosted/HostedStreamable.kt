package com.tajmoti.libtulip.model.hosted

import com.tajmoti.libtulip.model.key.StreamableKey

sealed interface HostedStreamable {
    val hostedKey: StreamableKey.Hosted
}