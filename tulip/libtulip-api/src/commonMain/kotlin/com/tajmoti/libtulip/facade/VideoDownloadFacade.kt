package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.model.info.StreamableInfo

interface VideoDownloadFacade {

    fun downloadFileToFiles(videoUrl: String, info: StreamableInfo): Long
}