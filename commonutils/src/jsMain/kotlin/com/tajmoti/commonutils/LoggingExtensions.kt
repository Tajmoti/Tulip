package com.tajmoti.commonutils

import mu.KLogger

/**
 * Access (or create) the logger of this class.
 */
actual val Any.logger: KLogger
    get() = JSLogger.INSTANCE