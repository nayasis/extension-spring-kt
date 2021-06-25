package com.github.nayasis.kotlin.spring.extension.servlet

import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class CashedServletRequest(request: HttpServletRequest): HttpServletRequestWrapper(request) {

    private var cachedBody: ByteArray = StreamUtils.copyToByteArray(request.inputStream)

    override fun getInputStream(): ServletInputStream {
        return CachedBodyServletInputStream(cachedBody)
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(ByteArrayInputStream(cachedBody)))
    }
}

class CachedBodyServletInputStream(body: ByteArray): ServletInputStream() {

    private var instream: InputStream = ByteArrayInputStream(body)

    override fun read(): Int = instream.read()

    override fun available(): Int = instream.available()

    override fun isFinished(): Boolean = available() == 0

    override fun isReady(): Boolean = true

    override fun setReadListener(p0: ReadListener?) {
        TODO("Not yet implemented")
    }

}