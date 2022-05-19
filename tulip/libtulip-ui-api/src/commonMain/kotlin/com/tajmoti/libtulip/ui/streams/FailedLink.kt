package com.tajmoti.libtulip.ui.streams

import com.tajmoti.libtulip.dto.CaptchaInfoDto
import com.tajmoti.libtulip.dto.StreamingSiteLinkDto
import com.tajmoti.libtulip.model.info.LanguageCode

/**
 * Direct link loading failed for this stream.
 */
data class FailedLink(
    val stream: StreamingSiteLinkDto,
    val languageCode: LanguageCode,
    val download: Boolean,
    /**
     * If not null, this failed because of a captcha.
     */
    val captchaInfo: CaptchaInfoDto?
)