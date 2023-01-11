package org.quizbe.dao

import org.quizbe.model.Question
import org.quizbe.model.Rating
import org.quizbe.model.Response
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResponseRepository : JpaRepository<Response?, Long?> {


    fun findByQuestion(question: Question?): List<Response?>
}
