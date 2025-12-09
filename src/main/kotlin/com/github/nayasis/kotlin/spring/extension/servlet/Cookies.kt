package com.github.nayasis.kotlin.spring.extension.servlet

import jakarta.servlet.http.Cookie
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

@Suppress("unused")
@Component("cookies")
@ConditionalOnExpression($$"'${server.cookies.enabled:true}' == 'true'")
class Cookies {

    val all: Map<String,Cookie>
        get() = HttpContext.cookies.associateBy { it.name }

    fun exists(name: String?): Boolean
        = get(name) != null

    operator fun get(name: String?): Cookie?
        = HttpContext.cookies.firstOrNull{ it.name == name }

    fun create(name: String, value: String?, path: String? = null, maxAge: Int? = null): Cookie {
        return Cookie(name, value).apply {
            if( path   != null ) this.path = path
            if( maxAge != null ) this.maxAge = maxAge
        }
    }

}