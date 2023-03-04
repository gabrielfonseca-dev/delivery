package com.algasko.delivery

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableCaching
@EnableAutoConfiguration
@EnableWebMvc
@Push(PushMode.MANUAL)
@Theme(themeClass = Lumo::class)
@PWA(name = "Delivery", shortName = "Delivery")
class DeliveryApp : SpringBootServletInitializer(), AppShellConfigurator  {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<DeliveryApp>(*args)
        }
    }
	@Override
	override fun configure(builder: SpringApplicationBuilder?): SpringApplicationBuilder {
		return builder!!.sources(DeliveryApp::class.java)
	}
}
