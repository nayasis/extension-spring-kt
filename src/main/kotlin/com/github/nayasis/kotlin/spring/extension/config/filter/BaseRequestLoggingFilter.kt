package com.github.nayasis.kotlin.spring.extension.config.filter

import com.github.nayasis.kotlin.basica.core.collection.toJson
import com.github.nayasis.kotlin.basica.core.collection.toString
import com.github.nayasis.kotlin.spring.extension.servlet.HttpContext
import com.github.nayasis.kotlin.spring.extension.servlet.HttpContext.Companion.hasProfile
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = KotlinLogging.logger {}

open class BaseRequestLoggingFilter: OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val requestWrapper = CachedServletRequest(request)
        logRequest(requestWrapper)
        filterChain.doFilter(requestWrapper, response)
    }

    private fun logRequest(request: HttpServletRequest) {
        if( !log.isInfoEnabled ) return
        log.info { ">> request: ${RequestInfo(request)}" }
        logRequestBody(request)
        logRequestParameter(request)
    }

    private fun logRequestBody(request: HttpServletRequest) {
        if (HttpContext.hasContentType(MediaType.MULTIPART_FORM_DATA, MediaType.MULTIPART_MIXED, MediaType.MULTIPART_RELATED)) return
        try {
            val body = IOUtils.toString(request.inputStream, StandardCharsets.UTF_8)
            if (body.isEmpty()) return
            log.info{">> request body\n${body}"}
        } catch (e: IOException) {
            log.error(e.message, e)
        }
    }

    private fun logRequestParameter(request: HttpServletRequest) {
        val params = request.parameterMap.also { if(it.isEmpty()) return }
            .map { it.key to it.value.joinToString(",") }.toMap()
        log.info{
            if( hasProfile("local") )
                ">> request parameter :\n${params.toString(false)}"
            else
                ">> request parameter : ${params.toJson()}"
        }
    }

}

private data class RequestInfo(
    val ip: String,
    val port: String,
    val protocol: String,
    val method: String,
    val uri: String
) {
    constructor(request: HttpServletRequest): this(
        "${request.remoteAddr}",
        "${request.remotePort}",
        "${request.protocol}",
        "${request.method}",
        "${request.requestURI}",
    )
}