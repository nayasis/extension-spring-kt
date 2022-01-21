package com.github.nayasis.kotlin.spring.extension.servlet

import org.springframework.stereotype.Component
import javax.servlet.http.Cookie

@Suppress("MemberVisibilityCanBePrivate")
@Component("cookies")
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