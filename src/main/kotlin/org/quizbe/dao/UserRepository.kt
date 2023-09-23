package org.quizbe.dao

import org.quizbe.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : PagingAndSortingRepository<User, Long> {
    fun findByEmail(email: String?): User?
    fun findByUsername(username: String?): User?

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', ?1,'%'))")
    fun findByUsernameWithPagination(name: String, pageable: Pageable): Page<User>

    @Query("SELECT u.* FROM userq u WHERE u.id IN (SELECT ur.user_id FROM user_roles ur WHERE ur.role_id IN (SELECT r.id FROM role r WHERE r.name = ?1))", nativeQuery = true)
    fun findByRoleWithPagination(role: String, pageable: Pageable): Page<User>
}
