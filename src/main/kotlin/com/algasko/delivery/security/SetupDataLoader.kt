package com.algasko.delivery.security

import com.algasko.delivery.data.entity.Permission
import com.algasko.delivery.data.entity.Role
import com.algasko.delivery.data.entity.User
import com.algasko.delivery.data.enum.PermissionType
import com.algasko.delivery.data.enum.Privileges
import com.algasko.delivery.data.repository.PermissionRepository
import com.algasko.delivery.data.repository.RoleRepository
import com.algasko.delivery.data.repository.UserRepository
import com.algasko.delivery.view.UserView
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SetupDataLoader(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val privilegeRepository: PermissionRepository
) : ApplicationListener<ContextRefreshedEvent?> {

    private var alreadySetup = false

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (alreadySetup) return

        val views = listOf(UserView::class.java.simpleName)
        val adminPrivileges: LinkedHashSet<Permission> = linkedSetOf(createPrivilegeIfNotFound("${Privileges.WRITE}"))
        views.forEach { adminPrivileges.add(createViewsIfNotFound(it, views.indexOf(it))) }
        createRoleIfNotFound("ADMIN", adminPrivileges)
        createRoleIfNotFound("DRIVER", listOf(null))

        val adminRole: Role? = roleRepository.findByName("ADMIN")
        val defaultUser = Security.defaultSystemUser
        if (userRepository.findByUsername(defaultUser) == null) {
            val user = User()
            user.name = defaultUser
            user.username = defaultUser
            user.password = defaultUser
            user.email = "$defaultUser@domain.com.br"
            user.role = adminRole
            user.enabled = true
            user.document = "000.000.000-00"
            userRepository.save<User>(user)
        }
        alreadySetup = true
    }

    @Transactional
    fun createPrivilegeIfNotFound(name: String?): Permission {
        var privilege: Permission? = privilegeRepository.findByCode(name.toString())
        if (privilege == null) {
            privilege = Permission()
            privilege.code = name!!
            privilege.type = PermissionType.PRIVILEGE
            privilege.active = 1
            privilegeRepository.save(privilege)
        }
        return privilege
    }

    @Transactional
    fun createRoleIfNotFound(name: String, privileges: Collection<Permission?>): Role? {
        var role: Role? = name.let { roleRepository.findByName(it) }
        if (role == null) {
            role = Role()
            role.name = name
            role.permissions = privileges.toMutableList()
            roleRepository.save(role)
        }
        return role
    }

    @Transactional
    fun createViewsIfNotFound(code: String, position: Int): Permission {
        var view: Permission? = privilegeRepository.findByCode(code)
        if (view == null) {
            view = Permission()
            view.code = code
            view.position = position
            view.description = code.substringBefore("View")
            view.active = 1
            view.icon = getIcon(code)
            view.type = PermissionType.MENU
            privilegeRepository.save(view)
        }
        return view
    }

    fun getIcon(view: String): String {
        return when (view) {
            "UserView" -> "users"
            else -> ""
        }
    }

}