package com.tajmoti.tulip.driver

import com.tajmoti.tulip.setup.Dependency
import com.tajmoti.tulip.setup.OSBrand
import com.tajmoti.tulip.setup.OSInfo
import com.tajmoti.tulip.unpackArchiveInPlace
import java.io.File

data class DriverFileInfo(
    /**
     * Name of the executable contained in the downloaded archive.
     * On Windows, the .exe extension needs to be appended.
     * Use [getFileName] to retrieve the actual name on the provided platform.
     */
    val executableName: String,
    val urlProvider: (OSInfo) -> String,
) : Dependency {

    override fun getFileName(os: OSInfo): String {
        var name = executableName
        if (os.brand == OSBrand.WINDOWS)
            name += ".exe"
        return name
    }

    override fun getUrl(os: OSInfo): String {
        return urlProvider(os)
    }

    override val transform: ((src: File, dst: File) -> Unit) = this::transform

    private fun transform(src: File, dst: File) {
        unpackArchiveInPlace(src)
        dst.setExecutable(true)
    }
}