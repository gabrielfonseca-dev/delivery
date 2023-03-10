package com.algasko.delivery.security.dev

import com.algasko.delivery.data.entity.Role
import com.algasko.delivery.data.repository.RoleRepository
import com.algasko.delivery.data.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Profile("dev")
@Component
class UserDetailsManager(dataSource: DataSource) : JdbcUserDetailsManager(dataSource) {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Override
    override fun loadUserByUsername(username: String): UserDetails {
        val user: com.algasko.delivery.data.entity.User = userRepository.findByUsername(username) ?:
        return org.springframework.security.core.userdetails.User(
            "user",
            "user",
            false,
            false,
            false,
            false,
            getAuthorities(roleRepository.findByName("USER")!!)
        )
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            user.enabled,
            true,
            true,
            true,
            getAuthorities(user.role!!)
        )
    }

    private fun getAuthorities(role: Role): Collection<GrantedAuthority> {
        return getGrantedAuthorities(getPrivileges(role))
    }

    private fun getPrivileges(role: Role): List<String> {
        val privileges: MutableList<String> = ArrayList()
        privileges.add(role.name)
        role.permissions.map { privileges.add(it!!.code) }
        return privileges
    }

    private fun getGrantedAuthorities(privileges: List<String>): Collection<GrantedAuthority> {
        val authorities: MutableList<GrantedAuthority> = ArrayList()
        for (privilege in privileges) {
            authorities.add(SimpleGrantedAuthority(privilege))
        }
        return authorities
    }

}