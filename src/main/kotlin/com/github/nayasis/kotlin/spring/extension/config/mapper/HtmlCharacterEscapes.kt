package com.github.nayasis.kotlin.spring.extension.config.mapper

import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.io.CharacterEscapes
import com.fasterxml.jackson.core.io.SerializedString
import org.apache.commons.text.StringEscapeUtils

open class HtmlCharacterEscapes: CharacterEscapes() {

    private val asciiEscapes = standardAsciiEscapesForJSON()

    private fun addXssCharacters() {
        asciiEscapes['<'.toInt()] = ESCAPE_CUSTOM
        asciiEscapes['>'.toInt()] = ESCAPE_CUSTOM
        asciiEscapes['\"'.toInt()] = ESCAPE_CUSTOM
        asciiEscapes['('.toInt()] = ESCAPE_CUSTOM
        asciiEscapes[')'.toInt()] = ESCAPE_CUSTOM
        asciiEscapes['#'.toInt()] = ESCAPE_CUSTOM
        asciiEscapes['\''.toInt()] = ESCAPE_CUSTOM
    }

    override fun getEscapeCodesForAscii(): IntArray {
        return asciiEscapes
    }

    override fun getEscapeSequence(ch: Int): SerializableString {
        return SerializedString(
            StringEscapeUtils.escapeHtml4(Character.toString(ch.toChar()))
        )
    }

    init {
        addXssCharacters()
    }
}