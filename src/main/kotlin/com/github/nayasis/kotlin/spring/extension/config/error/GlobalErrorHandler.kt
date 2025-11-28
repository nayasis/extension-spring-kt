package com.github.nayasis.kotlin.spring.extension.config.error

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeAttribute
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.error.ErrorAttributeOptions.Include
import org.springframework.boot.web.error.ErrorAttributeOptions.of
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest

private val logger = KotlinLogging.logger {}

@ControllerAdvice
open class GlobalErrorHandler(
    private val serverProperties: ServerProperties,
): ApplicationContextAware {

    private val ERROR_INTERNAL_ATTRIBUTE: String = DefaultErrorAttributes::class.java.getName() + ".ERROR"

    override fun setApplicationContext(context: ApplicationContext) {
        val errorFilter = context.environment.getProperty("server.error.filter", "").takeIf { it.isNotEmpty() }
        filter = errorFilter?.replace("\n", "|")?.toRegex()
    }

    private var filter: Regex? = null

    private val errorHandler = DefaultErrorAttributes()

    @ExceptionHandler(Throwable::class)
    fun defaultExceptionHandler(
        throwable: Throwable,
        request: WebRequest,
    ): ResponseEntity<Map<String, Any>> {

        val error = if(filter != null) {
            filter(throwable).also {
                request.setAttribute(ERROR_INTERNAL_ATTRIBUTE, it, RequestAttributes.SCOPE_REQUEST)
            }
        } else throwable

        logger.error(error) { error.message ?: "Internal Server Error" }

        val attributes = toErrorAttributes(request)
        val status     = attributes.get("status") as Int

        return ResponseEntity.status(status).body(attributes)
    }

    private fun toErrorAttributes(request: WebRequest): Map<String, Any> {
        return errorHandler.getErrorAttributes(request, determineOptions(request))
    }

    private fun determineOptions(request: WebRequest): ErrorAttributeOptions {
        val errorProperties = serverProperties.error
        return of(buildList {
            if (errorProperties.isIncludeException)
                add(Include.EXCEPTION)
            if(errorProperties.includeStacktrace.isActive(request, "trace"))
                add(Include.STACK_TRACE)
            if(errorProperties.includeMessage.isActive(request, "message"))
                add(Include.MESSAGE)
            if((errorProperties.includeBindingErrors.isActive(request, "errors")))
                add(Include.BINDING_ERRORS)
        })
    }

    private fun IncludeAttribute.isActive(request: WebRequest, param: String): Boolean {
        return when (this) {
            IncludeAttribute.ALWAYS -> true
            IncludeAttribute.ON_PARAM -> request.getParameter(param)
                ?.let { it.isEmpty() || it == "true" } ?: false
            IncludeAttribute.NEVER -> false
        }
    }

    private fun filter(throwable: Throwable): Throwable {
        val stacktrace = throwable.stackTrace.filter { element ->
            filter!!.find(element.toString()) != null
        }.toTypedArray()

        val cause = throwable.cause?.let { filter(it) }

        return Throwable(throwable.message, cause).apply {
            setStackTrace(stacktrace)
            throwable.suppressed.forEach { suppressed ->
                addSuppressed(filter(suppressed))
            }
        }
    }

}