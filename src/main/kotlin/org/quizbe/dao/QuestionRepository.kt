package org.quizbe.dao

import org.quizbe.model.Question
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<Question?, Long?> {
    @Query("select min(id) from Question where id > :id ")
    fun findNextById(id: Long): Int?
}