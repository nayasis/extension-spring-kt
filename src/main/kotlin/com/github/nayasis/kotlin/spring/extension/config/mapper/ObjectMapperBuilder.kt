package com.github.nayasis.kotlin.spring.extension.config.mapper

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.DateDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.addSerializer
import io.github.nayasis.kotlin.basica.core.localdate.toLocalDateTime
import io.github.nayasis.kotlin.basica.reflection.serializer.DateSerializer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDateTime
import java.util.*

class ObjectMapperBuilder {

    @JvmOverloads
    fun build(removeXss: Boolean = false, includeNotNull: Boolean = true): ObjectMapper {
        return buildFieldMapper().apply {
            if(includeNotNull)
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            if (removeXss)
                factory.characterEscapes = HtmlCharacterEscapes()
        }
    }

    private fun buildFieldMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .modules(
                KotlinModule.Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, false)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, false)
                    .configure(KotlinFeature.SingletonSupport, false)
                    .configure(KotlinFeature.StrictNullChecks, false)
                    .build(),
                JavaTimeModule(),
                SimpleModule(javaClass.simpleName).apply {
                    addSerializer(Date::class, DateSerializer())
                    addDeserializer(Date::class, DateDeserializers.DateDeserializer())
                    addDeserializer(LocalDateTime::class,CustomLocalDateTimeDeserializer())
                })
            .build()
    }
}

class CustomLocalDateTimeDeserializer: LocalDateTimeDeserializer() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        return try {
            super.deserialize(p, ctxt)
        } catch (e: Exception) {
            p.text.toLocalDateTime()
        }
    }
}