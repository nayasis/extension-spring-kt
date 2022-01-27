package com.github.nayasis.kotlin.spring.extension.jpa.domain

import com.github.nayasis.kotlin.basica.annotation.NoArg
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import javax.persistence.Column
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@NoArg
open class PageParam(
    var page: Int     = 0,
    var size: Int     = 10,
    var sort: String? = null,
) {

    @JvmOverloads
    fun toPageable(defaultSort: String? = null, columnMapper: ((field: String) -> String?)? = null): Pageable {
        val expression = sort ?: defaultSort
        val sortable   = if( expression.isNullOrEmpty() )  Sort.unsorted() else
            SortBuilder().toSort(expression,columnMapper)
        return PageRequest.of(page, size, sortable )
    }

}

/**
 * get column name on JPA entity class
 *
 * @param entityClass JPA entity class
 * @return { javaFieldName : columnName }
 */
fun getColumnNames(entityClass: KClass<*>): Map<String,String> {
    return entityClass.declaredMemberProperties.mapNotNull { it.javaField }
        .associate {
            val field = it.name
            val column = it.getAnnotation(Column::class.java)?.name ?: field
            field to column
        }
}
