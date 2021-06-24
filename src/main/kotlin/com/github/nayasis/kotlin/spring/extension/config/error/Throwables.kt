package com.github.nayasis.kotlin.spring.extension.config.error

import com.github.nayasis.kotlin.basica.exception.filterStackTrace
import com.github.nayasis.kotlin.basica.exception.toString
import mu.KotlinLogging
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger{}

@Component
class Throwables(
    @Value("\${server.error.filter:}")
    private var errorFilter: String = ""
): ApplicationContextAware {

    private var filter: Regex? = null

    fun toString(throwable: Throwable?): String {
        return throwable?.toString(filter) ?: ""
    }

    fun logError(throwable: Throwable?) {
        throwable?.filterStackTrace(filter)?.let { log.error(it.message, it) }
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        filter = errorFilter.replace("\n", "|").toRegex()
    }

}