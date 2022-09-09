package org.quizbe.dao

import org.quizbe.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role?, Long?> {
    fun findByName(roleName: String?): Role?
    fun findAllByOrderByName(): List<Role?>?
}