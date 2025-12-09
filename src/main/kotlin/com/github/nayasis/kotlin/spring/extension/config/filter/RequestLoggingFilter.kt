package com.github.nayasis.kotlin.spring.extension.config.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component
import org.springframework.web.filter.CommonsRequestLoggingFilter

private val log = KotlinLogging.logger{}

@Component
@ConditionalOnExpression($$"'${logging.request.enabled:true}' == 'true'")
class RequestLoggingFilter(
    @Value($$"${logging.request.include-client-info:true}")
    includeClientInfo: Boolean,
    @Value($$"${logging.request.include-headers:false}")
    includeHeaders: Boolean,
    @Value($$"${logging.request.include-payload:true}")
    includePayload: Boolean,
    @Value($$"${logging.request.include-query-string:true}")
    includeQueryString: Boolean,
    @Value($$"${logging.request.max-payload-length:5000}")
    payloadLength: Int
): CommonsRequestLoggingFilter() {

    init {
        setAfterMessagePrefix(">> Request detail\n")
        setAfterMessageSuffix("")
        this.isIncludeClientInfo  = includeClientInfo
        this.isIncludeHeaders     = includeHeaders
        this.isIncludePayload     = includePayload
        this.isIncludeQueryString = includeQueryString
        this.maxPayloadLength     = payloadLength
    }

    override fun shouldLog(request: HttpServletRequest): Boolean {
        return log.isInfoEnabled()
    }

    override fun afterRequest(request: HttpServletRequest, message: String) {
        log.info{ message }
    }

}