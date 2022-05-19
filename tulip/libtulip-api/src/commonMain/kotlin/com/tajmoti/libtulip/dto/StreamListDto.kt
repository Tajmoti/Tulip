package com.tajmoti.libtulip.dto

sealed interface StreamListDto {
    data class Success(
        val streams: List<StreamingSiteLinkDto>,
        /**
         * If true, no more streams might be loaded.
         * In this case, an error should be shown if no streams are available.
         */
        val possiblyFinished: Boolean
    ) : StreamListDto

    object Error : StreamListDto
}
