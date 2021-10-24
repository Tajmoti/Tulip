package com.tajmoti.tulip

import com.tajmoti.tulip.setup.OSBitness
import com.tajmoti.tulip.setup.OSBrand
import com.tajmoti.tulip.setup.OSInfo
import org.apache.commons.lang3.SystemUtils

fun getOsInfo(): OSInfo {
    val brand = getOsBrand()
    val jvmBitness = getOsBitness()
    return OSInfo(brand, jvmBitness)
}

private fun getOsBitness(): OSBitness {
    return if (System.getProperty("os.arch").contains("64")) OSBitness.X64 else OSBitness.X32
}

private fun getOsBrand() = if (SystemUtils.IS_OS_WINDOWS) {
    OSBrand.WINDOWS
} else if (SystemUtils.IS_OS_LINUX) {
    OSBrand.LINUX
} else {
    throw IllegalStateException("Unsupported OS - ${SystemUtils.OS_NAME}")
}