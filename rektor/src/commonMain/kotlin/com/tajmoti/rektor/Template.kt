package com.tajmoti.rektor

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class Template<T : Any>(
    val method: Method,
    val url: String,
    val clazz: KClass<T>,
    val type: KType
) {

    companion object {
        inline operator fun <reified T : Any> invoke(method: Method, url: String): Template<T> {
            return Template(method, url, T::class, typeOf<T>())
        }

        inline fun <reified T : Any> get(url: String): Template<T> {
            return Template(Method.GET, url, T::class, typeOf<T>())
        }
    }
}