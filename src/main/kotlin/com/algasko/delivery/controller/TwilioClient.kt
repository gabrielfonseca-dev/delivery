package com.algasko.delivery.controller

import com.algasko.delivery.data.entity.Instance
import com.algasko.delivery.data.entity.Message
import com.algasko.delivery.security.SecurityService
import com.algasko.delivery.security.credentials.CredentialsService
import com.twilio.Twilio
import com.twilio.type.PhoneNumber
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.twilio.rest.api.v2010.account.Message as TwilioMessage

@RestController
@RequestMapping("/twilio")
class TwilioClient {

    @Autowired
    private lateinit var instanceController: InstanceController

    @Autowired
    private lateinit var messageController: MessageController

    @Autowired
    private lateinit var credentialsService: CredentialsService

    @PostMapping(value = ["/webhook"], produces = ["application/xml"])
    /*fun handle(form: TwiMLForm): String? {

        val instance = instanceController.getInstance(form.from!!, form.to, form.profileName)
        val messageList = messageController.listMessages(instance)
        var ticket: Ticket? = instanceController.getTicket(instance)
        val lastMessage = messageList.find { !it.answered }
        val responseMessage: Message?

        if (lastMessage != null && instance.user == null) {
            responseMessage = when (lastMessage.code) {
                defaultMessages.CODE_ASK_FOR_TICKET_OWNER -> {
                    ticket!!.owner = form.body
                    ticketController.save(ticket)
                    lastMessage.answered = true
                    messageController.save(lastMessage)
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_ASK_FOR_TICKET_OWNER, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_TICKET_TITLE,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_TICKET_TITLE
                        )
                    )
                    Message.Builder(defaultMessages.ASK_TICKET_TITLE).build()
                }
                defaultMessages.CODE_ASK_TICKET_TITLE -> {
                    ticket!!.title = form.body
                    ticketController.save(ticket)
                    lastMessage.answered = true
                    messageController.save(lastMessage)
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_ASK_TICKET_TITLE, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_TICKET_DESCRIPTION,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_TICKET_DESCRIPTION
                        )
                    )
                    Message.Builder(defaultMessages.ASK_TICKET_DESCRIPTION).build()
                }
                defaultMessages.CODE_ASK_TICKET_DESCRIPTION -> {
                    ticket!!.description = form.body
                    ticketController.save(ticket)
                    lastMessage.answered = true
                    messageController.save(lastMessage)
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_ASK_TICKET_DESCRIPTION, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_ATTACHMENTS,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_ATTACHMENTS
                        )
                    )
                    Message.Builder(defaultMessages.ASK_FOR_TICKET_ATTACHMENTS).build()
                }
                defaultMessages.CODE_ASK_FOR_TICKET_ATTACHMENTS -> {
                    if (form.body?.lowercase()?.trim() == "sim") {
                        ticket!!.hasAttachments = true
                        ticketController.save(ticket)
                    }
                    lastMessage.answered = true
                    messageController.save(lastMessage)
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_ASK_FOR_TICKET_ATTACHMENTS, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_CONFIRMATION,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_CONFIRMATION
                        )
                    )
                    if (ticket!!.hasAttachments) Message.Builder(defaultMessages.ASK_FOR_TICKET_CONFIRMATION_WITH_ATTACHMENT)
                        .build()
                    else Message.Builder(defaultMessages.ASK_FOR_TICKET_CONFIRMATION).build()
                }
                defaultMessages.CODE_ASK_FOR_TICKET_CONFIRMATION -> {
                    if (form.mediaUrl?.isNotEmpty() == true) {
                        form.mediaUrl!!.map { ticket!!.attachments.plus("\n$it") }
                    }
                    if (form.body?.lowercase() == "sim") {
                        val taskDescription = StringBuilder()
                        taskDescription.appendLine("Solicitante -> ${form.profileName}")
                        taskDescription.appendLine("Telefone -> ${form.from}")
                        taskDescription.appendLine("Email -> ${ticket?.owner}")
                        taskDescription.appendLine(ticket?.description)
                        taskDescription.appendLine(ticket?.attachments)

                        jiraApi.createIssue(ticket?.title!!, taskDescription.toString(), null, listOf("whatsapp")).let {
                            instance.isOpen = false
                            instanceController.instanceRepository.save(instance)
                            Message.Builder("Sua solicitação foi aberta com sucesso \n\nO número do seu chamado é -> $it")
                                .build()
                        }
                    } else if(form.body?.lowercase() == "não" || form.body?.lowercase() == "nao") {
                        Message.Builder("Sua solicitação foi cancelada.").build()
                    }
                    else {
                        Message.Builder("Arquivos anexados. Envie mais anexos ou confirme a abertura com um *sim*").build()
                    }
                }
                defaultMessages.CODE_ASK_FOR_TICKET_OWNER_TO_CONSULT -> {
                    jiraApi.getIssueByOwner(form.body!!).let { issueList ->
                        instance.isOpen = false
                        instanceController.instanceRepository.save(instance)
                        Message.Builder("${defaultMessages.TICKET_LIST_MESSAGE}\n\n$issueList").build()
                    }
                }
                defaultMessages.CODE_ASK_FOR_TICKET_NUMBER -> {
                    jiraApi.getIssueDetails(form.body!!).let { issueDetails ->
                        instance.isOpen = false
                        instanceController.instanceRepository.save(instance)
                        Message.Builder(issueDetails).build()
                    }
                }
                defaultMessages.CODE_ASK_FOR_TICKET_NUMBER_TO_ANSWER -> {
                    ticket = Ticket()
                    ticket.instance = instance
                    ticket.ticketKey = form.body
                    ticketController.save(ticket)
                    lastMessage.answered = true
                    messageController.save(lastMessage)
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_ASK_FOR_TICKET_NUMBER_TO_ANSWER, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_COMMENT,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_COMMENT
                        )
                    )
                    Message.Builder(defaultMessages.ASK_FOR_TICKET_COMMENT).build()
                }
                defaultMessages.ASK_FOR_TICKET_COMMENT -> {
                    lastMessage.answered = true
                    messageController.save(lastMessage)
                    messageController.save(
                        form.convert().incoming(defaultMessages.ASK_FOR_TICKET_COMMENT, instance, true)
                    )
                    instance.isOpen = false
                    instanceController.instanceRepository.save(instance)
                    jiraApi.addComment(ticket!!.ticketKey!!, form.body!!).let { success ->
                        instance.isOpen = false
                        instanceController.instanceRepository.save(instance)
                        Message.Builder("Comentário ${if (success) "realizado" else "falhou"}").build()
                    }
                }
                else -> {
                    instance.isOpen = false
                    instanceController.instanceRepository.save(instance)
                    Message.Builder("Nao consegui entender. Vamos tentar novamente.\n\n${defaultMessages.WELCOME_MESSAGE}").build()
                }
            }
        } else {
            responseMessage = when (form.body) {
                defaultMessages.NEW_TICKET -> {
                    ticket = Ticket()
                    ticket.instance = instance
                    ticketController.save(ticket)
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_NEW_TICKET, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_OWNER,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_OWNER
                        )
                    )
                    Message.Builder(defaultMessages.ASK_FOR_TICKET_OWNER).build()
                }
                defaultMessages.MY_TICKETS -> {
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_MY_TICKETS, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_OWNER_TO_CONSULT,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_OWNER_TO_CONSULT
                        )
                    )
                    Message.Builder(defaultMessages.ASK_FOR_TICKET_OWNER_TO_CONSULT).build()
                }
                defaultMessages.TICKET_DETAILS -> {
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_TICKET_DETAILS, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_NUMBER,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_NUMBER
                        )
                    )
                    Message.Builder(defaultMessages.ASK_FOR_TICKET_NUMBER).build()
                }
                defaultMessages.ANSWER_TICKET -> {
                    messageController.save(
                        form.convert().incoming(defaultMessages.CODE_ANSWER_TICKET, instance, true)
                    )
                    messageController.save(
                        Message().outbound(
                            defaultMessages.CODE_ASK_FOR_TICKET_NUMBER_TO_ANSWER,
                            instance,
                            from = form.to!!,
                            to = form.from!!,
                            defaultMessages.ASK_FOR_TICKET_NUMBER_TO_ANSWER
                        )
                    )
                    Message.Builder(defaultMessages.ASK_FOR_TICKET_NUMBER_TO_ANSWER).build()
                }
                defaultMessages.LIVE_CHAT -> {
                    messageController.save(form.convert().incoming(defaultMessages.CODE_LIVE_CHAT, instance, true))
                    if (instance.user == null) {
                    var operatorPhone = messageController.userController.getAvailableUser(instance)
                    val split = operatorPhone?.split("::")
                    if (!split.isNullOrEmpty()) operatorPhone = split[0]
                        if (operatorPhone != null) {
                            messageController.save(Message().outbound(defaultMessages.CODE_LIVE_CHAT,instance,from = form.to!!,to = form.from!!,defaultMessages.ASK_TO_WAIT_FOR_CONTACT))
                            if (split != null && split.contains("SHIFT")) {
                                sendMessage(instance.contact!!, operatorPhone,"Ola, por favor entre em contato com ${instance.contactName} atraves do numero ${instance.contact}")
                            }
                            Message.Builder(defaultMessages.ASK_TO_WAIT_FOR_CONTACT).build()
                        } else {
                            messageController.save(Message().outbound(defaultMessages.CODE_LIVE_CHAT,instance,from = form.to!!,to = form.from!!,defaultMessages.ASK_TO_WAIT_FOR_CONTACT))
                            instance.chatAwaitStart = Timestamp.from(Instant.now())
                            instanceController.instanceRepository.save(instance)
                            Message.Builder(defaultMessages.ASK_TO_WAIT_FOR_CONTACT).build()
                        }
                    } else {
                        null
                    }
                }
                else -> {
                    when(lastMessage?.code) {
                        defaultMessages.CODE_LIVE_CHAT -> {
                            lastMessage.answered = true
                            messageController.save(lastMessage)
                            messageController.save(form.convert().incoming(defaultMessages.CODE_LIVE_CHAT, instance, true))
                            null
                        }
                     else -> Message.Builder(defaultMessages.WELCOME_MESSAGE).build()
                    }
                }
            }
        }

        return if(responseMessage != null) {
            MessagingResponse.Builder().message(responseMessage).build().toXml()
        } else {
            null
        }

    }*/

    fun sendMessage(instance: Instance, msg: String): Message {
        val message = Message()
        val user = messageController.userController.findByUsername(SecurityService().authenticatedUser ?: "")
        message.outbound("LIVE_CHAT", instance, instance.twilioNumber!!, instance.contact!!, msg, user)
        messageController.save(message)

        Twilio.init(
            credentialsService.twilioConfig.getString(credentialsService.twilioClient),
            credentialsService.twilioConfig.getString(credentialsService.twilioSecret)
        )

        TwilioMessage.creator(
            PhoneNumber("${instance.contact}"),
            PhoneNumber("${instance.twilioNumber}"),
            msg)
            .create()

        return message
    }

    fun sendMessage(from: String, to: String, msg: String) {
        Twilio.init(
            credentialsService.twilioConfig.getString(credentialsService.twilioClient),
            credentialsService.twilioConfig.getString(credentialsService.twilioSecret)
        )

        TwilioMessage.creator(
            PhoneNumber(to),
            PhoneNumber(from),
            msg)
            .create()
    }

}