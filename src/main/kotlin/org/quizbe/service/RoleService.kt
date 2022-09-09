package org.quizbe.service

import org.quizbe.dao.RoleRepository
import org.quizbe.model.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RoleService @Autowired constructor(var roleRepository: RoleRepository) {
    fun findByName(roleName: String?): Role? {
        return roleRepository.findByName(roleName)
    }

    fun findAllByOrderByName(): List<Role?>? {
        return roleRepository.findAllByOrderByName()
    }

    fun saveRole(role: Role) {
        roleRepository.save(role)
    }
}