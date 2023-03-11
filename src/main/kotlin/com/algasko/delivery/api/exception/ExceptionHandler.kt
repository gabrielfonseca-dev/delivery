package com.algasko.delivery.api.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.sql.SQLIntegrityConstraintViolationException
import java.util.stream.Stream
import javax.persistence.EntityNotFoundException

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun invalid(e: MethodArgumentNotValidException): ResponseEntity<Stream<FieldErrorMessage>> {
        val errors = ArrayList<FieldErrorMessage>()
        e.fieldErrors.forEach { error ->
            errors.add(
                FieldErrorMessage(
                    error.code,
                    error.defaultMessage
                )
            )
        }
        return ResponseEntity.badRequest().body(errors.stream())
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun notFound(e: EntityNotFoundException): ResponseEntity<Any> {
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(NullPointerException::class)
    fun notFound(e: NullPointerException): ResponseEntity<Any> {
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException::class)
    fun database(e: SQLIntegrityConstraintViolationException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(e.message)
    }

    class FieldErrorMessage(field: String?, error: String?)

}