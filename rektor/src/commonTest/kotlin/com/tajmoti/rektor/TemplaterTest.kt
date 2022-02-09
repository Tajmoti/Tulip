package com.tajmoti.rektor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TemplaterTest {
    private val builder = Templater


    @Test
    fun buildUrl_replaces_placeholders() {
        assertEquals("ABC", builder.buildUrl("{cawko}", mapOf("cawko" to "ABC")))
        assertEquals("/root/ABC/", builder.buildUrl("/root/{cawko}/", mapOf("cawko" to "ABC")))
        assertEquals("/ABC/", builder.buildUrl("/{cawko}/", mapOf("cawko" to "ABC")))
        assertEquals("/A/BC/DEF/", builder.buildUrl("/{a}/{b}/{c}/", mapOf("a" to "A", "b" to "BC", "c" to "DEF")))
        assertEquals("/ABC/BC/ABC/", builder.buildUrl("/{a}/{b}/{a}/", mapOf("a" to "ABC", "b" to "BC")))
    }

    @Test
    fun `buildUrl_throws_on_empty_placeholder`() {
        assertFailsWith(IllegalArgumentException::class) { builder.buildUrl("{}", emptyMap()) }
    }

    @Test
    fun `buildUrl_fails_on_missing_value`() {
        assertFailsWith(IllegalArgumentException::class) { builder.buildUrl("{caw}", emptyMap()) }
    }
}