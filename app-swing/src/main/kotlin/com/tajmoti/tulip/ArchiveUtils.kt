package com.tajmoti.tulip

import org.rauschig.jarchivelib.Archiver
import org.rauschig.jarchivelib.ArchiverFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun unpackArchiveInPlace(archive: File) {
    val dst = File(".")
    when {
        archive.name.endsWith(".tar.gz") ->
            unpackTarGzFile(archive, dst)
        archive.name.endsWith(".zip") ->
            unpackZipFile(archive, dst)
        else ->
            throw IllegalArgumentException("Unknown archive type $archive")
    }
}

fun unpackZipFile(zipFile: File, dstDir: File) {
    ZipInputStream(FileInputStream(zipFile)).use { zis ->
        var zipEntry: ZipEntry? = zis.nextEntry
            ?: throw IllegalStateException("Zip file $zipFile contains no ZIP entries")
        while (zipEntry != null) {
            processZipEntry(dstDir, zipEntry, zis)
            zipEntry = zis.nextEntry
        }
        zis.closeEntry()
    }
}

fun unpackTarGzFile(archive: File, dstDir: File) {
    val archiver: Archiver = ArchiverFactory.createArchiver("tar", "gz")
    archiver.extract(archive, dstDir)
}

private fun processZipEntry(destDir: File, zipEntry: ZipEntry, zis: ZipInputStream) {
    val newFile: File = newFile(destDir, zipEntry)
    if (zipEntry.isDirectory) {
        if (!newFile.isDirectory && !newFile.mkdirs()) {
            throw IOException("Failed to create directory $newFile")
        }
    } else {
        // fix for Windows-created archives
        val parent = newFile.parentFile
        if (!parent.isDirectory && !parent.mkdirs()) {
            throw IOException("Failed to create directory $parent")
        }
        FileOutputStream(newFile).use { fos ->
            zis.copyTo(fos)
        }
    }
}

fun newFile(destinationDir: File, zipEntry: ZipEntry): File {
    val destFile = File(destinationDir, zipEntry.name)
    val destDirPath = destinationDir.canonicalPath
    val destFilePath = destFile.canonicalPath
    if (!destFilePath.startsWith(destDirPath + File.separator)) {
        throw IOException("Entry is outside of the target dir: " + zipEntry.name)
    }
    return destFile
}