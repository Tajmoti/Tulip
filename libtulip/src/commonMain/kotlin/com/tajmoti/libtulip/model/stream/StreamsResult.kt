package com.tajmoti.libtulip.model.stream

sealed interface StreamsResult {
    data class Success(
        val streams: List<UnloadedVideoStreamRef>,
        /**
         * If true, no more streams might be loaded.
         * In this case, an error should be shown if no streams are available.
         */
        val possiblyFinished: Boolean
    ) : StreamsResult

    object Error : StreamsResult
}
