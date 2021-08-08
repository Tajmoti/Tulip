package com.tajmoti.tulip.service.impl

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.tajmoti.tulip.R
import com.tajmoti.tulip.model.StreamableInfo
import com.tajmoti.tulip.service.VideoDownloadService
import java.io.File

class DownloadManagerVideoDownloadService(
    val context: Context
) : VideoDownloadService {
    companion object {
        private const val SEASON_DIRECTORY_NAME = "Season"
        private const val FILE_EXTENSION = ".mp4"
    }

    override fun downloadFileToFiles(uri: Uri, info: StreamableInfo): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val displayName = buildDisplayName(info)
        val savePath = getSavePath(info)
        val request = DownloadManager.Request(uri)
            .setTitle(displayName)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, savePath)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .apply { allowScanningByMediaScanner() }
        return downloadManager.enqueue(request)
    }

    private fun buildDisplayName(item: StreamableInfo): String {
        return when (item) {
            is StreamableInfo.TvShow -> showToDownloadName(item)
            is StreamableInfo.Movie -> item.movie.name
        }
    }

    private fun showToDownloadName(item: StreamableInfo.TvShow): String {
        val episode = item.episode.number
        val prefix = "${item.show.name} S${pad(item.season.number)}"
        val name = if (episode != null) {
            "E${pad(episode)}"
        } else {
            item.episode.name!!
        }
        return prefix + name
    }

    private fun getSavePath(item: StreamableInfo): String {
        val path = when (item) {
            is StreamableInfo.TvShow -> showToSavePath(item)
            is StreamableInfo.Movie -> normalize(item.movie.name)
        }
        return context.getString(R.string.app_name) + File.separator + path + FILE_EXTENSION
    }

    private fun showToSavePath(item: StreamableInfo.TvShow): String {
        val sep = File.separator
        val ss = pad(item.season.number)
        val ep = item.episode.number
        val prefix = "${normalize(item.show.name)}$sep$SEASON_DIRECTORY_NAME $ss$sep"
        val epName = if (ep != null) {
            var fileName = pad(ep)
            val epName = item.episode.name
            if (epName != null)
                fileName += " - $epName"
            fileName
        } else {
            normalize(item.episode.name!!)
        }
        return prefix + epName
    }

    private fun normalize(name: String): String {
        return name.replace(File.separator, "").replace(File.pathSeparator, "-")
    }

    private fun pad(name: Int): String {
        return name.toString().padStart(2, '0')
    }
}