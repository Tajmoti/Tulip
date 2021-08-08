package com.tajmoti.tulip.service

import android.net.Uri
import com.tajmoti.tulip.model.StreamableInfo

interface VideoDownloadService {

    fun downloadFileToFiles(uri: Uri, info: StreamableInfo): Long
}