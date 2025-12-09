package io.github.nayasis.kotlin.spring.extension.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler

private val logger = KotlinLogging.logger{}

class IgnorableCacheErrorHandler: CacheErrorHandler {
    override fun handleCacheGetError(e: RuntimeException, cache: Cache, o: Any) = logger.warn(e) { e.message }
    override fun handleCachePutError(e: RuntimeException, cache: Cache, o: Any, o1: Any?) = logger.warn(e) { e.message }
    override fun handleCacheEvictError(e: RuntimeException, cache: Cache, o: Any) = logger.warn(e) { e.message }
    override fun handleCacheClearError(e: RuntimeException, cache: Cache) = logger.warn(e) { e.message }
}