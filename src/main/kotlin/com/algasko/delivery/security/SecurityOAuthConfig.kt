package com.algasko.delivery.security

import com.algasko.delivery.security.credentials.CredentialsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

@Profile("prod")
@Configuration
class SecurityOAuthConfig(private val credentialsService: CredentialsService) {

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(clientRegistration())
    }

    private fun clientRegistration(): ClientRegistration {
        credentialsService.getCredentials()
        val cognito = credentialsService.cognitoConfig
        return ClientRegistration.withRegistrationId("cognito")
            .clientId(cognito.getString(credentialsService.cognitoClient))
            .clientSecret(cognito.getString(credentialsService.cognitoSecret))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            /*.redirectUri("")
            .scope("openid")
            .issuerUri("")
            .authorizationUri("")
            .tokenUri("")
            .userInfoUri("")
            .userNameAttributeName("cognito:username")
            .jwkSetUri("")
            .clientName("delivery")*/
            .build()
    }

}