package com.github.nayasis.kotlin.spring.extension.config.mapper

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

class ObjectMapperBuilder {

    @JvmOverloads
    fun fieldMapper(removeXss: Boolean = false, includeNotNull: Boolean = true): ObjectMapper {
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
            .modules(JavaTimeModule())
            .modules(KotlinModule())
            .build()
    }
}