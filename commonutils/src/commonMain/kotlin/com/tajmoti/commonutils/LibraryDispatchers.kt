package com.tajmoti.commonutils

import kotlinx.coroutines.CoroutineDispatcher

expect object LibraryDispatchers {
    val libraryContext: CoroutineDispatcher
}