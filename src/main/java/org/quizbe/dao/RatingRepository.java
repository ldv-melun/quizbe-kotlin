package org.quizbe.dao;

import org.quizbe.model.Question;
import org.quizbe.model.Rating;
import org.quizbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query(value = "select r from Rating r where r.question = :q and r.user = :u")
    Optional<Rating> findUserRatingOfThisQuestion(User u, Question q);

//  @Query("select min(id) from Question where id > :id ")
//  Integer findNextById(long id);

}
