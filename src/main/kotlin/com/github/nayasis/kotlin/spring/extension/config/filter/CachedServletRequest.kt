package com.github.nayasis.kotlin.spring.extension.config.filter

import org.apache.commons.io.IOUtils
import org.springframework.http.MediaType
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.util.*
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/**
 * Http request wrapper which can read content's inputstream many times.
 */
open class CachedServletRequest(request: HttpServletRequest): HttpServletRequestWrapper(request) {

    private val body = runCatching { IOUtils.toByteArray(request.inputStream) }.getOrNull()
    private val params = request.parameterMap.toMap()

    override fun getInputStream(): ServletInputStream = InputStreamWrapper(body)

    override fun getParameter(name: String): String? = getParameterValues(name)?.get(0)

    override fun getParameterMap(): Map<String, Array<String>> = params

    override fun getParameterNames(): Enumeration<String> = Collections.enumeration(params.keys)

    override fun getParameterValues(name: String): Array<String>? = params[name]?.copyOf()

    override fun getReader(): BufferedReader = BufferedReader(InputStreamReader(inputStream))

    fun hasContentType(vararg type: MediaType): Boolean {
        val contentType = contentType.also { if(it.isNullOrEmpty()) return false }.lowercase()
        return type.firstOrNull{contentType.contains(it.type)} != null
    }

    /**
     * Servlet inputstream wrapper
     */
    private class InputStreamWrapper(body: ByteArray?): ServletInputStream() {
        private val input: ByteArrayInputStream = ByteArrayInputStream(body)
        override fun isFinished(): Boolean = false
        override fun isReady(): Boolean = true
        override fun setReadListener(listener: ReadListener) {}
        override fun read(): Int = input.read()
    }

}