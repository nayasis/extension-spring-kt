package com.github.nayasis.kotlin.spring.extension.jpa.domain

import com.github.nayasis.kotlin.basica.core.validator.nvl
import org.springframework.data.domain.Sort

@Suppress("MemberVisibilityCanBePrivate")
class SortBuilder {

    /**
     * build sort expression
     *
     * @param expression sort expression
     *  ex. name,asc ^ id,desc
     * @param columnMapper column name mapper
     *  ex. { field -> COLUMNS.get(field) }
     * @return query sorting expression
     */
    @JvmOverloads
    fun toSort(expression: String?, columnMapper: ((field: String) -> String?)? = null): Sort {
        return Sort.by(toOrders(expression,columnMapper))
    }

    /**
     * build order rules
     *
     * @param expression order expressions
     *  ex. name,asc ^ id,desc
     * @param columnMapper column name mapper
     *  ex. { field -> COLUMNS.get(field) }
     * @return order rules
     */
    @JvmOverloads
    fun toOrders(expression: String?, columnMapper: ((field: String) -> String?)? = null): List<Sort.Order> {
        return nvl(expression).split("^").mapNotNull { toOrder(it,columnMapper) }
    }

    /**
     * build order
     *
     * @param expression order expression
     *  ex1. colA,desc
     *  ex2. colA,asc
     *  ex3. colA
     * @param columnMapper column name mapper
     *  ex. { field -> COLUMNS.get(field) }
     * @return order
     */
    @JvmOverloads
    fun toOrder(expression: String?, columnMapper: ((field: String) -> String?)? = null ): Sort.Order? {
        val words     = nvl(expression).split(",")
        val field     = words.first().trim()
        val column    = columnMapper?.let{it(field)} ?: field
        val direction = toDirection(words.getOrNull(1))
        return if (column.isNullOrEmpty()) null else Sort.Order(direction, column)
    }

    fun toDirection(direction: String?): Sort.Direction {
        return when (direction?.trim()?.lowercase()) {
            "desc" -> Sort.Direction.DESC
            "asc"  -> Sort.Direction.ASC
            else   -> Sort.DEFAULT_DIRECTION
        }
    }

}