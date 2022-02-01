package com.tajmoti.rektor

object Templater {

    fun buildUrl(template: String, params: Map<String, String>): String {
        val indices = getPlaceholderIndices(template)
        val replacements = indices
            .map { (start, end) -> getParamForPlaceholder(template, start, end, params) }
            .zip(indices)
        return replaceTemplates(template, replacements)
    }

    private fun replaceTemplates(template: String, replacements: List<Pair<String, Pair<Int, Int>>>): String {
        val remaining = replacements.toMutableList()
        var current = template
        var offset = 0
        while (remaining.isNotEmpty()) {
            val (replacement, range) = remaining.removeFirst()
            val (start, end) = range
            val sizeBefore = current.length
            current = current.replaceAtIndices(start - offset, end - offset, replacement)
            val sizeAfter = current.length
            offset += (sizeBefore - sizeAfter)
        }
        return current
    }

    private fun String.replaceAtIndices(start: Int, end: Int, replacement: String): String {
        return substring(0, start - 1) + replacement + substring(end + 1)
    }

    private fun getPlaceholderIndices(template: String): List<Pair<Int, Int>> {
        val starts = template.indicesOf('{').map { it + 1}.toList()
        val ends = template.indicesOf('}').toList()
        if (starts.size != ends.size) throwBracketException()
        val zipped = starts.zip(ends)
        validatePlaceholders(zipped)
        return zipped
    }

    private fun validatePlaceholders(zipped: List<Pair<Int, Int>>) {
        if (zipped.any { it.first > it.second }) throwBracketException()
        if (zipped.any { it.first == it.second }) throwBracketException("Empty group found")
    }

    private fun getParamForPlaceholder(string: String, start: Int, end: Int, params: Map<String, String>): String {
        val placeholder = string.substring(start, end)
        return params[placeholder] ?: throw IllegalArgumentException("No value for '$placeholder'")
    }

    private fun throwBracketException(message: String? = null) {
        throw IllegalArgumentException(message ?: "Invalid bracket format")
    }

    private fun String.indicesOf(sub: Char) = sequence {
        var index = indexOf(sub)
        while (index >= 0) {
            yield(index)
            index = indexOf(sub, index + 1)
        }
    }
}