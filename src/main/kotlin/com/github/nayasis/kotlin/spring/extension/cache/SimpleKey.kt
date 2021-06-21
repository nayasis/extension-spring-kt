package com.github.nayasis.kotlin.spring.extension.cache

import org.springframework.lang.Nullable

class SimpleKey(vararg elements: Any?) {

    companion object {
        val EMPTY = SimpleKey()
    }

    private val params: Array<Any?> = arrayOf(*elements)

    @Transient
    private val hashCode: Int = elements.contentDeepHashCode()

    override fun equals(@Nullable other: Any?): Boolean {
        return this === other ||
            other is SimpleKey && other.hashCode == hashCode && params.contentDeepEquals(other.params)
    }

    override fun hashCode(): Int = hashCode

    override fun toString(): String = params.joinToString()

}