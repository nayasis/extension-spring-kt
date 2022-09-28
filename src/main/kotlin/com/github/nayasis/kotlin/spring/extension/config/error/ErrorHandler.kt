package com.github.nayasis.kotlin.spring.extension.config.error

import com.github.nayasis.kotlin.basica.core.extention.ifEmpty
import com.github.nayasis.kotlin.spring.extension.exception.DomainException
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.error.ErrorAttributeOptions.Include
import org.springframework.boot.web.error.ErrorAttributeOptions.Include.*
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

/**
 * Error handler
 *
 * it is only reference. ErrorAttributes must be declared in main project.
 *
 */
@Component
@ConditionalOnMissingBean(ErrorAttributes::class)
class ErrorHandler (
    private val throwables: Throwables
){

    @Bean
    fun errorAttributes(): ErrorAttributes {
        return object : DefaultErrorAttributes() {
            override fun getErrorAttributes(request: WebRequest, options: ErrorAttributeOptions): Map<String,Any> {
                val attributes = super.getErrorAttributes(request, options)
                unwrap( getError(request) )?.let{ throwable ->
                    if (options.isIncluded(EXCEPTION))   attributes["exception"] = throwable.javaClass.name
                    if (options.isIncluded(STACK_TRACE)) attributes["trace"]     = throwables.toString(throwable)
                    attributes["message"] = throwable.message ?: throwable.toString()
                    if( throwable is DomainException ) {
                        attributes["code"]   = throwable.code.ifEmpty { null }
                        attributes["detail"] = throwable.detail.ifEmpty { null }
                    }
                }
                return attributes
            }
        }
    }

    fun toErrorAttribute( exception: Throwable? ): ErrorResponse? {
        return unwrap(exception)?.let {
            return ErrorResponse(
                javaClass.name,
                throwables.toString(it),
                when (it) {
                    is DomainException -> it.code
                    else -> null
                },
                it.message ?: it.toString(),
                if( it is DomainException ) it.detail else null
            )
        }
    }

    fun unwrap(throwable: Throwable?): Throwable? =
        with(throwable) {
            when (this?.cause) {
                is DomainException -> this.cause
                else -> this
            }
        }

}

data class ErrorResponse(
    val exception: String? = null,
    val trace: String?     = null,
    val code: String?      = null,
    val message: String?   = null,
    val detail: String?    = null,
)