package com.tajmoti.tulip.setup

import java.io.File

interface Dependency {
    /**
     * Function returning the destination name of the dependency file.
     */
    fun getFileName(os: OSInfo): String

    /**
     * Function returning the dependency download URL depending on the OS.
     */
    fun getUrl(os: OSInfo): String
    /**
     * Function to transform the downloaded file. null if not required
     */
    val transform: ((src: File, dst: File) -> Unit)?
}
