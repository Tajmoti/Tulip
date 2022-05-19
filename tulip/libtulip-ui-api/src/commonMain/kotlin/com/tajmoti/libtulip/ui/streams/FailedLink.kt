package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.dto.CaptchaInfoDto
import com.tajmoti.libtulip.dto.LanguageCodeDto
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto

/**
 * Direct link loading failed for this stream.
 */
data class FailedLink(
    val stream: StreamingSiteLinkDto,
    val languageCode: LanguageCodeDto,
    val download: Boolean,
    /**
     * If not null, this failed because of a captcha.
     */
    val captchaInfo: CaptchaInfoDto?
)