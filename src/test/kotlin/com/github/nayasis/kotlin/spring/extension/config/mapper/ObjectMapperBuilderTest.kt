package com.github.nayasis.kotlin.spring.extension.config.mapper

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.nayasis.kotlin.basica.core.localdate.toLocalDateTime
import com.github.nayasis.kotlin.basica.reflection.Reflector
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalDateTime.now

class ObjectMapperBuilderTest {

    @Test
    fun basic() {

        val simple = Simple("nayasis", "1999-01-01".toLocalDateTime())

        val mapper = ObjectMapperBuilder().fieldMapper().apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

        println( Reflector.toJson(simple) )

        println( mapper.writeValueAsString(simple) )

    }

}

data class Simple (
    var name: String = "",
    var birth: LocalDateTime = now()

)