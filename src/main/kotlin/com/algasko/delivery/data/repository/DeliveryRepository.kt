package com.algasko.delivery.data.repository

import com.algasko.delivery.data.entity.Delivery
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

@Repository
interface DeliveryRepository : JpaRepositoryImplementation<Delivery, Long>, JpaSpecificationExecutor<Delivery> {
    @Query("select d from delivery d where d.code = :code")
    fun findByCode(code: String): Delivery?
    @Query("select d from delivery d join d.driver u where u.username = :user and d.isOpen = true")
    fun findByUser(user: String): Collection<Delivery>?
}