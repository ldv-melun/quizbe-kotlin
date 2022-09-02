package org.quizbe.service;

import org.quizbe.dao.RatingRepository;
import org.quizbe.dao.RoleRepository;
import org.quizbe.model.Question;
import org.quizbe.model.Rating;
import org.quizbe.model.Role;
import org.quizbe.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {
    RatingRepository ratingRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository){
        this.ratingRepository = ratingRepository;
    }

    public void save(Rating rating) {
        ratingRepository.save(rating);
    }

    public Optional<Rating> getRating(User user, Question question) {
        return ratingRepository.findUserRatingOfThisQuestion(user, question);
    }

    public Optional<Rating> findById(long idRating) {
        return ratingRepository.findById(idRating);
    }

    public void delete(Rating userRating) {
        ratingRepository.delete(userRating);
    }
}
