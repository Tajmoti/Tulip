package com.tajmoti.libtulip.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mu.KLogger
import kotlin.reflect.KProperty0

fun logAllFlowValues(
    sourceObject: Any,
    scope: CoroutineScope,
    logger: KLogger,
    ignoreList: List<KProperty0<Flow<*>>> = emptyList()
) {
    val clazz = sourceObject::class.java
    val target = Flow::class.java
    (clazz.declaredFields + clazz.fields).toSet()
        .filter { it.type.isAssignableFrom(target) || target.isAssignableFrom(it.type) }
        .onEach { it.isAccessible = true }
        .map { it.name to it.get(sourceObject) as Flow<*> }
        .filter { (_, flow) -> ignoreList.none { it.get() == flow } }
        .forEach { (name, flow) -> logEach(scope, logger, name, flow) }
}

fun <T> logEach(scope: CoroutineScope, logger: KLogger, name: String, flow: Flow<T>) {
    scope.launch {
        flow.collect {
            logger.debug { "Flowing on [$name]: ${thingToString(it)}" }
        }
    }
}

private fun <T> thingToString(objIn: T?): String {
    val obj = objIn ?: return "null"
    val clazz = obj::class.java
    if (clazz.getMethod("toString").declaringClass != clazz && !clazz.isEnum)
        return clazz.simpleName
    return objIn.toString()
}