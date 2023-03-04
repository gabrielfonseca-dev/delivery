package com.algasko.delivery.view

import com.algasko.delivery.controller.MessageController
import com.algasko.delivery.controller.TwilioClient
import com.algasko.delivery.controller.UserController
import com.algasko.delivery.data.entity.Permission
import com.algasko.delivery.security.Security
import com.algasko.delivery.security.SecurityService
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import java.util.*

@SpringComponent
@UIScope
@CssImport(value = "./styles/default.css")
@JsModule("./script/chat.js")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
class MainLayout(
    val userController: UserController,
    val messageController: MessageController,
    val twilioClient: TwilioClient
) : AppLayout() {

    private val toggle = DrawerToggle()
    private val header = Header()
    private val menuBar = HorizontalLayout()
    private val nav = Nav()
    private val menuList = VerticalLayout()
    private val livechat = HorizontalLayout()
    private val chatList: MutableList<ChatComponent.ChatInfo> = ArrayList()
    private var permissions: MutableList<Permission> = ArrayList()

    init {
        UI.getCurrent().page.addStyleSheet("https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css")
        this.toggle.isVisible = userController.findByUsername(SecurityService().authenticatedUser ?: "")?.driver == false
        this.toggle.element.setAttribute("aria-label", "Menu toggle")
        this.livechat.addClassNames("d-flex", "justify-content-center")
        this.livechat.alignItems = FlexComponent.Alignment.CENTER
        this.primarySection = Section.NAVBAR
        this.isDrawerOpened = false
        this.addToNavbar(true, createHeaderContent())
        this.addToDrawer(createDrawerContent())
    }

    private fun createChat(chatComponent: ChatComponent) {
        var openConversations =
            userController.getOpenInstances(userController.findByUsername(SecurityService().authenticatedUser ?: ""))
        if (openConversations.isNotEmpty()) {
            for (i in 0 until livechat.componentCount) {
                openConversations =
                    openConversations.filter { it.id.toString() != livechat.getComponentAt(i).id.toString() }
            }
            chatComponent.chatList = this.chatList
            chatComponent.livechat = this.livechat
            chatComponent.createChat(openConversations)
        }
    }

    private fun createHeaderContent(): Component {
        createTopMenuBar()
        this.menuBar.alignItems = FlexComponent.Alignment.CENTER
        this.menuBar.setWidthFull()
        this.header.setWidthFull()
        this.header.add(menuBar)
        return header
    }

    private fun createTopMenuBar() {

        val logoutBtn = Button("Sair", Icon(VaadinIcon.EXIT))
        logoutBtn.isVisible = Security.isUserLoggedIn
        logoutBtn.isIconAfterText = true
        logoutBtn.addClickListener { SecurityService().logout() }

        val loginBtn = Button("Entrar", Icon(VaadinIcon.SIGN_IN)) {
            UI.getCurrent().page.open("/oauth2/authorization/cognito","_self")
        }
        loginBtn.isVisible = !Security.isUserLoggedIn

        val homeBtn = Button("Entregas", Icon(VaadinIcon.PACKAGE)) { UI.getCurrent().page.open("/", "_self") }
        homeBtn.isIconAfterText = true
        homeBtn.isVisible = Security.isUserLoggedIn

        val headerLayout = HorizontalLayout(toggle)
        headerLayout.setWidthFull()

        val userMenu = HorizontalLayout(logoutBtn, loginBtn)
        userMenu.justifyContentMode = FlexComponent.JustifyContentMode.END

        val menuComponents = HorizontalLayout(homeBtn, livechat)

        if((userController.findByUsername(SecurityService().authenticatedUser ?: "")?.driver == true)) {
            val chatComponent = ChatComponent(messageController, userController, twilioClient)
            chatComponent.chatList = this.chatList
            chatComponent.livechat = this.livechat
            chatComponent.setId("refresher")
            createChat(chatComponent)
            menuComponents.add(chatComponent)
        }

        val titleDiv = HorizontalLayout()
        titleDiv.setWidthFull()
        titleDiv.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        titleDiv.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        headerLayout.add(menuComponents, titleDiv, userMenu)
        headerLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        headerLayout.isSpacing = false
        headerLayout.setWidthFull()

        this.menuBar.add(headerLayout)

    }

    private fun createDrawerContent(): Component {
        return Section(createNavigation())
    }

    private fun createNavigation(): Nav {
        nav.element.setAttribute("aria-labelledby", "views")
        menuList.alignItems = FlexComponent.Alignment.CENTER
        menuList.setWidthFull()
        menuList.add(Image("icons/icon-minor.png", "support"))
        nav.add(menuList)
        createLinks()
        val user = userController.findByUsername(SecurityService().authenticatedUser ?: "") ?: return nav
        val info = VerticalLayout()
        info.add(Span(user.name))
        info.isPadding = false
        info.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        menuList.add(info)
        return nav
    }

    private fun createLinks() {
        if (Security.isUserLoggedIn) {
            val role = userController.findByUsername(SecurityService().authenticatedUser ?: "")?.role ?: return
            permissions = userController.findMenuListByRole(role.name).toMutableList()

            if (permissions.isNotEmpty()) permissions.forEach { i ->
                @Suppress("UNCHECKED_CAST") val div = Div(
                    Icon(i.icon ?: ""), createLink(
                        MenuItemInfo(
                            i.description ?: "",
                            (Class.forName("${this.javaClass.packageName}.${i.code}") as Class<out Component>)
                        )
                    )
                )
                div.setSizeFull()
                div.addClassNames("d-flex", "justify-content-between")
                menuList.add(div)
            }

        }
    }

    private fun createLink(menuItemInfo: MenuItemInfo): RouterLink {
        val link = RouterLink()
        link.setRoute(menuItemInfo.view)
        val label = Label(menuItemInfo.text)
        link.add(label)
        return link
    }

    private class MenuItemInfo(val text: String, val view: Class<out Component>)

}