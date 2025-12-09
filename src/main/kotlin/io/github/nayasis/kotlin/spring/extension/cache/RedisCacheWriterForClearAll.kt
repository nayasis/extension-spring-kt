package io.github.nayasis.kotlin.spring.extension.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.data.redis.cache.CacheStatistics
import org.springframework.data.redis.cache.CacheStatisticsCollector
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStringCommands
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.types.Expiration
import org.springframework.lang.Nullable
import java.lang.Thread.sleep
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.function.Function


private val log = KotlinLogging.logger{}

private val CACHE_NAME_ON_CLEANING: MutableSet<String> = ConcurrentHashMap.newKeySet()

class RedisCacheWriterForClearAll @JvmOverloads constructor(
    private val connectionFactory: RedisConnectionFactory,
    private val sleepTime: Duration = Duration.ZERO,
    private var cacheStatisticsCollector: CacheStatisticsCollector = CacheStatisticsCollector.none(),
): RedisCacheWriter {

    override fun getCacheStatistics(cacheName: String): CacheStatistics {
        return cacheStatisticsCollector.getCacheStatistics(cacheName)
    }

    override fun put(name: String, key: ByteArray, value: ByteArray, @Nullable ttl: Duration?) {
        execute(name) { connection ->
            if (shouldExpireWithin(ttl)) {
                connection.stringCommands().set(
                    key, 
                    value, 
                    Expiration.from(ttl!!.toMillis(), TimeUnit.MILLISECONDS),
                    RedisStringCommands.SetOption.upsert()
                )
            } else {
                connection.stringCommands().set(key, value)
            }
            "OK"
        }
    }

    override fun get(name: String, key: ByteArray): ByteArray? =
        execute(name) { connection -> connection.stringCommands().get(key) }

    override fun putIfAbsent(name: String, key: ByteArray, value: ByteArray, @Nullable ttl: Duration?): ByteArray? {
        return execute(name) { connection ->
            if (isLockingCacheWriter) lock(name, connection)
            try {
                if (connection.stringCommands().setNX(key, value) == true) {
                    if (shouldExpireWithin(ttl))
                        connection.keyCommands().pExpire(key, ttl!!.toMillis())
                    return@execute null
                }
                return@execute connection.stringCommands().get(key)
            } finally {
                if (isLockingCacheWriter)
                    unlock(name, connection)
            }
        }
    }

    override fun remove(name: String, key: ByteArray) {
        execute(name) { connection -> connection.keyCommands().del(key) }
    }

    override fun clean(name: String, pattern: ByteArray) {
        execute(name) { connection ->
            if (CACHE_NAME_ON_CLEANING.contains(name)) {
                log.warn{"cancel cache cleaning because task($name) is already running."}
                return@execute "DONE"
            }
            CACHE_NAME_ON_CLEANING.add(name)
            var total = 0L
            val keys = HashSet<ByteArray?>()
            try {
                val condition = ScanOptions.scanOptions().count(1000L).match(String(pattern)).build()
                val cursor: Cursor<*> = connection.keyCommands().scan(condition)
                while (cursor.hasNext()) {
                    val value = cursor.next() as ByteArray
                    if (value.isNotEmpty()) {
                        keys.add(value)
                        total++
                    }
                    if (total % 100 == 0L) clean(keys, connection)
                }
            } finally {
                clean(keys, connection)
                CACHE_NAME_ON_CLEANING.remove(name)
                log.info{"clean (key:${name}, count:${total})"}
            }
            "OK"
        }
    }

    override fun clearStatistics(name: String) =
        cacheStatisticsCollector.reset(name)

    override fun withStatisticsCollector(cacheStatisticsCollector: CacheStatisticsCollector): RedisCacheWriter =
        RedisCacheWriterForClearAll(connectionFactory,sleepTime,cacheStatisticsCollector)

    override fun retrieve(name: String, key: ByteArray, ttl: Duration?): CompletableFuture<ByteArray> {
        return CompletableFuture.supplyAsync {
            get(name, key) ?: throw IllegalStateException("Value not found")
        }
    }

    override fun store(name: String, key: ByteArray, value: ByteArray, ttl: Duration?): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            put(name, key, value, ttl)
        }
    }

    private fun clean(keys: MutableSet<ByteArray?>, connection: RedisConnection) {
        if (keys.isEmpty()) return
        try {
            connection.keyCommands().del(*keys.toTypedArray())
            keys.clear()
        } catch (e: Exception) {
            log.error(e) { e.message ?: "" }
        }
    }

    private fun lock(name: String, connection: RedisConnection): Boolean =
        connection.stringCommands().setNX(getLockKey(name), ByteArray(0)) == true

    private fun unlock(name: String, connection: RedisConnection): Long? =
        connection.keyCommands().del(getLockKey(name))

    private fun isLocked(name: String, connection: RedisConnection): Boolean =
        connection.keyCommands().exists(getLockKey(name)) == true

    private val isLockingCacheWriter: Boolean
        get() = !sleepTime.isZero && !sleepTime.isNegative

    private fun <T> execute(name: String, callback: Function<RedisConnection, T>): T {
        return connectionFactory.connection.use {
            waitUnlocked(name, it)
            callback.apply(it)
        }
    }

    private fun waitUnlocked(name: String, connection: RedisConnection) {
        if (!isLockingCacheWriter) return
        try {
            while (isLocked(name, connection)) {
                sleep(sleepTime.toMillis())
            }
        } catch (ex: InterruptedException) {
            // Re-interrupt current thread, to allow other participants to react.
            Thread.currentThread().interrupt()
            throw PessimisticLockingFailureException( "Interrupted while waiting to unlock cache $name", ex )
        }
    }

    private fun shouldExpireWithin(ttl: Duration?): Boolean =
        ttl != null && !ttl.isZero && !ttl.isNegative

    private fun getLockKey(name: String): ByteArray =
        "$name~lock".toByteArray(StandardCharsets.UTF_8)

}