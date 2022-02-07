package com.tajmoti.tulip.service.impl

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.tajmoti.commonutils.logger
import com.tajmoti.libtulip.model.info.*
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
            .apply { @Suppress("DEPRECATION") allowScanningByMediaScanner() }
        logger.debug { "Downloading '$uri' as $info to path '$savePath'" }
        return downloadManager.enqueue(request)
    }

    private fun buildDisplayName(item: StreamableInfo): String {
        return when (item) {
            is TulipCompleteEpisodeInfo -> showToDownloadName(item)
            is TulipMovie -> item.name
        }
    }

    private fun showToDownloadName(item: TulipCompleteEpisodeInfo): String {
        val episode = item.episodeNumber
        val prefix = "${item.showName} S${pad(item.seasonNumber)}"
        val name = "E${pad(episode)}"
        return prefix + name
    }

    private fun getSavePath(item: StreamableInfo): String {
        val path = when (item) {
            is TulipCompleteEpisodeInfo -> showToSavePath(item)
            is TulipMovie -> normalize(item.name)
        }
        return context.getString(R.string.app_name) + File.separator + path + FILE_EXTENSION
    }

    private fun showToSavePath(item: TulipCompleteEpisodeInfo): String {
        val sep = File.separator
        val ss = pad(item.seasonNumber)
        val ep = item.episodeNumber
        val prefix = "${normalize(item.showName)}$sep$SEASON_DIRECTORY_NAME $ss$sep"
        var fileName = pad(ep)
        val epName = item.name
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