package io.github.nayasis.kotlin.spring.extension.config.error

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView

private val logger = KotlinLogging.logger {}

@Component
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnExpression($$"'${server.error.global.enabled:true}' == 'true'")
class GlobalErrorLogger: HandlerExceptionResolver, ApplicationContextAware {

    private val ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes::class.java.getName() + ".ERROR"

    private var errorFilter: Regex? = null

    override fun setApplicationContext(context: ApplicationContext) {
        context.environment.getProperty("server.error.filter", "")
            .takeIf { it.isNotEmpty() }
            ?.replace("\n", "|")
            ?.toRegex()
            ?.let { errorFilter = it }
    }

    override fun resolveException(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?,
        e: Exception
    ): ModelAndView? {
        if(errorFilter != null) {
            filter(e)
            request.setAttribute(ERROR_INTERNAL_ATTRIBUTE, e)
        }
        logger.error(e) { e.message ?: "Internal Server Error" }
        return null
    }

    private fun filter(exception: Throwable?) {
        if(errorFilter == null || exception == null) return
        val filtered = exception.stackTrace.filter { element ->
            errorFilter!!.find(element.toString()) != null
        }.toTypedArray()

        exception.stackTrace = filtered
        filter(exception.cause)
    }

}