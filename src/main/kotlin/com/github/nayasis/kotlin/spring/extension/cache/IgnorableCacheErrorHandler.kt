package com.github.nayasis.kotlin.spring.extension.cache

import mu.KotlinLogging
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler

private val logger = KotlinLogging.logger{}

class IgnorableCacheErrorHandler: CacheErrorHandler {
    override fun handleCacheGetError(e: RuntimeException, cache: Cache, o: Any) = logger.warn(e.message, e)
    override fun handleCachePutError(e: RuntimeException, cache: Cache, o: Any, o1: Any?) = logger.warn(e.message, e)
    override fun handleCacheEvictError(e: RuntimeException, cache: Cache, o: Any) = logger.warn(e.message, e)
    override fun handleCacheClearError(e: RuntimeException, cache: Cache) = logger.warn(e.message, e)
}