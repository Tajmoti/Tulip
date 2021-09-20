package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtvprovider.VideoStreamRef
import com.tajmoti.libtvvideoextractor.CaptchaInfo

/**
 * Direct link loading failed for this stream.
 */
data class FailedLink(
    val stream: VideoStreamRef,
    val download: Boolean,
    /**
     * If not null, this failed because of a captcha.
     */
    val captchaInfo: CaptchaInfo?
)