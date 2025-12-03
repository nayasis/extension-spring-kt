package com.github.nayasis.kotlin.spring.extension.config.message

import io.github.nayasis.kotlin.basica.core.string.bind
import io.github.nayasis.kotlin.basica.model.Messages
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import java.util.*

open class CustomMessageSource: MessageSource {

    override fun getMessage(code: String, args: Array<out Any>?, defaultMessage: String?, locale: Locale?): String? {
        return Messages[locale, code].bind(*(args ?: emptyArray()))
    }

    override fun getMessage(code: String, args: Array<out Any>?, locale: Locale?): String {
        return getMessage(code, args, null, locale) ?: code
    }

    override fun getMessage(resolvable: MessageSourceResolvable, locale: Locale?): String {
        val codes = resolvable.codes
        val args  = resolvable.arguments ?: emptyArray()
        return if (codes != null && codes.isNotEmpty()) {
            Messages[locale, codes[0]].bind(*(args))
        } else {
            resolvable.defaultMessage ?: ""
        }
    }

}