package com.github.nayasis.kotlin.spring.extension.jpa.domain

import com.github.nayasis.kotlin.basica.annotation.NoArg
import com.github.nayasis.kotlin.basica.core.extention.ifEmpty
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@NoArg
open class BasePageParam(
    var page: Int     = 0,
    var size: Int     = 10,
    var sort: String? = null,
) {

    fun toPageable(defaultSort: String, entity: KClass<*>): Pageable {
        val expression = sort.ifEmpty { defaultSort }
        val sortable   = if( expression.isNullOrEmpty() )  Sort.unsorted() else
            SortBuilder().toSort(expression,entity)
        return PageRequest.of(page, size, sortable )
    }

    fun toPageable(defaultSort: String? = null, columnMapper: ((field: String) -> String?)? = null): Pageable {
        val expression = sort.ifEmpty { defaultSort }
        val sortable   = if( expression.isNullOrEmpty() )  Sort.unsorted() else
            SortBuilder().toSort(expression,columnMapper)
        return PageRequest.of(page, size, sortable )
    }

}

/**
 * get column name on JPA entity class
 *
 * @param entity JPA entity class
 * @return field names
 */
fun getFieldNames(entity: KClass<*>): Set<String> {
    return entity.declaredMemberProperties.mapNotNull { it.javaField?.name }.toSet()
}