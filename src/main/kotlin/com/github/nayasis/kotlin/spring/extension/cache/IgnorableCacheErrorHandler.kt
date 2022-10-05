package com.github.nayasis.kotlin.spring.extension.cache

import mu.KotlinLogging
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler

private val log = KotlinLogging.logger{}

class IgnorableCacheErrorHandler: CacheErrorHandler {
    override fun handleCacheGetError(e: RuntimeException, cache: Cache, o: Any) = log.warn(e.message, e)
    override fun handleCachePutError(e: RuntimeException, cache: Cache, o: Any, o1: Any?) = log.error(e.message, e)
    override fun handleCacheEvictError(e: RuntimeException, cache: Cache, o: Any) = log.error(e.message, e)
    override fun handleCacheClearError(e: RuntimeException, cache: Cache) = log.error(e.message, e)
}