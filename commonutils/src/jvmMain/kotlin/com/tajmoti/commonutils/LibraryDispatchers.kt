package com.tajmoti.commonutils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual object LibraryDispatchers {
    actual val libraryContext: CoroutineDispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
}