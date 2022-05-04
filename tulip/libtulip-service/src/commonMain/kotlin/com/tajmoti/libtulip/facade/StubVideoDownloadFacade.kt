package com.tajmoti.libtulip.facade

import com.tajmoti.libtulip.facade.VideoDownloadFacade
import com.tajmoti.libtulip.model.info.StreamableInfo

class StubVideoDownloadFacade : VideoDownloadFacade {
    override fun downloadFileToFiles(videoUrl: String, info: StreamableInfo): Long {
        return 0
    }
}