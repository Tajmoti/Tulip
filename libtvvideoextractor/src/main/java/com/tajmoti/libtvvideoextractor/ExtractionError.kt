package com.tajmoti.libtvvideoextractor

sealed interface ExtractionError {
    data class Captcha(val info: CaptchaInfo) : ExtractionError
    data class Exception(val throwable: Throwable) : ExtractionError
    object UnexpectedPageContent: ExtractionError
    object NoHandler: ExtractionError
}