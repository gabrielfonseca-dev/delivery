package com.algasko.delivery.security.dev

import com.algasko.delivery.security.Security
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDecisionVoter.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import java.util.*

@Profile("dev")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
class SecurityConfig : GlobalMethodSecurityConfiguration() {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http.csrf().disable()
            .formLogin().loginPage("/login").permitAll().defaultSuccessUrl("/")
            .and().logout().logoutSuccessUrl("/")
            .and().authorizeRequests()
            .requestMatchers(Security::isFrameworkInternalRequest).permitAll()
            .requestMatchers(Security::isApiRequest).permitAll()
            .and().authorizeRequests().antMatchers(
                "/VAADIN/**",
                "/vaadinServlet/**",
                "/vaadinServlet/UIDL/**",
                "/vaadinServlet/HEARTBEAT/**",
                "/manifest.webmanifest",
                "/sw.js",
                "/offline.html",
                "/icons/**",
                "/images/**",
                "/styles/**",
                "/img/**"
            ).permitAll()
            .mvcMatchers("/admin/**").hasAuthority("ADMIN")
            .anyRequest().authenticated()
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(5)
    }

    override fun authenticationManager(): AuthenticationManager? {
        return authenticationManager
    }

}