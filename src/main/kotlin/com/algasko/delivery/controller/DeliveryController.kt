package com.algasko.delivery.controller

import com.algasko.delivery.controller.api.DeliveryForm
import com.algasko.delivery.data.entity.Delivery
import com.algasko.delivery.data.repository.DeliveryRepository
import com.algasko.delivery.data.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

// TODO Api Swagger
@RestController
@RequestMapping("delivery")
class DeliveryController(val deliveryRepository: DeliveryRepository, val userRepository: UserRepository) {

    @Transactional
    @PostMapping(consumes = ["application/json"])
    fun create(@RequestBody @Valid form: DeliveryForm, resp: HttpServletResponse): Any? {
        val driver = userRepository.findByDocument(form.driver ?: "") ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Driver not found")
        return deliveryRepository.save(form.toDelivery(driver))
    }

    @GetMapping("/{code}")
    fun find(@PathVariable code: String): Delivery {
        return deliveryRepository.findByCode(code)!!
    }

}