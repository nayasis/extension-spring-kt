package com.github.nayasis.kotlin.spring.extension.servlet

import com.github.nayasis.kotlin.basica.core.extension.ifEmpty
import com.github.nayasis.kotlin.basica.core.extension.isEmpty
import com.github.nayasis.kotlin.basica.core.validator.nvl
import com.github.nayasis.kotlin.basica.thread.ThreadRoot
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.UriUtils
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST","MemberVisibilityCanBePrivate")
@Component("httpctx")
class HttpContext: ApplicationContextAware {

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {

        lateinit var context: ApplicationContext

        private val servletAttributes: ServletRequestAttributes?
            get() = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?

        val request: HttpServletRequest
            get() = servletAttributes?.request ?: MockHttpServletRequest()

        val response: HttpServletResponse
            get() = servletAttributes?.response ?: MockHttpServletResponse()

        fun hasContentType(vararg type: MediaType): Boolean {
            for (t in type)
                if (contentType.contains(t.type)) return true
            return false
        }

        val contentType: String
            get() = request.contentType?.lowercase() ?: ""

        val contextRoot: String
            get() = request.contextPath ?: ""

        val session: HttpSession
            get() = request.session

        fun createSession(): HttpSession = request.getSession(true)

        val headers: Map<String, String>
            get() {
                return LinkedHashMap<String,String>().apply {
                    request.headerNames.iterator().forEach { name ->
                        name.toString().let { this[it] = header(it) }
                    }
                }
            }

        fun header(key: String?): String {
            return request.getHeader(key) ?: ""
        }

        val userAgent: String
            get() = header("user-agent")

        val parameters: Map<String,String>
            get() {
                return LinkedHashMap<String,String>().apply {
                    request.parameterMap.forEach { (key, value) -> this["$key"] = nvl(value) }
                }
            }

        fun setHeaderForFileDownload(fileName: String): HttpServletResponse {
            val encodedFileName = encodePath(fileName)
            return response.apply {
                setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                setHeader("Pragma", "no-cache")
                setHeader("Expires", "0")
                setHeader("Content-Disposition", String.format("attachment;filename=\"%s\"", encodedFileName))
            }
        }

        /**
         * escape file name for URL
         *
         * @param fileName file name
         * @return escaped file name
         */
        fun encodePath(fileName: String): String = UriUtils.encodePath(fileName, UTF_8)

        /**
         * return Spring bean.
         *
         * @param byKlass bean class
         * @param <T> return type of bean
         * @return Spring bean
        </T> */
        fun <T:Any> bean(byKlass: KClass<T>, vararg arg: String): T {
            return when {
                arg.isEmpty() -> context.getBean(byKlass.java)
                else -> context.getBean(byKlass.java, *arg)
            }
        }

        /**
         * return Spring bean.
         *
         * @param byName  bean name
         * @param <T> return type of bean
         * @return Spring bean
        </T> */
        fun <T> bean(byName: String, vararg arg: String): T {
            return when {
                arg.isEmpty() -> context.getBean(byName)
                else -> context.getBean(byName, *arg)
            } as T
        }

        /**
         * return SpringBoot environment bean
         *
         * @return environment bean
         */
        val environment: Environment
            get() = bean(Environment::class)

        /**
         * return configuration value in application.properties(or yml)
         *
         * @param key  configuration key
         * @return configuration value
         */
        @JvmOverloads
        fun environment(key: String, defaultValue: String = ""): String {
            return environment.getProperty(key).ifEmpty { defaultValue }
        }

        /**
         * active profile (get from "spring.profiles.active")
         */
        val activeProfile: String
            get() = environment("spring.profiles.active")

        /**
         * check if given profile exists in active profiles.
         *
         * @param profile profile
         * @return true if given profile exists
         */
        fun hasProfile(profile: String): Boolean
            = activeProfile.contains(profile)

        /**
         * transaction ID based on UUID.
         */
        val txId: String
            get() = ThreadRoot.key

        /**
         * IP of remote client
         */
        val remoteAddress: String
            get() = request.remoteAddr?.replace(":", ".") ?: ""

        /**
         * IP of local host.
         */
        val localhostIp: String
            get() {
                return try {
                    InetAddress.getLocalHost()?.hostAddress?.replace(":",".") ?: ""
                } catch (e: UnknownHostException) {
                    localAddress
                }
            }

        /**
         * local host name.
         */
        val localhost: String
            get() {
                return try {
                    InetAddress.getLocalHost()?.hostName ?: ""
                } catch (e: UnknownHostException) {
                    localAddress
                }
        }

        /**
         * canonical local host name.
         */
        val canonicalLocalHost: String
            get() {
                return try {
                    InetAddress.getLocalHost().canonicalHostName
                } catch (e: UnknownHostException) {
                    localAddress
                }
            }

        /**
         * local address
         */
        val localAddress: String
            get() {
                NetworkInterface.getNetworkInterfaces().iterator().forEach {
                    it.inetAddresses.iterator().forEach { address ->
                        if( acceptableAddress(address) )
                            return address.hostAddress
                    }
                }
                throw UnknownHostException()
            }

        private fun acceptableAddress(address: InetAddress?): Boolean {
            return address != null && !address.isLoopbackAddress && !address.isAnyLocalAddress && !address.isLinkLocalAddress
        }

        /**
         * HTTP cookies.
         */
        val cookies: List<Cookie>
            get() = request.cookies.toList()

}}