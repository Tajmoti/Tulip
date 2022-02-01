package com.tajmoti.rektor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TemplaterTest {
    private val builder = Templater


    @Test
    fun `buildUrl() replaces placeholders`() {
        assertEquals("ABC", builder.buildUrl("{cawko}", mapOf("cawko" to "ABC")))
        assertEquals("/root/ABC/", builder.buildUrl("/root/{cawko}/", mapOf("cawko" to "ABC")))
        assertEquals("/ABC/", builder.buildUrl("/{cawko}/", mapOf("cawko" to "ABC")))
        assertEquals("/A/BC/DEF/", builder.buildUrl("/{a}/{b}/{c}/", mapOf("a" to "A", "b" to "BC", "c" to "DEF")))
        assertEquals("/ABC/BC/ABC/", builder.buildUrl("/{a}/{b}/{a}/", mapOf("a" to "ABC", "b" to "BC")))
    }

    @Test
    fun `buildUrl() throws on empty placeholder`() {
        assertFailsWith(IllegalArgumentException::class) { builder.buildUrl("{}", emptyMap()) }
    }

    @Test
    fun `buildUrl() fails on missing value`() {
        assertFailsWith(IllegalArgumentException::class) { builder.buildUrl("{caw}", emptyMap()) }
    }
}