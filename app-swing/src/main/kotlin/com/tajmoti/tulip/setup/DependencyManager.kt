package com.tajmoti.tulip.setup

import com.tajmoti.commonutils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DependencyManager(
    private val os: OSInfo,
    private val dependencies: List<Dependency>
) {

    /**
     * Checks whether all dependencies are downloaded and ready to be used.
     */
    fun allDependenciesPresent(os: OSInfo): Boolean {
        return dependencies
            .map { File(it.getFileName(os)) }
            .all { it.exists() }
    }

    /**
     * Downloads all missing dependencies. The progress is reported from the calling thread to [progress].
     */
    suspend fun downloadMissingDependencies(): Flow<Float> {
        val driversToDownload = dependencies
        val progress = withContext(Dispatchers.IO) {
            processOnIoContext(driversToDownload)
        }
        return progress
    }

    private fun processOnIoContext(driversToDownload: List<Dependency>) = flow {
        val total = driversToDownload.size.toFloat() * 2
        for ((index, info) in driversToDownload.withIndex()) {
            val executable = File(info.getFileName(os))
            logger.info("Processing $executable")
            if (!executable.exists()) {
                val dlName = downloadDependency(info)
                emit(((index * 2) / total))
                transformDependency(info, dlName)
            }
            emit(((index * 2 + 1) / total))
        }
    }

    private fun downloadDependency(dep: Dependency): String {
        val url = dep.getUrl(os)
        val dlName = url.substring(url.lastIndexOf('/') + 1)
        logger.info("Downloading $dlName from $url")
        downloadFileAs(url, dlName)
        return dlName
    }

    private fun transformDependency(dep: Dependency, dlName: String) {
        val transform = dep.transform ?: return
        logger.info("Transforming $dlName")
        transform(File(dlName), File(dep.getFileName(os)))
    }

    private fun downloadFileAs(url: String, localFileName: String) {
        URL(url).openStream().use { inStream ->
            FileOutputStream(localFileName).use { outStream ->
                inStream.copyTo(outStream)
            }
        }
    }
}