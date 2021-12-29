package com.tajmoti.commonutils

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object LibraryDispatchers {
    val libraryContext = Executors.newCachedThreadPool().asCoroutineDispatcher()
}