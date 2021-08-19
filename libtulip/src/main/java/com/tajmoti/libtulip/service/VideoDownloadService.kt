package com.tajmoti.libtulip.service

import com.tajmoti.libtulip.model.StreamableInfo

interface VideoDownloadService {

    fun downloadFileToFiles(videoUrl: String, info: StreamableInfo): Long
}