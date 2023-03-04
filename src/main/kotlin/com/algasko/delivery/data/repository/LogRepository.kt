package com.algasko.delivery.data.repository

import com.algasko.delivery.data.entity.Log
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface LogRepository : JpaRepository<Log, Long>, JpaSpecificationExecutor<Log>