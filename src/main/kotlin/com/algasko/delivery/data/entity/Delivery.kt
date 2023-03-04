package com.algasko.delivery.data.entity

import java.sql.Timestamp
import java.time.Instant.now
import javax.annotation.Nullable
import javax.persistence.*

@Entity(name = "delivery")
@Table(name = "delivery")
class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(unique = true, nullable = false)
    var code: String? = null

    @Column(nullable = false)
    var customerName = ""

    @Column(nullable = false)
    var customerPhone = ""

    @Column(nullable = false)
    var address = ""

    @Column(nullable = false)
    var orderNumber = ""

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], targetEntity = Volume::class)
    var volumes: Collection<Volume>? = null

    @Nullable
    @ManyToOne
    private var driver: User? = null

    @Column(nullable = false)
    var isOpen = true
        private set

    @Column(nullable = true)
    var closedAt: Timestamp? = null
        private set

    @Column(nullable = false)
    var sync: Boolean = false
        private set

    fun close() {
        this.isOpen = false
        this.closedAt = Timestamp.from(now())
        this.sync = true
    }

    fun syncDone() {
        this.sync = false
    }

    fun setDriver(driver: User) {
        this.driver = driver
    }

}