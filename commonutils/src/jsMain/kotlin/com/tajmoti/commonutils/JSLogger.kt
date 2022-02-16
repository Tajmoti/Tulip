package com.tajmoti.commonutils

import mu.KLogger
import mu.Marker

class JSLogger : KLogger {
    companion object {
        val INSTANCE by lazy { JSLogger() }
    }

    override fun <T : Throwable> catching(throwable: T) {

    }

    override fun debug(msg: () -> Any?) {
        console.log(msg())
    }

    override fun debug(t: Throwable?, msg: () -> Any?) {
        console.log("${msg()} ${t?.stackTraceToString()}")
    }

    override fun debug(marker: Marker?, msg: () -> Any?) {
        console.log(msg())
    }

    override fun debug(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        console.log(msg())
    }

    override fun entry(vararg argArray: Any?) {

    }

    override fun error(msg: () -> Any?) {
        console.error(msg())
    }

    override fun error(t: Throwable?, msg: () -> Any?) {
        console.error("${msg()} ${t?.stackTraceToString()}")
    }

    override fun error(marker: Marker?, msg: () -> Any?) {
        console.error(msg())
    }

    override fun error(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        console.error(msg())
    }

    override fun exit() {

    }

    override fun <T> exit(result: T): T {
        return result
    }

    override fun info(msg: () -> Any?) {
        console.info(msg())
    }

    override fun info(t: Throwable?, msg: () -> Any?) {
        console.info("${msg()} ${t?.stackTraceToString()}")
    }

    override fun info(marker: Marker?, msg: () -> Any?) {
        console.info(msg())
    }

    override fun info(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        console.info(msg())
    }

    override fun <T : Throwable> throwing(throwable: T): T {
        return throwable
    }

    override fun trace(msg: () -> Any?) {
        console.log(msg())
    }

    override fun trace(t: Throwable?, msg: () -> Any?) {
        console.log("${msg()} ${t?.stackTraceToString()}")
    }

    override fun trace(marker: Marker?, msg: () -> Any?) {
        console.log(msg())
    }

    override fun trace(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        console.log(msg())
    }

    override fun warn(msg: () -> Any?) {
        console.warn(msg())
    }

    override fun warn(t: Throwable?, msg: () -> Any?) {
        console.warn("${msg()} ${t?.stackTraceToString()}")
    }

    override fun warn(marker: Marker?, msg: () -> Any?) {
        console.warn(msg())
    }

    override fun warn(marker: Marker?, t: Throwable?, msg: () -> Any?) {
        console.warn(msg())
    }
}