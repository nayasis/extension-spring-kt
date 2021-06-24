package com.github.nayasis.kotlin.spring.extension.cache

import com.github.nayasis.kotlin.basica.core.validator.isEmpty
import mu.KotlinLogging
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStringCommands
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.types.Expiration
import org.springframework.lang.Nullable
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.function.Function

private val log = KotlinLogging.logger{}

class ClearAllRedisCacheWriter @JvmOverloads constructor(
    private val connectionFactory: RedisConnectionFactory,
    private val sleepTime: Duration = Duration.ZERO
): RedisCacheWriter {

    companion object {
        private val CACHE_NAME_ON_CLEANING: MutableSet<String> = ConcurrentHashMap.newKeySet()
    }

    override fun put(name: String, key: ByteArray, value: ByteArray, @Nullable ttl: Duration?) {
        execute(name) { connection ->
            if (shouldExpireWithin(ttl)) {
                connection[key, value, Expiration.from(ttl!!.toMillis(), TimeUnit.MILLISECONDS)] =
                    RedisStringCommands.SetOption.upsert()
            } else {
                connection[key] = value
            }
            "OK"
        }
    }

    override fun get(name: String, key: ByteArray): ByteArray? {
        return execute(name) { connection -> connection[key] }
    }

    override fun putIfAbsent(name: String, key: ByteArray, value: ByteArray, @Nullable ttl: Duration?): ByteArray? {
        return execute(name) { connection ->
            if (isLockingCacheWriter) lock(name, connection)
            try {
                if (connection.setNX(key, value) == true) {
                    if (shouldExpireWithin(ttl))
                        connection.pExpire(key, ttl!!.toMillis())
                    return@execute null
                }
                return@execute connection[key]
            } finally {
                if (isLockingCacheWriter)
                    unlock(name, connection)
            }
        }
    }

    override fun remove(name: String, key: ByteArray) {
        execute(name) { connection -> connection.del(key) }
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
                val cursor: Cursor<*> = connection.scan(condition)
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

    private fun clean(keys: MutableSet<ByteArray?>, connection: RedisConnection) {
        if (isEmpty(keys)) return
        try {
            connection.del(*keys.toTypedArray())
            keys.clear()
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }

    private fun lock(name: String, connection: RedisConnection): Boolean {
        return connection.setNX(getLockKey(name), ByteArray(0)) == true
    }

    private fun unlock(name: String, connection: RedisConnection): Long? {
        return connection.del(getLockKey(name))
    }

    private fun isLocked(name: String, connection: RedisConnection): Boolean {
        return connection.exists(getLockKey(name)) == true
    }

    private val isLockingCacheWriter: Boolean
        get() = !sleepTime.isZero && !sleepTime.isNegative

    private fun <T> execute(name: String, callback: Function<RedisConnection, T>): T {
        val connection = connectionFactory.connection
        return try {
            waitUnlocked(name, connection)
            callback.apply(connection)
        } finally {
            connection.close()
        }
    }

    private fun waitUnlocked(name: String, connection: RedisConnection) {
        if (!isLockingCacheWriter) return
        try {
            while (isLocked(name, connection)) {
                Thread.sleep(sleepTime.toMillis())
            }
        } catch (ex: InterruptedException) {
            // Re-interrupt current thread, to allow other participants to react.
            Thread.currentThread().interrupt()
            throw PessimisticLockingFailureException( "Interrupted while waiting to unlock cache $name", ex )
        }
    }

    private fun shouldExpireWithin(ttl: Duration?): Boolean {
        return ttl != null && !ttl.isZero && !ttl.isNegative
    }

    private fun getLockKey(name: String): ByteArray {
        return "$name~lock".toByteArray(StandardCharsets.UTF_8)
    }

}