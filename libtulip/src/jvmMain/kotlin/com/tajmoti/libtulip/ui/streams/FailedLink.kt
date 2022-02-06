package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.model.info.LanguageCode
import com.tajmoti.libtvprovider.model.VideoStreamRef
import com.tajmoti.libtvvideoextractor.CaptchaInfo

/**
 * Direct link loading failed for this stream.
 */
data class FailedLink(
    val stream: VideoStreamRef,
    val languageCode: LanguageCode,
    val download: Boolean,
    /**
     * If not null, this failed because of a captcha.
     */
    val captchaInfo: CaptchaInfo?
)