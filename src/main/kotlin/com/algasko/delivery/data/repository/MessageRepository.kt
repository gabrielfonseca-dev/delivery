package com.algasko.delivery.data.repository

import com.algasko.delivery.data.entity.Instance
import com.algasko.delivery.data.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    @Query("select m from message m where m.instance = :instance order by m.id asc")
    fun findAllByInstance(instance: Instance): List<Message>
}