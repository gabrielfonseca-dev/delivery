package com.algasko.delivery.data.repository.specification

import com.algasko.delivery.data.entity.Log
import com.algasko.delivery.data.enum.LogCode
import com.algasko.delivery.data.enum.LogType
import org.springframework.data.jpa.domain.Specification

class LogSpecification {

    fun cmd(type: LogType): Specification<Log> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<LogType>("type"),
                type
            )
        }
    }

    fun code(code: LogCode): Specification<Log> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<LogCode>("code"),
                code
            )
        }
    }

    fun script(script: String): Specification<Log> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("script"),
                "%$script%"
            )
        }
    }

    fun task(task: String): Specification<Log> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("task"),
                "%$task%"
            )
        }
    }

    fun value(value: String): Specification<Log> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("value"),
                "%$value%"
            )
        }
    }
}