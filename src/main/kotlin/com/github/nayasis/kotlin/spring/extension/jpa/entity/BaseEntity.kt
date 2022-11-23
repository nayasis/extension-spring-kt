package com.github.nayasis.kotlin.spring.extension.jpa.entity

import com.github.nayasis.kotlin.basica.reflection.Reflector
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.MappedSuperclass

@MappedSuperclass
class BaseEntity: Serializable {

    @CreationTimestamp
    @Column
    var regDt: LocalDateTime? = null

    @Column
    var regId: String? = null

    @UpdateTimestamp
    @Column
    var updDt: LocalDateTime? = null
        set(value) {
            field = value
            if(regDt == null)
                regDt = value
        }

    @Column
    var updId: String? = null
        set(value) {
            field = value
            if( regId == null )
                regId = value
        }

    override fun toString(): String {
        return Reflector.toJson(this)
    }

}