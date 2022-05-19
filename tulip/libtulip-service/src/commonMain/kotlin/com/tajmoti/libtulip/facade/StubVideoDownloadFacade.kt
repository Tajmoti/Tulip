package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.dto.StreamableInfoDto

class StubVideoDownloadFacade : VideoDownloadFacade {
    override fun downloadFileToFiles(videoUrl: String, info: StreamableInfoDto): Long {
        return 0
    }
}