package com.github.nayasis.kotlin.spring.extension.config.message

import com.github.nayasis.kotlin.basica.core.extension.ifEmpty
import com.github.nayasis.kotlin.basica.core.extension.ifNotEmpty
import com.github.nayasis.kotlin.basica.core.string.bind
import com.github.nayasis.kotlin.basica.model.Messages
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import java.util.*

open class CustomMessageSource: MessageSource {

    @Suppress("PrivatePropertyName")
    private val EMPTY_ARG = arrayOf<Any>()

    override fun getMessage(code: String, args: Array<Any>?, defaultMessage: String?, locale: Locale): String {
        return Messages[locale, code].bind(*args.ifEmpty {EMPTY_ARG})
    }

    override fun getMessage(code: String, args: Array<Any>?, locale: Locale): String {
        return getMessage(code, args, null, locale)
    }

    override fun getMessage(resolvable: MessageSourceResolvable, locale: Locale): String {
        val codes = resolvable.codes
        val args  = resolvable.arguments.ifNotEmpty {EMPTY_ARG}!!
        return Messages[locale, codes!![0]].bind(*args)
    }

}