package io.github.nayasis.kotlin.spring.extension.cache

import org.springframework.cache.interceptor.KeyGenerator
import java.lang.reflect.Method

class SimpleKeyGenerator: KeyGenerator {
    override fun generate(target: Any, method: Method, vararg param: Any?): Any {
        when {
            param.isEmpty() -> return SimpleKey.EMPTY
            param.size == 1 -> with(param.first()) { if( this is Array<*> ) return this }
        }
        return SimpleKey(*param)
    }
}