package io.github.nayasis.kotlin.spring.extension.config.message

import io.github.nayasis.kotlin.basica.core.string.bind
import io.github.nayasis.kotlin.basica.etc.error
import io.github.nayasis.kotlin.basica.model.Messages
import io.github.nayasis.kotlin.basica.model.Messages.Companion.loadMessages
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.EnvironmentAware
import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnExpression($$"'${spring.messages.path:}' != ''")
class CustomMessageSource: MessageSource, EnvironmentAware {

    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    @PostConstruct
    fun loadExternalMessages() {
        try {
            environment.getRequiredProperty("spring.messages.path")
                .trimEnd('/').plus("/**/*")
                .loadMessages()
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    override fun getMessage(code: String, args: Array<out Any>?, defaultMessage: String?, locale: Locale?): String? {
        return Messages[code, locale ?: Locale.getDefault()].bind(*(args ?: emptyArray()))
    }

    override fun getMessage(code: String, args: Array<out Any>?, locale: Locale?): String {
        return getMessage(code, args, null, locale) ?: code
    }

    override fun getMessage(resolvable: MessageSourceResolvable, locale: Locale?): String {
        val codes = resolvable.codes
        val args  = resolvable.arguments ?: emptyArray()
        return if (codes != null && codes.isNotEmpty()) {
            Messages[codes[0], locale ?: Locale.getDefault() ].bind(*(args))
        } else {
            resolvable.defaultMessage ?: ""
        }
    }

}