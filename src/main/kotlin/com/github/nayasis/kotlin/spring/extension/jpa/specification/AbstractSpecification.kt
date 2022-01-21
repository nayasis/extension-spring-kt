package com.github.nayasis.kotlin.spring.extension.jpa.specification

import com.github.nayasis.kotlin.basica.core.validator.isEmpty
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Expression
import javax.persistence.criteria.Path
import javax.persistence.criteria.Root

@Suppress("UNCHECKED_CAST","MemberVisibilityCanBePrivate")
abstract class AbstractSpecification<T> {

    abstract fun build(): Specification<T>

    protected fun contains(key: String, values: Collection<*>?): Specification<T> {
        return Specification { root, _, cb ->
            if ( values.isNullOrEmpty() ) {
                cb.conjunction()
            } else {
                val column: Path<Any>? = getPath(root, key)
                val `in` = cb.`in`(column)
                values.forEach{ status -> `in`.value(status) }
                `in`
            }
        }
    }

    protected fun like(key: String, value: Any?): Specification<T> {
        return Specification { root, _, cb ->
            if ( isEmpty(value)) {
                cb.conjunction()
            } else {
                cb.like(getPath(root, key) as Expression<String>, "%$value%")
            }
        }
    }

    protected fun notLike(key: String, value: Any?): Specification<T> {
        return Specification { root: Root<T>?, _: CriteriaQuery<*>?, cb: CriteriaBuilder ->
            if (isEmpty(value)) {
                cb.conjunction()
            } else {
                cb.notLike(getPath(root, key) as Expression<String>, "%$value%")

            }
        }
    }

    protected fun likes(key: String, values: Collection<*>?): Specification<T> {
        var specification: Specification<T>? = null
        if( ! values.isNullOrEmpty() ) {
            for (value in values) {
                if (value == null) continue
                specification = if (specification == null) {
                    like(key, value)
                } else {
                    specification.or(like(key, value))
                }
            }
        }
        return specification ?: Specification { _, _, cb -> cb.conjunction() }
    }

    protected fun notLikes(key: String, values: Collection<*>?): Specification<T> {
        var specification: Specification<T>? = null
        if( ! values.isNullOrEmpty() ) {
            for (value in values) {
                if (value == null) continue
                specification = if (specification == null) {
                    notLike(key, value)
                } else {
                    specification.or(notLike(key, value))
                }
            }
        }
        return specification ?: Specification { _, _, cb -> cb.conjunction() }
    }

    protected fun isNull(key: String): Specification<T> {
        return Specification { root, _, cb -> cb.isNull( getPath(root, key) ) }
    }

    protected fun isNotNull(key: String): Specification<T> {
        return Specification { root, _, cb -> cb.isNotNull( getPath(root, key) ) }
    }

    protected fun equal(key: String, value: Any?): Specification<T> {
        return Specification { root, _, cb ->
            if (isEmpty(value)) cb.conjunction()
            cb.equal(getPath(root, key), value)
        }
    }

    protected fun <E> between(key: String, from: E?, to: E?): Specification<T> {
        return Specification { root, _, cb ->
            if (isEmpty(from) || isEmpty(to)) cb.conjunction()
            val path = getPath(root, key)
            if (from is Int)           cb .between(path as Expression<out Int>,           from, to as Int)
            if (from is Double)        cb .between(path as Expression<out Double>,        from, to as Double        )
            if (from is Float)         cb .between(path as Expression<out Float>,         from, to as Float         )
            if (from is BigDecimal)    cb .between(path as Expression<out BigDecimal>,    from, to as BigDecimal)
            if (from is BigInteger)    cb .between(path as Expression<out BigInteger>,    from, to as BigInteger)
            if (from is LocalDateTime) cb .between(path as Expression<out LocalDateTime>, from, to as LocalDateTime )
            if (from is LocalDate)     cb .between(path as Expression<out LocalDate>,     from, to as LocalDate)
            cb.between(path as Expression<out String>, from.toString(), to.toString())
        }
    }

