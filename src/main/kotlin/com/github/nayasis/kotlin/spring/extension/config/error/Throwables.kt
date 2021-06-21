package com.github.nayasis.kotlin.spring.extension.config.error

import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
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

    fun filterStackTrace(throwable: Throwable?): Throwable {
        val clone = Throwable(throwable!!.message)
        val list = ArrayList<StackTraceElement>()
        for (e in throwable.stackTrace) {
            if (!errorFilter!!.isEmpty() && errorFilter.toRegex().find(e.toString()) ) continue
            list.add(e)
        }
        clone.stackTrace = list.toArray<StackTraceElement>(arrayOf<StackTraceElement>())
        if (throwable.cause != null) {
            val cause = filterStackTrace(throwable.cause)
            clone.initCause(cause)
        }
        return clone
    }

    fun toString(throwable: Throwable?): String {
        if (throwable == null) return ""
        val proxy = ThrowableProxy(filterStackTrace(throwable))
        proxy.calculatePackagingData()
        return ThrowableProxyUtil.asString(proxy)
    }

    fun logError(throwable: Throwable?) {
        val e = filterStackTrace(throwable)
        log.error(e.message, e)
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        errorFilter = errorFilter!!.replace("\n", "|")
    }
}
