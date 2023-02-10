package org.quizbe.dao

import org.quizbe.model.Question
import org.quizbe.model.Scope
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository

interface QuestionRepository : JpaRepository<Question?, Long?> {

    @Query("select min(id) from Question where id > :id and scope = :scope")
    fun findNextById(id: Long, scope: Scope): Int?

    @Query("select max(id) from Question where id < :id and scope = :scope")
    fun findPreviousById(id: Long, scope: Scope): Int?
    //
    @Query("select min(id) from Question where scope = :scope")
    fun findFirstByScope(scope: Scope): Int?

    @Query("select max (id) from Question where scope = :scope")
    fun findLastByScope(scope: Scope): Int?

    fun findByScopeIdAndTopicId(scopeId: Long, topicId: Long): List<Question>

}