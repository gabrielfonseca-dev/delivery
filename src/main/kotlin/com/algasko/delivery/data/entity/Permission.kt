package com.algasko.delivery.data.entity

import com.algasko.delivery.data.enum.PermissionType
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity(name = "permission")
@Table(name = "permission")
class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false, unique = true)
    @NotBlank
    lateinit var code: String

    @Enumerated(EnumType.STRING)
    var type: PermissionType? = null

    @Column(nullable = true)
    var active: Int? = null
    var description: String? = null
    var position: Int? = null
    var icon: String? = null
    var url: String? = null
}