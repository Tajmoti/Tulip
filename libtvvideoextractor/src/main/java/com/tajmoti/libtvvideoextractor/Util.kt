package com.tajmoti.libtvvideoextractor

typealias PageSourceLoader = suspend (url: String, count: Int, urlBlocker: (String) -> Boolean) -> String