    protected fun lessThan(key: String, value: Any?): Specification<T> {
        return Specification { root, _, cb ->
            if (isEmpty(value)) cb.conjunction()
            val path = getPath(root, key)
            if (value is Int)           cb.lessThan(path as Expression<out Int>,           value)
            if (value is Double)        cb.lessThan(path as Expression<out Double>,        value)
            if (value is Float)         cb.lessThan(path as Expression<out Float>,         value)
            if (value is BigDecimal)    cb.lessThan(path as Expression<out BigDecimal>,    value)
            if (value is BigInteger)    cb.lessThan(path as Expression<out BigInteger>,    value)
            if (value is LocalDateTime) cb.lessThan(path as Expression<out LocalDateTime>, value)
            if (value is LocalDate)     cb.lessThan(path as Expression<out LocalDate>,     value)
            cb.lessThan(path as Expression<out String>, value.toString())
        }
    }

    protected fun lessThanOrEqual(key: String, value: Any?): Specification<T> {
        return Specification { root, _, cb ->
            if (isEmpty(value)) cb.conjunction()
            val path = getPath(root, key)
            if (value is Int)           cb.lessThanOrEqualTo(path as Expression<out Int>,           value)
            if (value is Double)        cb.lessThanOrEqualTo(path as Expression<out Double>,        value)
            if (value is Float)         cb.lessThanOrEqualTo(path as Expression<out Float>,         value)
            if (value is BigDecimal)    cb.lessThanOrEqualTo(path as Expression<out BigDecimal>,    value)
            if (value is BigInteger)    cb.lessThanOrEqualTo(path as Expression<out BigInteger>,    value)
            if (value is LocalDateTime) cb.lessThanOrEqualTo(path as Expression<out LocalDateTime>, value)
            if (value is LocalDate)     cb.lessThanOrEqualTo(path as Expression<out LocalDate>,     value)
            cb.lessThanOrEqualTo(path as Expression<out String>, value.toString())
        }
    }

    protected fun greaterThan(key: String, value: Any?): Specification<T> {
        return Specification { root, _, cb ->
            if (isEmpty(value)) cb.conjunction()
            val path = getPath(root, key)
            if (value is Int)           cb.greaterThan(path as Expression<out Int>,           value)
            if (value is Double)        cb.greaterThan(path as Expression<out Double>,        value)
            if (value is Float)         cb.greaterThan(path as Expression<out Float>,         value)
            if (value is BigDecimal)    cb.greaterThan(path as Expression<out BigDecimal>,    value)
            if (value is BigInteger)    cb.greaterThan(path as Expression<out BigInteger>,    value)
            if (value is LocalDateTime) cb.greaterThan(path as Expression<out LocalDateTime>, value)
            if (value is LocalDate)     cb.greaterThan(path as Expression<out LocalDate>,     value)
            cb.greaterThan(path as Expression<out String>, value.toString())
        }
    }

    protected fun greaterThanOrEqual(key: String, value: Any?): Specification<T> {
        return Specification { root, _, cb ->
            if (isEmpty(value)) cb.conjunction()
            val path = getPath(root, key)
            if (value is Int)           cb.greaterThanOrEqualTo(path as Expression<out Int>,           value)
            if (value is Double)        cb.greaterThanOrEqualTo(path as Expression<out Double>,        value)
            if (value is Float)         cb.greaterThanOrEqualTo(path as Expression<out Float>,         value)
            if (value is BigDecimal)    cb.greaterThanOrEqualTo(path as Expression<out BigDecimal>,    value)
            if (value is BigInteger)    cb.greaterThanOrEqualTo(path as Expression<out BigInteger>,    value)
            if (value is LocalDateTime) cb.greaterThanOrEqualTo(path as Expression<out LocalDateTime>, value)
            if (value is LocalDate)     cb.greaterThanOrEqualTo(path as Expression<out LocalDate>,     value)
            cb.greaterThanOrEqualTo(path as Expression<out String>, value.toString())
        }
    }

    protected fun junction(specification: Specification<*>): Specification<T> {
        return specification as Specification<T>
    }

    protected fun getPath(expression: Path<*>?, key: String): Path<Any>? {
        if (expression == null) return null
        val index = key.indexOf(".")
        return if (index < 0) {
            expression.get(key)
        } else {
            val prevKey = key.substring(0, index)
            val nextKey = key.substring(index + 1)
            val path: Path<Any>?  = expression.get(prevKey)
            val child: Path<Any>? = getPath(path, nextKey)
            child ?: path
        }
    }

}