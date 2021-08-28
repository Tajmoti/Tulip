package com.tajmoti.tulip.service.impl

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.service.VideoDownloadService
import com.tajmoti.tulip.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class DownloadManagerVideoDownloadService @Inject constructor(
    @ApplicationContext
    val context: Context
) : VideoDownloadService {
    companion object {
        private const val SEASON_DIRECTORY_NAME = "Season"
        private const val FILE_EXTENSION = ".mp4"
    }

    override fun downloadFileToFiles(videoUrl: String, info: StreamableInfo): Long {
        val uri = Uri.parse(videoUrl)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val displayName = buildDisplayName(info)
        val savePath = getSavePath(info)
        val request = DownloadManager.Request(uri)
            .setTitle(displayName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, savePath)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .apply { allowScanningByMediaScanner() }
        logger.debug("Downloading '{}' as {} to path '{}'", uri, info, savePath)
        return downloadManager.enqueue(request)
    }

    private fun buildDisplayName(item: StreamableInfo): String {
        return when (item) {
            is StreamableInfo.Episode -> showToDownloadName(item)
            is StreamableInfo.Movie -> item.name
        }
    }

    private fun showToDownloadName(item: StreamableInfo.Episode): String {
        val episode = item.info.number
        val prefix = "${item.showName} S${pad(item.seasonNumber)}"
        val name = "E${pad(episode)}"
        return prefix + name
    }

    private fun getSavePath(item: StreamableInfo): String {
        val path = when (item) {
            is StreamableInfo.Episode -> showToSavePath(item)
            is StreamableInfo.Movie -> normalize(item.name)
        }
        return context.getString(R.string.app_name) + File.separator + path + FILE_EXTENSION
    }

    private fun showToSavePath(item: StreamableInfo.Episode): String {
        val sep = File.separator
        val ss = pad(item.seasonNumber)
        val ep = item.info.number
        val prefix = "${normalize(item.showName)}$sep$SEASON_DIRECTORY_NAME $ss$sep"
        var fileName = pad(ep)
        val epName = item.info.name
        if (epName != null)
            fileName += " - $epName"
        return prefix + fileName
    }

    private fun normalize(name: String): String {
        return name.replace(File.separator, "").replace(File.pathSeparator, "-")
    }

    private fun pad(name: Int): String {
        return name.toString().padStart(2, '0')
    }
}