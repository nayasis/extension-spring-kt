package com.github.nayasis.kotlin.spring.extension.config.error

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

/**
 * Error handler
 *
 * (it used only reference. ErrorAttributes must be declared in main project.)
 *
 * @author nayasis@gmail.com
 * @since  2016-03-04
 */
@Component
@ConditionalOnMissingBean(ErrorAttributes::class)
class ErrorHandler {
    @Value("\${server.error.include-exception:false}")
    private val includeException = false

    @Autowired
    private val throwHandler: Throwables? = null
    @Bean
    fun errorAttributes(): ErrorAttributes {
        return object: DefaultErrorAttributes() {
            override fun getErrorAttributes(request: WebRequest, includeStackTrace: Boolean): MutableMap<String, Any> {
                val attributes = getErrorAttributes(request, includeStackTrace)
                val error = getError(request)
                if (error != null) {
                    throwHandler.logError(error)
                    if (includeException) attributes["exception"] = error.javaClass.name
                    if (includeStackTrace) attributes["trace"] = throwHandler.toString(error)
                    if (error is DomainException) {
                        val exception: DomainException = error as DomainException
                        if (Strings.isNotEmpty(exception.errorCode())) {
                            attributes["code"] = exception.errorCode()
                        }
                    }
                    attributes["message"] = error.message!!
                }
                return attributes
            }
        }
    }
}
