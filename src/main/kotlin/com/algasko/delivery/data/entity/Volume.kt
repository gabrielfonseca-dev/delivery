package com.algasko.delivery.data.entity

import javax.persistence.*

@Entity(name = "volume")
@Table(name = "volume")
class Volume : Comparable<Volume> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    val code = ""
    val barcode = ""
    val productImgUrl = ""
    var confirmed = false

    override fun compareTo(other: Volume): Int {
        return this.code.compareTo(other.code)
    }
}