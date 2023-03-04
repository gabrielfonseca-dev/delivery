package com.algasko.delivery.data.repository

import com.algasko.delivery.data.entity.User
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepositoryImplementation<User, Long>, JpaSpecificationExecutor<User> {
    @Query("select u from user u where u.username = :name and u.enabled = true")
    fun findByUsername(name: String): User?

    fun findByNameOrUsernameOrEmailOrDocument(name: String, username: String, email: String, document: String): User?

    fun findByDocument(document: String): User?
}