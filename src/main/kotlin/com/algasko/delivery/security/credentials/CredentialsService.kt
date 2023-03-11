package com.algasko.delivery.security.credentials

import org.atmosphere.config.service.Singleton
import org.json.JSONObject
import org.springframework.stereotype.Component

@Singleton
@Component
class CredentialsService(private var secretManager: SecretManager) {

    var database = JSONObject()
    var cognitoConfig = JSONObject()
    var twilioConfig = JSONObject()

    final val dbHost = "database-host"
    final val dbSchema = "database-schema"
    final val dbUser = "database-user"
    final val dbPassword = "database-password"
    final val cognitoClient = "cognito-clientId"
    final val cognitoSecret = "cognito-clientSecret"
    final val twilioClient = "twilio-clientId"
    final val twilioSecret = "twilio-clientSecret"

    fun getCredentials() {

        try {
            val secrets = secretManager.getSecrets()
            this.database = JSONObject()
                .put(this.dbHost, secrets?.get(this.dbHost)?.asText())
                .put(this.dbSchema, secrets?.get(this.dbSchema)?.asText())
                .put(this.dbUser, secrets?.get(this.dbUser)?.asText())
                .put(this.dbPassword, secrets?.get(this.dbPassword)?.asText())
            this.cognitoConfig
                .put(this.cognitoClient,secrets?.get(this.cognitoClient)?.asText())
                .put(this.cognitoSecret,secrets?.get(this.cognitoSecret)?.asText())
            this.twilioConfig
                .put(this.twilioClient,secrets?.get(this.twilioClient)?.asText())
                .put(this.twilioSecret,secrets?.get(this.twilioSecret)?.asText())
        } catch (e: Exception) {
            System.err.println(e.message)
        }

    }

}