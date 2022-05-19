package com.tajmoti.libtulip.dto

sealed interface ExtractionErrorDto {
    data class Captcha(val info: CaptchaInfoDto) : ExtractionErrorDto
    data class Exception(val throwable: Throwable) : ExtractionErrorDto
    object UnexpectedPageContent: ExtractionErrorDto
    object NoHandler: ExtractionErrorDto
}