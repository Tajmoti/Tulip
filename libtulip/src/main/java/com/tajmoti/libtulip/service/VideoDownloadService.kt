package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.info.StreamableInfo

interface VideoDownloadService {

    fun downloadFileToFiles(videoUrl: String, info: StreamableInfo): Long
}