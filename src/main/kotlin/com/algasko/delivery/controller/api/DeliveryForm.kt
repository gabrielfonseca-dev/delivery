package com.algasko.delivery.controller.api

import com.algasko.delivery.data.entity.Delivery
import com.algasko.delivery.data.entity.User
import com.algasko.delivery.data.entity.Volume
import javax.validation.constraints.NotNull

class DeliveryForm {

    @NotNull
    var code: String? = null

    @NotNull
    var customerName = ""

    @NotNull
    var customerPhone = ""

    @NotNull
    var address = ""

    @NotNull
    var orderNumber = ""

    @NotNull
    var volumes: Collection<Volume>? = null
        private set

    @NotNull
    var driver: String? = null

    fun toDelivery(driver: User): Delivery {
        val delivery = Delivery()
        delivery.code = this.code
        delivery.customerName = this.customerName
        delivery.customerPhone = this.customerPhone
        delivery.address = this.address
        delivery.orderNumber = this.orderNumber
        delivery.volumes = this.volumes
        delivery.setDriver(driver)
        return delivery
    }

}