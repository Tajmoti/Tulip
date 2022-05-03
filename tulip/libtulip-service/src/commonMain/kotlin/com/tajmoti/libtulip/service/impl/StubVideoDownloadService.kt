package com.tajmoti.libtulip.service.impl

import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.service.VideoDownloadService

class StubVideoDownloadService : VideoDownloadService {
    override fun downloadFileToFiles(videoUrl: String, info: StreamableInfo): Long {
        return 0
    }
}