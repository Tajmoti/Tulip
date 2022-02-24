package com.tajmoti.libtulip.model.info

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class LanguageCode(val code: String)