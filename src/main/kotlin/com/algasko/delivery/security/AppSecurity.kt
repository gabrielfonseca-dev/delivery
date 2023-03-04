package com.algasko.delivery.security

import com.algasko.delivery.data.enum.Privileges
import com.algasko.delivery.data.repository.UserRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
interface AppSecurity {

    fun hasPrivilege(privilege: Privileges, userRepository: UserRepository): Boolean {
        return userRepository.findByUsername(SecurityService().authenticatedUser ?: "")
            ?.role?.permissions?.find { it?.code == "$privilege" } != null
    }

    fun hasPrivilege(privilege: String, userRepository: UserRepository): Boolean {
        return userRepository.findByUsername(SecurityService().authenticatedUser ?: "")?.role?.permissions?.find { it?.code == privilege } != null
    }

    fun userExists(name: String, username: String, email: String, document: String, userRepository: UserRepository): Boolean {
        return userRepository.findByNameOrUsernameOrEmailOrDocument(name, username, email, document) != null
    }

    fun generatePassword(): String {
        val upperCaseLetters = RandomStringUtils.random(4, 65, 90, true, true)
        val lowerCaseLetters = RandomStringUtils.random(4, 97, 122, true, true)
        val numbers = RandomStringUtils.randomNumeric(4)
        val specialChar = RandomStringUtils.random(4, 33, 47, false, false)
        val totalChars = RandomStringUtils.randomAlphanumeric(4)
        val combinedChars = upperCaseLetters + lowerCaseLetters + numbers + specialChar + totalChars
        val pwdChars = combinedChars.chars()
            .mapToObj { c: Int -> c.toChar() }
            .collect(Collectors.toList())
        pwdChars.shuffle()
        return pwdChars.stream().map(Char::toString).collect(Collectors.joining())
    }

}