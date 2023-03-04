package com.algasko.delivery.controller

import com.algasko.delivery.data.entity.Instance
import com.algasko.delivery.data.entity.Permission
import com.algasko.delivery.data.entity.User
import com.algasko.delivery.data.enum.LogCode
import com.algasko.delivery.data.enum.LogType
import com.algasko.delivery.data.enum.PermissionType
import com.algasko.delivery.data.repository.PermissionRepository
import com.algasko.delivery.data.repository.UserRepository
import com.algasko.delivery.data.repository.specification.UserSpecification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Controller

@Controller
class UserController(
    var userRepository: UserRepository,
    var permissionRepository: PermissionRepository,
    var instanceController: InstanceController,
    var logController: LogController
) {

    private val specification = UserSpecification()

    fun search(param: String, boolean: Boolean): Page<User> {
        return userRepository.findAll(
            Specification.where(
                specification.name(param).or(
                    specification.login(param)
                )
                    .or(specification.email(param))
                    .or(specification.document(param))
            )
                .and(specification.inactive(boolean)),
            Pageable.ofSize(50)
        )
    }

    fun findMenuListByRole(role: String): List<Permission> {
        return permissionRepository.listRoleMenu(role, PermissionType.MENU)
    }

    fun findByUsername(name: String): User? {
        return userRepository.findByUsername(name)
    }

    fun save(user: User): User {
        logController.saveLog(LogCode.USER, LogType.UPDATE, null, user.toJson(), null, null)
        userRepository.save(user)
        return user
    }

    fun closeInstance(instance: Instance) {
        instanceController.close(instance)
    }

    fun getOpenInstances(user: User?): List<Instance> {
        return if (user != null) {
            instanceController.instanceRepository.findOpenInstanceByUser(user.id!!)
        } else {
            listOf()
        }
    }

}