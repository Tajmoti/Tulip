package com.tajmoti.commonutils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Access (or create) the logger of this class.
 * This is not too slow, implementations cache logger instances.
 */
fun Any.getClassLogger(): Logger {
    return LoggerFactory.getLogger(javaClass)
}

/**
 * Same as [getClassLogger], but as a property.
 */
val Any.logger: Logger
    get() = getClassLogger()