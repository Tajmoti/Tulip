package com.tajmoti.tulip.driver

import com.tajmoti.tulip.setup.OSBitness
import com.tajmoti.tulip.setup.OSBrand
import com.tajmoti.tulip.setup.OSInfo
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxDriverLogLevel
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.htmlunit.HtmlUnitDriver

private const val HEADLESS = true

private const val GECKODRIVER_URL_PREFIX =
    "https://github.com/mozilla/geckodriver/releases/download/v0.30.0/geckodriver-v0.30.0-"
private const val GECKODRIVER_PATH =
    "geckodriver"
private const val CHROMEDRIVER_URL_PREFIX =
    "https://chromedriver.storage.googleapis.com/96.0.4664.18/chromedriver_"
private const val CHROMEDRIVER_PATH =
    "chromedriver"

private val CHROMEDRIVER_INFO = WebDriverInfo(DriverFileInfo(CHROMEDRIVER_PATH)
{ getChromedriverUrl(it) }) { os, block -> makeChromeDriver(os, block) }

private val GECKODRIVER_INFO = WebDriverInfo(DriverFileInfo(GECKODRIVER_PATH)
{ getGeckodriverUrl(it) }) { os, block -> makeFirefoxDriver(os, block) }

private val HTMLUNIT_INFO = WebDriverInfo(null) { _, _ -> makeHtmlUnitDriver() }

val SUPPORTED_DRIVERS = listOf(CHROMEDRIVER_INFO, GECKODRIVER_INFO, HTMLUNIT_INFO)

private fun makeHtmlUnitDriver(): WebDriver {
    return HtmlUnitDriver(true)
}

private fun makeChromeDriver(os: OSInfo, blockImages: Boolean): WebDriver {
    val executable = CHROMEDRIVER_INFO.driverInfo!!.getFileName(os)
    System.setProperty("webdriver.chrome.driver", executable)
    val options = ChromeOptions()
    options.setHeadless(HEADLESS)
    if (blockImages) {
        val images = HashMap<String, Any>()
        images["images"] = 2
        val prefs = HashMap<String, Any>()
        prefs["profile.default_content_setting_values"] = images
        options.setExperimentalOption("prefs", prefs)
    }
    val service = ChromeDriverService.Builder()
        .withSilent(true)
        .build()
    return ChromeDriver(service, options)
}

private fun makeFirefoxDriver(os: OSInfo, blockImages: Boolean): WebDriver {
    val executable = GECKODRIVER_INFO.driverInfo!!.getFileName(os)
    System.setProperty("webdriver.gecko.driver", executable)
    val options = FirefoxOptions()
        .setLogLevel(FirefoxDriverLogLevel.FATAL)
    options.setHeadless(HEADLESS)
    if (blockImages) {
        val profile = FirefoxProfile()
        profile.setPreference("permissions.default.image", 2)
        options.profile = profile
    }
    return FirefoxDriver(options)
}

private fun getChromedriverUrl(osInfo: OSInfo): String {
    val osName = when (osInfo.brand) {
        OSBrand.WINDOWS -> "win"
        OSBrand.LINUX -> "linux"
    }
    val bitness = when (osInfo.bits) {
        OSBitness.X32 -> "32"
        OSBitness.X64 -> "64"
    }
    if (osInfo.bits == OSBitness.X32 && osInfo.brand == OSBrand.LINUX)
        throw IllegalStateException("32 bit Linux is not supported")
    return "$CHROMEDRIVER_URL_PREFIX$osName$bitness.zip"
}

private fun getGeckodriverUrl(osInfo: OSInfo): String {
    val (osName, extension) = when (osInfo.brand) {
        OSBrand.WINDOWS -> "win" to ".zip"
        OSBrand.LINUX -> "linux" to ".tar.gz"
    }
    val bitness = when (osInfo.bits) {
        OSBitness.X32 -> "32"
        OSBitness.X64 -> "64"
    }
    return GECKODRIVER_URL_PREFIX + osName + bitness + extension
}