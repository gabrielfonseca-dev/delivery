package com.algasko.delivery.controller

import com.algasko.delivery.data.entity.Instance
import com.algasko.delivery.data.entity.Message
import com.algasko.delivery.data.repository.MessageRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MessageController {

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Autowired
    lateinit var userController: UserController

    fun listMessages(instance: Instance?): List<Message> {
        return if(instance != null) {
            messageRepository.findAllByInstance(instance)
        } else{
            listOf()
        }
    }

    fun save(message: Message) {
        messageRepository.save(message)
    }

}