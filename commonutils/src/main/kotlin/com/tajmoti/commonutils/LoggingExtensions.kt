package com.tajmoti.commonutils

import mu.KotlinLogging
import mu.KLogger

/**
 * Access (or create) the logger of this class.
 * This is not too slow, implementations cache logger instances.
 */
fun Any.getClassLogger(): KLogger {
    return KotlinLogging.logger {  }
}

/**
 * Same as [getClassLogger], but as a property.
 */
val Any.logger: KLogger
    get() = getClassLogger()