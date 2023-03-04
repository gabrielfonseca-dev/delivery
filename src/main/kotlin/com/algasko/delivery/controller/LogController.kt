package com.algasko.delivery.controller

import com.algasko.delivery.data.entity.Log
import com.algasko.delivery.data.enum.LogCode
import com.algasko.delivery.data.enum.LogType
import com.algasko.delivery.data.repository.LogRepository
import com.algasko.delivery.data.repository.UserRepository
import com.algasko.delivery.data.repository.specification.LogSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Controller
import java.sql.Timestamp
import java.time.Instant.now

@Controller
class LogController(private var logRepository: LogRepository, private var userRepository: UserRepository) {

    private var specification = LogSpecification()
    // private var systemUser: User? = Security.defaultSystemUser

    fun search(text: String, page: Int = 0): Page<Log> {
        return logRepository.findAll(Specification.where(specification.script(text).or(specification.task(text)).or(specification.value(text))), Pageable.ofSize(50).withPage(if(page < 0) 0 else page))
    }

    fun saveLog(code: LogCode, type: LogType, elapsedTime: Long?, value: String, task: String? = null, script: String? = null) {
        val log = Log()
        // TODO log.user = userRepository.findByUsername(SecurityService().authenticatedUser ?: "") ?: systemUser
        log.code = code
        log.type = type
        log.occurredAt = Timestamp.from(now())
        log.elapsedTime = elapsedTime
        log.value = value
        log.task = task
        log.script = script
        logRepository.save(log)
    }

    fun saveSystemLog(code: LogCode, type: LogType, elapsedTime: Long?, value: String, task: String? = null, script: String? = null) {
        val log = Log()
        // TODO log.user = systemUser
        log.code = code
        log.type = type
        log.occurredAt = Timestamp.from(now())
        log.elapsedTime = elapsedTime
        log.value = value
        log.task = task
        log.script = script
        logRepository.save(log)
    }

}