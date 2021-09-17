package com.tajmoti.libwebdriver

interface WebDriver {

    /**
     * Loads the [url] into a real web browser; once the page load
     * callback is called, returns the HTML of the entire page.
     */
    suspend fun getPageHtml(url: String, params: Params): Result<String>

    data class Params(
        /**
         * Specifies when the HTML will be returned.
         */
        val submitTrigger: SubmitTrigger = SubmitTrigger.OnPageLoaded,
        /**
         * Timeout after which the loading is aborted.
         */
        val timeoutMs: Long = 30_000,
        /**
         * Accepts or reject URLs, see [UrlFilter] doc.
         */
        val urlFilter: UrlFilter? = null,
        /**
         * Whether to disable loading of all images.
         */
        val blockImages: Boolean = true,
    )

    sealed interface SubmitTrigger {
        /**
         * Generator of custom JS code that will be executed once the page is loaded
         * into the browser. This custom code is responsible for calling $it.submitHtml()
         * once the page is in its final state and its HTML source is ready to be submitted.
         * $it is the parameter supplied to the generator the name of the interface
         * that will be used to return the result.
         */
        val jsGenerator: (String) -> String

        /**
         * Entire page HTML will be returned as soon as the page is initially loaded.
         * Note that all JS might not have run so the page might not be in its final state.
         */
        object OnPageLoaded : SubmitTrigger {
            override val jsGenerator: (String) -> String
                get() = { "$it.submitHtml();" }
        }

        /**
         * Custom code will determine when the HTML will be returned.
         * This code must call "$it.submitHtml()" exactly once.
         * It may call "$it.logPrint(String)" to print messages into the log.
         *
         * The parameter of the [jsGenerator] function is the name of the object
         * on which the submitHtml() method needs to be called.
         */
        class CustomJs(override val jsGenerator: (interfaceName: String) -> String) : SubmitTrigger
    }
}