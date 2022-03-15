package com.tajmoti.commonutils

import mu.KLogger
import mu.KotlinLogging

/**
 * Access (or create) the logger of this class.
 */
actual val Any.logger: KLogger
    get() = KotlinLogging.logger(this.javaClass.canonicalName)