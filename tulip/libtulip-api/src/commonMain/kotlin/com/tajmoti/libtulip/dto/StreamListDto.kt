package com.tajmoti.libtulip.dto

import com.tajmoti.libtulip.model.stream.UnloadedVideoStreamRef

sealed interface StreamListDto {
    data class Success(
        val streams: List<UnloadedVideoStreamRef>,
        /**
         * If true, no more streams might be loaded.
         * In this case, an error should be shown if no streams are available.
         */
        val possiblyFinished: Boolean
    ) : StreamListDto

    object Error : StreamListDto
}
