package com.github.nayasis.kotlin.spring.extension.jpa.converter

import com.github.nayasis.kotlin.basica.reflection.Reflector
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class ListConverter<T>: AttributeConverter<List<T>,String?> {
    override fun convertToDatabaseColumn(items: List<T>?): String? {
        return if(items == null) null else Reflector.toJson(items)
    }
    override fun convertToEntityAttribute(string: String?): List<T> {
        return Reflector.toObject(string)
    }
}