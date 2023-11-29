package com.github.nayasis.kotlin.spring.extension.jpa.domain

import com.github.nayasis.kotlin.basica.core.extension.then
import com.github.nayasis.kotlin.basica.core.validator.nvl
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

private val cache = mutableMapOf<KClass<*>,Set<String>>()

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
     * build sort expression
     *
     * @param expression sort expression
     *  ex. name,asc ^ id,desc
     * @param columnMapper column name mapper
     *  ex. { field -> COLUMNS.get(field) }
     * @return query sorting expression
     */
    fun toSort(expression: String?, entity: KClass<*>): Sort {
        return Sort.by(toOrders(expression, entity))
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
     * build order rules
     *
     * @param expression order expressions
     *  ex. name,asc ^ id,desc
     * @param columnMapper column name mapper
     *  ex. { field -> COLUMNS.get(field) }
     * @return order rules
     */
    fun toOrders(expression: String?, entity: KClass<*>): List<Sort.Order> {
        return nvl(expression).split("^").mapNotNull { toOrder(it,entity) }
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
        val field     = words.first().trim().also { if(it.isEmpty()) return null }
        val column    = if( columnMapper == null ) field else columnMapper(field) ?: return null
        val direction = toDirection(words.getOrNull(1))
        return if (column.isNullOrEmpty()) null else Sort.Order(direction, column)
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
    fun toOrder(expression: String?, entity: KClass<*>): Sort.Order? {
        if( ! cache.containsKey(entity) )
            cache[entity] = getFieldNames(entity)
        return toOrder(expression) { cache[entity]!!.contains(it).then(it) }
    }


    fun toDirection(direction: String?): Sort.Direction {
        return when (direction?.trim()?.lowercase()) {
            "desc" -> Sort.Direction.DESC
            "asc"  -> Sort.Direction.ASC
            else   -> Sort.DEFAULT_DIRECTION
        }
    }

}