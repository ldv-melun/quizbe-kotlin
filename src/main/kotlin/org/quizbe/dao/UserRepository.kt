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

    @Query("SELECT u.* FROM USERQ u WHERE u.ID IN (SELECT ur.USER_ID FROM USER_ROLES ur WHERE ur.ROLE_ID IN (SELECT r.ID FROM ROLE r WHERE r.NAME = ?1))", nativeQuery = true)
    fun findByRoleWithPagination(role: String, pageable: Pageable): Page<User>
}
