package com.tajmoti.commonutils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object LibraryDispatchers {
    actual val libraryContext: CoroutineDispatcher
        get() = Dispatchers.Default
}