package com.tajmoti.rektor

fun params(vararg pairs: Pair<String, Any?>): Map<String, String> {
    return mapOf(*pairs.mapNotNull { (k, v) -> v?.let { k to v.toString() } }.toTypedArray())
}