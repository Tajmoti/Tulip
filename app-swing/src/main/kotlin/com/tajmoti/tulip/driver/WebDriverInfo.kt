package com.tajmoti.tulip.driver

import com.tajmoti.tulip.setup.OSInfo
import org.openqa.selenium.WebDriver

data class WebDriverInfo(
    val driverInfo: DriverFileInfo?,
    val setupMethod: (osInfo: OSInfo, blockImages: Boolean) -> WebDriver
)