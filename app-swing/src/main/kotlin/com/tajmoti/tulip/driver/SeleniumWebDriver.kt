package com.tajmoti.tulip.driver

import com.tajmoti.commonutils.logger
import com.tajmoti.libwebdriver.TulipWebDriver
import com.tajmoti.tulip.getOsInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import java.util.logging.Level

class SeleniumWebDriver : TulipWebDriver {

    override suspend fun getPageHtml(url: String, params: TulipWebDriver.Params): Result<String> {
        return withContext(Dispatchers.IO) { getPageHtmlOnIo(params, url) }
    }

    private fun getPageHtmlOnIo(params: TulipWebDriver.Params, url: String): Result<String> {
        logger.debug("Creating WebDriver")
        disableWebDriverLogging()
        val driver = getSupportedDriver(params.blockImages)
        return try {
            extractHtml(driver, url, params)
        } catch (e: Exception) {
            logger.warn("WebDriver failed with", e)
            Result.failure(e)
        } finally {
            driver.quit()
        }
    }

    private fun getSupportedDriver(blockImages: Boolean): WebDriver {
        val osInfo = getOsInfo()
        for ((_, initializer) in SUPPORTED_DRIVERS) {
            try {
                val driver = initializer(osInfo, blockImages)
                logger.debug("Utilizing WebDriver $driver")
                return driver
            } catch (t: Throwable) {
                val relevantLines = t.message
                    ?.lineSequence()
                    ?.take(4)
                    ?.joinToString("\n")
                    .toString()
                logger.warn("Exception while initializing driver: {}", relevantLines)
            }
        }
        throw IllegalStateException("Initialization of all drivers failed!")
    }

    private fun extractHtml(d: WebDriver, url: String, p: TulipWebDriver.Params): Result<String> {
        logger.debug("Setting timeouts")
        d.manage().timeouts().implicitlyWait(p.timeoutMs, TimeUnit.MILLISECONDS)
        logger.debug("Performing GET of $url")
        d.get(url)
        logger.debug("Extracting HTML of $url")
        return when (p.submitTrigger) {
            is TulipWebDriver.SubmitTrigger.OnPageLoaded -> Result.success(d.pageSource)
            is TulipWebDriver.SubmitTrigger.CustomJs -> extractHtmlUsingJs(
                d,
                p.submitTrigger as TulipWebDriver.SubmitTrigger.CustomJs
            )
        }
    }

    private fun extractHtmlUsingJs(
        driver: WebDriver,
        p: TulipWebDriver.SubmitTrigger.CustomJs,
    ): Result<String> {
        if (driver !is JavascriptExecutor)
            return Result.failure(IllegalStateException("Driver is not a JS executor"))
        logger.debug("Executing provided JS")
        val customJs = p.jsGenerator("window.webDriverCallback")
        val html = driver.executeAsyncScript("$JS_SETUP\n$customJs") as String
        logger.debug("Returning generated HTML of length ${html.length}")
        return Result.success(html)
    }

    private fun disableWebDriverLogging() {
        System.setProperty("webdriver.chrome.silentOutput", "true")
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null")
        java.util.logging.Logger.getLogger("org.openqa.selenium").level = Level.OFF
    }

    companion object {

        private const val JS_SETUP =
            """
            var callback = arguments[arguments.length - 1];
            window.webDriverCallback = new Object();
            window.webDriverCallback.submitHtml = function(o) {
                callback(document.documentElement.outerHTML);
            };
            window.webDriverCallback.logPrint = function(m){};
            """
    }
}