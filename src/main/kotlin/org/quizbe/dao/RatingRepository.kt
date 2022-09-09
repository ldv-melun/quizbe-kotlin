package org.quizbe.dao

import org.quizbe.model.Question
import org.quizbe.model.Rating
import org.quizbe.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RatingRepository : JpaRepository<Rating?, Long?> {

    @Query("select r from Rating r where r.user = :u")
    fun findAllByUser(u: User): List<Rating>

    @Query(value = "select r from Rating r where r.question = :q and r.user = :u")
    fun findUserRatingOfThisQuestion(u: User?, q: Question?): Optional<Rating?>? //  @Query("select min(id) from Question where id > :id ")
    //  Integer findNextById(long id);
}