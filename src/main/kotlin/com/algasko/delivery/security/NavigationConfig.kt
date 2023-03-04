package com.algasko.delivery.security

import com.algasko.delivery.data.repository.UserRepository
import com.algasko.delivery.security.Security.isUserLoggedIn
import com.algasko.delivery.security.dev.LoginView
import com.algasko.delivery.view.MainView
import com.algasko.delivery.view.UserView
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("prod")
@Component
class NavigationConfig(var userRepository: UserRepository) : VaadinServiceInitListener {

    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addUIInitListener { uiEvent: UIInitEvent ->
            val ui = uiEvent.ui
            ui.addBeforeEnterListener { event: BeforeEnterEvent -> beforeEnter(event) }
        }
    }

    private fun beforeEnter(event: BeforeEnterEvent) {
        val user = userRepository.findByUsername(SecurityService().authenticatedUser ?: "")

        if (LoginView::class.java != event.navigationTarget && !isUserLoggedIn && MainView::class.java != event.navigationTarget) {
            event.rerouteTo(LoginView::class.java)
        }
        if (LoginView::class.java == event.navigationTarget && isUserLoggedIn) {
            event.rerouteTo(MainView::class.java)
        }
        if (UserView::class.java == event.navigationTarget && (user)?.role?.permissions?.find { p -> p?.code == UserView::class.simpleName } == null || user?.role?.name != "ADMIN") {
            event.rerouteTo(MainView::class.java)
        }
    }

}