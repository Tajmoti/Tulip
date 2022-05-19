package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.StreamableInfoDto

interface VideoDownloadFacade {

    fun downloadFileToFiles(videoUrl: String, info: StreamableInfoDto): Long
}