package com.github.nayasis.kotlin.spring.extension.component

import com.github.nayasis.kotlin.basica.etc.Platforms
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import redis.embedded.RedisServer
import redis.embedded.exceptions.EmbeddedRedisException
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private val log = KotlinLogging.logger{}

@Component
@ConditionalOnClass(RedisServer::class)
class EmbeddedRedis {

    @Value("\${spring.redis.embedded.port:6379}")
    private var redisPort: Int = 6379

    @Value("\${spring.redis.embedded.enable:false}")
    private var enable: Boolean = false

    private lateinit var redisServer: RedisServer

    @PostConstruct
    fun startRedis() {
        if (!enable!!) {
            log.debug{"DO NOT start local redis server because [spring.redis.embedded.enable] in \"application.properties(or yml)\" is false."}
            return
        }
        redisServer = RedisServer(redisPort)
        try {
            redisServer.start()
            log.debug{"embedded redis started (port:$redisPort)"}
        } catch (e: Exception) {
            log.info("try to kill previous embedded-redis processor.")
            if (killProcess()) {
                try {
                    redisServer.start()
                    log.debug{"embedded redis started (port:$redisPort)"}
                    return
                } catch (ex2nd: Exception) {
                    log.error("fail to kill previous embedded-redis processor.", ex2nd)
                }
            } else {
                log.error("fail to kill previous embedded-redis processor.")
            }
        }
    }

    private fun killProcess(): Boolean {
        val command = when {
            Platforms.isWindows -> "taskkill /F /IM redis-server-2.8.19.exe"
            else -> return false
        }
        return ProcessBuilder().apply { command(command) }.start().waitFor(5, TimeUnit.SECONDS)
    }

    @PreDestroy
    fun stopRedis() {
        if (redisServer != null) {
            log.debug{"stop local Redis server"}
            try {
                redisServer.stop()
            } catch (e: EmbeddedRedisException) {
                log.error(e.message, e)
            }
        }
    }
}