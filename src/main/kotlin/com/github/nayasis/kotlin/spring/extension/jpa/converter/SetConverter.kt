package com.github.nayasis.kotlin.spring.extension.jpa.converter

import com.github.nayasis.kotlin.basica.reflection.Reflector
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class SetConverter<T>: AttributeConverter<Set<T>,String> {
    override fun convertToDatabaseColumn(items: Set<T>?): String {
        return Reflector.toJson(items)
    }
    override fun convertToEntityAttribute(string: String?): Set<T> {
        return Reflector.toObject(string)
    }
}