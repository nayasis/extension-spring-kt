package com.github.nayasis.kotlin.spring.extension.config.message

import com.github.nayasis.kotlin.basica.core.string.bind
import com.github.nayasis.kotlin.basica.core.validator.nvl
import com.github.nayasis.kotlin.basica.model.Messages
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import java.util.*

class CustomMessageSource: MessageSource {
    private val EMPTY_ARGS = arrayOf<Any>()
    override fun getMessage(code: String, args: Array<Any>?, defaultMessage: String?, locale: Locale): String? {
        return Messages.get(locale, code).bind(*nvl(args, EMPTY_ARGS))
    }

    @Throws(NoSuchMessageException::class)
    override fun getMessage(code: String, args: Array<Any>?, locale: Locale): String {
        return getMessage(code, args, null, locale)!!
    }

    @Throws(NoSuchMessageException::class)
    override fun getMessage(resolvable: MessageSourceResolvable, locale: Locale): String {
        val codes = resolvable.codes
        val args: Array<Any> = nvl(resolvable.arguments, EMPTY_ARGS)
        return Messages.get(locale, codes!![0]).bind(*args)
    }
}