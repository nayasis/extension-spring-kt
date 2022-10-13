package com.github.nayasis.kotlin.spring.extension.exception

import com.github.nayasis.kotlin.basica.model.Messages
import org.springframework.context.i18n.LocaleContextHolder

class DomainCodeException: DomainException {
    constructor(code: String): super(getMessage(code)) {
        this.code = code
    }
    constructor(code: String, message: String?): super(message) {
        this.code = code
    }
    constructor(code: String, cause: Throwable?): super(getMessage(code), cause) {
        this.code = code
    }
    constructor(code: String, message: String?, cause: Throwable?): super(message, cause) {
        this.code = code
    }
    constructor(code: String, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean): super(getMessage(code),cause,enableSuppression,writableStackTrace) {
        this.code = code
    }
    constructor(code: String, message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean): super(message,cause,enableSuppression,writableStackTrace) {
        this.code = code
    }
}

private fun getMessage(code: String?): String {
    return Messages[LocaleContextHolder.getLocale(), code]
}