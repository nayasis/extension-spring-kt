package com.github.nayasis.kotlin.spring.extension.exception

class DomainException: RuntimeException {

    var code: String = ""
    var detail: String = ""

    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean): super(message,cause,enableSuppression,writableStackTrace)

}