package com.algasko.delivery.controller

import com.algasko.delivery.data.entity.Instance
import com.algasko.delivery.data.repository.InstanceRepository
import org.springframework.stereotype.Controller

@Controller
class InstanceController(val instanceRepository: InstanceRepository) {

    fun getInstance(from: String, to: String? = null, profileName: String? = null): Instance {
        val existingInstance = instanceRepository.findOpenInstance(from)
        return if (existingInstance.isPresent) existingInstance.get()
        else {
            val newInstance = Instance()
            newInstance.contact = from
            newInstance.contactName = profileName
            newInstance.twilioNumber = to
            instanceRepository.save(newInstance)
            newInstance
        }
    }

    fun getPendingChats(): List<Instance> {
        return instanceRepository.findOpenInstanceForChat()
    }

    fun getAllOpen(): List<Instance> {
        return instanceRepository.findAllOpen()
    }

    fun close(instance: Instance) {
       instance.isOpen = false
       instanceRepository.save(instance)
    }

}