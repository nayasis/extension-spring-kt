package io.github.nayasis.kotlin.spring.extension.jpa.converter

import io.github.nayasis.kotlin.basica.reflection.Reflector
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class SetConverter<T>: AttributeConverter<Set<T>,String?> {
    override fun convertToDatabaseColumn(items: Set<T>?): String? {
        return if(items == null) null else Reflector.toJson(items)
    }
    override fun convertToEntityAttribute(string: String?): Set<T> {
        return Reflector.toObject(string)
    }
}