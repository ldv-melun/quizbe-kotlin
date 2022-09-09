package org.quizbe.service

import org.quizbe.dao.RatingRepository
import org.quizbe.model.Question
import org.quizbe.model.Rating
import org.quizbe.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class RatingService @Autowired constructor(var ratingRepository: RatingRepository) {
    fun save(rating: Rating) {
        ratingRepository.save(rating)
    }

    fun getRating(user: User?, question: Question?): Optional<Rating?>? {
        return ratingRepository.findUserRatingOfThisQuestion(user, question)
    }

    fun findById(idRating: Long): Optional<Rating?> {
        return ratingRepository.findById(idRating)
    }

    fun delete(userRating: Rating) {
        ratingRepository.delete(userRating)
    }

    fun findAllByUser(user :User): List<Rating?> {
        return ratingRepository.findAllByUser(user)
    }
}